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
				// 今日高开高走
				matchTodayK();
        return result;
    }

	private boolean matchTodayK() {
		double tclose = todayStock.getTclose();
		double topen = todayStock.getTopen();
		double yclose = chartStocks.get(0).getTclose();

		return
				(tclose > topen) &&
				(topen > yclose) &&
				(calcRedKRate(topen, tclose) > 0.007f);
	}

	@Override
    public String getName() {
        return "当日冲高开高走做多";
    }

}
