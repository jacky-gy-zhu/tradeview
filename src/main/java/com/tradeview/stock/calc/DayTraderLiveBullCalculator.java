package com.tradeview.stock.calc;

import com.tradeview.stock.model.StockData;
import com.tradeview.stock.model.StockResult;

import java.util.List;

public class DayTraderLiveBullCalculator extends AbstractCalculator {

    public DayTraderLiveBullCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match(StockResult stockResult) {
        boolean result =
				// 今日高开
				matchTodayK();
        return result;
    }

	private boolean matchTodayK() {
		double topen = todayStock.getTopen();
		double yclose = chartStocks.get(0).getTclose();

		return
				(topen > yclose);
	}

	@Override
    public String getName() {
        return "当冲高开做多";
    }

}
