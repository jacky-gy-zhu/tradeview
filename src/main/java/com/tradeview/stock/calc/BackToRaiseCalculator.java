package com.tradeview.stock.calc;


import com.tradeview.stock.model.StockData;

import java.util.List;

public class BackToRaiseCalculator extends AbstractCalculator {

    public BackToRaiseCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match() {
        return
				// 今日收盘大于昨日高点，今日红K，今日站上MA5，昨日黑K，昨日在MA5下方，今日MA5大于等于昨日MA5
				matchRedK();
    }

	private boolean matchRedK() {
		// 今日收盘价
		double tclose = todayStock.getTclose();

		// 今日开盘
		double topen = todayStock.getTopen();

		// 昨日高点
		StockData ystStockData = chartStocks.get(0);
		double yHigh = ystStockData.gettHigh();

		// 昨日开盘
		double yopen = ystStockData.getTopen();

		// 昨日收盘
		double yclose = ystStockData.getTclose();

		// 今日MA5
		double ma5 = calcTodayMa(5, tclose);

		// 昨日MA5
		double _ma5 = calcMa(5, 0);

//		return (tclose > yHigh) &&
//				(tclose > topen) &&
//				(tclose > ma5) &&
//				(yopen > yclose) &&
//				(yclose < _ma5) &&
//				(ma5 >= _ma5);
		return false;
	}


	@Override
    public String getName() {
        return "回后起涨";
    }
}
