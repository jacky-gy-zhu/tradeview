package com.tradeview.stock.calc;

import com.tradeview.stock.config.Constants;
import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.*;

import java.util.List;

public class HeaderFooterHigherCalculator extends AbstractCalculator {

    public HeaderFooterHigherCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match(StockResult stockResult) {
		ThreeFooter threeFooter = new ThreeFooter();
		RiskManagement riskManagement = new RiskManagement();
        boolean result =
				// 今日收盘大于MA5，今日红K，今日收盘大于昨日高点
				matchTodayK() &&
				// MA5头头高底底高
				matchMA5HeaderFooterHigher(threeFooter) &&
				// K线（高点和低点）头头高底底高
				matchHighLowHeaderFooterHigher(threeFooter, riskManagement) &&
				// 前2次回调幅度差不多
				matchPeriodEven(threeFooter) &&
				// 高点和低点之间的震动幅度大
				matchHighLowWave(threeFooter);
		if (result) {
//			stockResult.setPeriod(threeFooter.getPeriod1() + " ~ " + threeFooter.getPeriod2());
			stockResult.setPeriod(riskManagement.getDescription());
			stockResult.setSort(riskManagement.getRate());
		}
        return result;
    }

	private boolean matchTodayK() {
    	double tclose = todayStock.getTclose();
    	double topen = todayStock.getTopen();
    	double yhigh = chartStocks.get(0).getThigh();
//    	double ylow = chartStocks.get(0).getTlow();
    	double tMa5 = calcTodayMa(5, tclose);
//		double yMa5 = calcMa(5, 0);

		// MA20向上
		double ma20 = calcTodayMa(20, tclose);
		double _ma20 = calcMa(20, 0);
		return
				(tclose > tMa5) &&
				(tclose > topen) &&
				(tclose > yhigh) &&
				(ma20 > _ma20);
//				(ylow < yMa5);
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

	private boolean matchHighLowHeaderFooterHigher(ThreeFooter threeFooter, RiskManagement riskManagement) {
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

		threeFooter.setH1(header1);
		threeFooter.setH2(header2);
		threeFooter.setF1(footer1);
		threeFooter.setF2(footer2);
		threeFooter.setF3(footer3);
		threeFooter.setPeriod1(footer2Obj.getIndex() - footer1Obj.getIndex());
		threeFooter.setPeriod2(footer3Obj.getIndex() - footer2Obj.getIndex());

		boolean success =
				(header1 > header2) &&
				((footer1 > footer2) && (footer2 > footer3)) &&
				(footer1 > header2);

		if (success) {
			double preH = header1;
			double preL = footer2;
			double startL = footer1;
			double target = preH - preL + startL;
			double buy = todayStock.getTclose();
			double quit = chartStocks.get(0).getTclose();
			double rate = (target - buy) / (buy - quit);
			double targetRate = (target - buy) / buy;
			double lossRate = (quit - buy) / buy;
			riskManagement.setBuy(buy);
			riskManagement.setLoss(quit);
			riskManagement.setTarget(target);
			riskManagement.setDescription("目标价：" + Constants.DF.format(riskManagement.getTarget()) + " (+" + (int) (targetRate * 100) + "%，" + (int)(lossRate*100) + "%)"
					+ "，止损价：" + Constants.DF.format(riskManagement.getLoss())
					+ "，盈亏比：" + (int) rate + ":1");
			riskManagement.setRate(rate);
		}

		return success;
	}

	private boolean matchPeriodEven(ThreeFooter threeFooter) {
		int period1 = threeFooter.getPeriod1();
		int period2 = threeFooter.getPeriod2();
		return calcGapPercentage(period1, period2) > Param.THREE_FOOTER_PERIOD_EVEN_RATE;
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
				(calcRate(h1, f2) > Param.THREE_FOOTER_HIGH_LOW_WAVE_RATE) &&
				(calcRate(h2, f3) > Param.THREE_FOOTER_HIGH_LOW_WAVE_RATE) &&
				(calcRate(h1, f1) > Param.THREE_FOOTER_HIGH_LOW_WAVE_RATE2) &&
				(calcRate(h2, f2) > Param.THREE_FOOTER_HIGH_LOW_WAVE_RATE2) &&
				((h1-f1)/(h1-f2) <= Param.GOLD_CUT_RATE) &&
				((h2-f2)/(h2-f3) <= Param.GOLD_CUT_RATE2) &&
				((period1 < Param.THREE_FOOTER_PERIOD_GAP_DAYS) && (period2 < Param.THREE_FOOTER_PERIOD_GAP_DAYS));
	}

	@Override
    public String getName() {
        return "头头高底底高";
    }

}
