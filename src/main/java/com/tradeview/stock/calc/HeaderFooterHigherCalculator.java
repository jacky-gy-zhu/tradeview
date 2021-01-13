package com.tradeview.stock.calc;

import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.StockData;
import com.tradeview.stock.model.StockPoint;
import com.tradeview.stock.model.StockResult;
import com.tradeview.stock.model.ThreeFooter;

import java.util.List;

public class HeaderFooterHigherCalculator extends AbstractCalculator {

    public HeaderFooterHigherCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match(StockResult stockResult) {
		ThreeFooter threeFooter = new ThreeFooter();
        boolean result =
				// 今日收盘大于MA5，今日红K，今日收盘大于昨日高点，昨日低点在MA5下方
				matchTodayK() &&
				// MA5头头高底底高
				matchMA5HeaderFooterHigher(threeFooter) &&
				// K线（高点和低点）头头高底底高
				matchHighLowHeaderFooterHigher(threeFooter) &&
				// 3个脚点低点可以连成一线
				matchFooterInOneLine(threeFooter);
		if (result) {
			stockResult.setPeriod(threeFooter.getPeriod1() + " ~ " + threeFooter.getPeriod2());
		}
        return result;
    }

	private boolean matchTodayK() {
    	double tclose = todayStock.getTclose();
    	double topen = todayStock.getTopen();
    	double yhigh = chartStocks.get(0).getThigh();
    	double ylow = chartStocks.get(0).getTlow();
    	double tMa5 = calcTodayMa(5, tclose);
		double yMa5 = calcMa(5, 0);
		return
				(tclose > tMa5) &&
				(tclose > topen) &&
				(tclose > yhigh) &&
				(ylow < yMa5);
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
			StockData stockData = chartStocks.get(i);
			double ma = calcMa(5, i);
			double _ma = calcMa(5, i+1);

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
					if (h1 <= h2) {
						return false;
					}
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

		return
				// 头头高
				(h1 > h2) &&
				// 底底高
				(f1 > f2 && f2 > f3);
	}

	private boolean matchHighLowHeaderFooterHigher(ThreeFooter threeFooter) {
		int p1 = threeFooter.getP1();
		int p2 = threeFooter.getP2();
		int p3 = threeFooter.getP3();

		StockPoint header1Obj = findHighByIndexRange(0, p2);
		StockPoint header2Obj = findHighByIndexRange(p2, p3);
		double header1 = header1Obj.getPrice();
		double header2 = header2Obj.getPrice();
		StockPoint footer1Obj = findLowByIndexRange(0, header1Obj.getIndex());
		double footer1 = footer1Obj.getPrice();
		StockPoint footer2Obj = findLowByIndexRange(header1Obj.getIndex(), header2Obj.getIndex());
		double footer2 = footer2Obj.getPrice();
		StockPoint footer3Obj = findLowByIndexRange(header2Obj.getIndex(), p3+2);
		double footer3 = footer3Obj.getPrice();

		threeFooter.setF1(footer1);
		threeFooter.setF2(footer2);
		threeFooter.setF3(footer3);
		threeFooter.setPeriod1(footer2Obj.getIndex() - footer1Obj.getIndex());
		threeFooter.setPeriod2(footer3Obj.getIndex() - footer2Obj.getIndex());

		return
				(header1 > header2) &&
				((footer1 > footer2) && (footer2 > footer3));
	}

	private boolean matchFooterInOneLine(ThreeFooter threeFooter) {
    	double f1= threeFooter.getF1();
    	double f2 = threeFooter.getF2();
    	double f3 = threeFooter.getF3();
    	int period1 = threeFooter.getPeriod1();
    	int period2 = threeFooter.getPeriod2();

		double expectingF3 = calc3rdFootPrice(f1, f2, period1, period2);
		return calcRate(expectingF3, f3) < Param.THREE_FOOTER_MAX_GAP_RATE;
	}

	private StockPoint findHighByIndexRange(int from, int to) {
    	double high = 0;
    	int index = 0;
		for(int i = from; i < to; i++) {
			StockData stockData = chartStocks.get(i);
			double thigh = stockData.getThigh();

			if (thigh > high) {
				high = thigh;
				index = i;
			}
		}
		return new StockPoint(high, index);
	}

	private StockPoint findLowByIndexRange(int from, int to) {
		double low = 999999;
		int index = 0;
		for(int i = from; i < to; i++) {
			StockData stockData = chartStocks.get(i);
			double tlow = stockData.getTlow();

			if (tlow < low) {
				low = tlow;
				index = i;
			}
		}
		return new StockPoint(low, index);
	}

	@Override
    public String getName() {
        return "头头高低低高";
    }

}
