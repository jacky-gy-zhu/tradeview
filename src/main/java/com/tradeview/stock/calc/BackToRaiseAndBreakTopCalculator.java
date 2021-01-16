package com.tradeview.stock.calc;

import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.StockData;
import com.tradeview.stock.model.StockResult;

import java.util.List;

public class BackToRaiseAndBreakTopCalculator extends BackToRaiseCalculator {

    public BackToRaiseAndBreakTopCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match(StockResult stockResult) {
        return
				// 今日收盘价大于60日内的最高点（但不超过5%），并且红K（超过4%），并且上影线很小
				matchTodayK() &&
				// 回后起涨
				super.match(stockResult);
    }

	private boolean matchTodayK() {
		// 今日收盘价
		double tclose = todayStock.getTclose();

		// 60日内的最高价（除了今日）
		double topPrice = 0;
		for(int i = 0; i < chartStocks.size(); i++) {
			StockData stockData = chartStocks.get(i);
			double high = stockData.getThigh();

			if(high > topPrice) {
				topPrice = high;
			}
		}

		// 今日开盘价，今日红K
		double topen = todayStock.getTopen();

		// 今日最高价，今日上影线很小
		double thigh = todayStock.getThigh();

		// MA20向上
		double ma20 = calcTodayMa(20, tclose);
		double _ma20 = calcMa(20, 0);

		return (tclose > topPrice) &&
				calcRate(tclose, topPrice) < Param.EXCEED_RATE &&
				(tclose > topen) &&
				(calcHighShallowRate(topen, thigh, tclose) < Param.TODAY_HIGH_SHADOW_RATE) &&
				(calcRedKRate(topen, tclose) > Param.TODAY_RED_K_RATE) &&
				(ma20 > _ma20);
	}

	protected boolean matchMa() {
		// 今日
		double tclose = todayStock.getTclose();
		double ma5 = calcTodayMa(5, tclose);
		double ma10 = calcTodayMa(10, tclose);
		double ma20 = calcTodayMa(20, tclose);
		double ma30 = calcTodayMa(30, tclose);

		// 昨日
		double _ma5 = calcMa(5, 0);

		return
				tclose > ma5 &&
				tclose > ma10 &&
				tclose > ma20 &&
				tclose > ma30 &&
				ma5 > _ma5 &&
				calcRate(ma5, ma30) < Param.MA5_MA30_GAP_RATE &&
				calcRate(ma5, ma20) < Param.MA5_MA20_GAP_RATE &&
				calcRate(ma5, ma10) < Param.MA5_MA10_GAP_RATE;
	}

	@Override
    public String getName() {
        return "回后突破";
    }
}
