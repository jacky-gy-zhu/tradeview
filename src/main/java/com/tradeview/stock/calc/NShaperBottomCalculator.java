package com.tradeview.stock.calc;

import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.*;

import java.util.List;

public class NShaperBottomCalculator extends AbstractCalculator {

    public NShaperBottomCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match(StockResult stockResult) {
		ThreeFooter threeFooter = new ThreeFooter();
        boolean result =
				// 今日收盘大于MA5，今日红K，今日收盘大于昨日高点，上影线很短
				matchTodayK() &&
				// MA3形成N字底
				matchMA5HeaderFooterHigher(threeFooter) &&
				// K线（高点和低点）形成N字底
				matchHighLowHeaderFooterHigher(threeFooter) &&
				// 高点和低点之间的震动幅度大
				matchHighLowWave(threeFooter);
				// 回调时期有且只有3根连续黑K（实体部分大于1%)
//				matchBack3BlackK(threeFooter);
        return result;
    }

	private boolean matchTodayK() {
		double thigh = todayStock.getThigh();
    	double tclose = todayStock.getTclose();
    	double topen = todayStock.getTopen();
    	double yhigh = chartStocks.get(0).getThigh();
    	double tMa3 = calcTodayMa(3, tclose);

		return
				(tclose > tMa3) &&
				(tclose > topen) &&
				(tclose > yhigh) &&
				(thigh - tclose) * 3 < (tclose - topen);
	}

	private boolean matchMA5HeaderFooterHigher(ThreeFooter threeFooter) {
    	int step = 0;
		double f1 = 0; // 第三个脚
		int p1 = 0; // 第三个脚
		double h1 = 0; // 第二个脚和第三个脚之间的高点
    	int p2 = 0; // 第二个脚和第三个脚之间的高点
		double f2 = 0; // 第二个脚
    	int p3 = 0; // 第二个脚
		double h2 = 0; // 第一个脚和第二个脚之间的高点
    	int p4 = 0; // 第一个脚和第二个脚之间的高点
		double f3 = 0; // 第一个脚
    	int p5 = 0; // 第一个脚
		for(int i = 0; i < chartStocks.size()-1; i++) {
//			StockData stockData = chartStocks.get(i);
			double ma = calcMa(3, i);
			double _ma = calcMa(3, i+1);

			// 未开始
			if (step == 0) {
				if (_ma > ma) {
					step = 1;
					p1 = i;
					f1 = ma;
				}
			}
			// 向上
			if (step == 1) {
				if (_ma < ma) {
					step = 2;
					p2 = i;
					h1 = ma;
				}
			}
			// 向下
			if (step == 2) {
				if (_ma > ma) {
					step = 3;
					p3 = i;
					f2 = ma;
				}
			}
			// 向上
			if (step == 3) {
				if (_ma < ma) {
					step = 4;
					p4 = i;
					h2 = ma;
				}
			}
			// 向下
			if (step == 4) {
				if (_ma > ma) {
					step = 5;
					p5 = i;
					f3 = ma;
					break;
				}
			}
		}

		threeFooter.setP1(p1);
		threeFooter.setP2(p3);
		threeFooter.setP3(p5);

		threeFooter.setA1(p1);
		threeFooter.setA2(p2);
		threeFooter.setA3(p3);
		threeFooter.setA4(p4);
		threeFooter.setA5(p5);

		return
				// 底高
				(f1 > f2) &&
				// 第二个是高脚
				(h1 - f1) * 3 < (h1 - f2);
	}

	private boolean matchHighLowHeaderFooterHigher(ThreeFooter threeFooter) {
		int p1 = threeFooter.getP1();
		int p2 = threeFooter.getP2();
		int p3 = threeFooter.getP3();

		StockPoint header1Obj = findHighByIndexRange(0, p2);
		if (header1Obj.getIndex() == 0) {
			return false;
		}
		StockPoint header2Obj = findHighByIndexRange(p2, p3);
		double header1 = header1Obj.getPrice();
		double header2 = header2Obj.getPrice();
		StockPoint footer1Obj = findLowByIndexRange(0, header1Obj.getIndex());
		double footer1 = footer1Obj.getPrice();
		StockPoint footer2Obj = findLowByIndexRange(header1Obj.getIndex(), header2Obj.getIndex());
		double footer2 = footer2Obj.getPrice();
		StockPoint footer3Obj = findLowByIndexRange(header2Obj.getIndex(), p3+2);
		double footer3 = footer3Obj.getPrice();

		threeFooter.setH1(header1);
		threeFooter.setH2(header2);
		threeFooter.setF1(footer1);
		threeFooter.setF2(footer2);
		threeFooter.setF3(footer3);
		threeFooter.setPeriod1(footer2Obj.getIndex() - footer1Obj.getIndex());
		threeFooter.setPeriod2(footer3Obj.getIndex() - footer2Obj.getIndex());

		boolean success =
				(footer1 > footer2) &&
				(header1 - footer1) * 2 <= (header1 - footer2);

		return success;
	}

	private boolean matchHighLowWave(ThreeFooter threeFooter) {
    	double h1 = threeFooter.getH1();
    	double h2 = threeFooter.getH2();
    	double f1 = threeFooter.getF1();
    	double f2 = threeFooter.getF2();
    	double f3 = threeFooter.getF3();
		int period1 = threeFooter.getPeriod1();
		int period2 = threeFooter.getPeriod2();

    	return
				(calcRate(h1, f2) > Param.TWO_FOOTER_HIGH_LOW_WAVE_RATE) &&
				(period1 < Param.TWO_FOOTER_PERIOD_GAP_DAYS);
	}

	private boolean matchBack3BlackK(ThreeFooter threeFooter) {
		int p1 = threeFooter.getA1();
		int p2 = threeFooter.getA2();

		int startIndex = p1>0?(p1-1):0;
		int endIndex = p2+1;

		int count = 0;
		for (int i = startIndex; i <= endIndex; i++) {
			StockData stockData = chartStocks.get(i);
			double topen = stockData.getTopen();
			double tclose = stockData.getTclose();
			if (count == 0) {
				if (meetDropMoreThan1Perc(topen, tclose)) {
					count++;
				} else if (topen > tclose) {
					return false;
				}
			} else {
				if (meetDropMoreThan1Perc(topen, tclose)) {
					count++;
				} else if (topen > tclose) {
					return false;
				} else {
					break;
				}
			}
		}

		if (count == 3) {
			return true;
		} else {
			return false;
		}
	}

	private boolean meetDropMoreThan1Perc(double topen, double tclose) {
    	return (topen > tclose) &&
				calcRate(topen, tclose) > 0.01f;
	}

	@Override
    public String getName() {
        return "N字底";
    }

}
