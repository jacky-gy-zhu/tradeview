package com.tradeview.stock.calc;

import com.tradeview.stock.model.StockData;
import com.tradeview.stock.model.StockResult;

import java.util.List;

public class DayTraderBearCalculator extends AbstractCalculator {

    public DayTraderBearCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match(StockResult stockResult) {
        boolean result =
				// 今日下跌超过5%，黑K实体线大于5%，成交额在5000万以上
				matchTodayK() &&
				// 收盘价在MA5，MA10，MA20，MA30之下，均线并且向下
				matchMA();
        return result;
    }

	private boolean matchTodayK() {
    	double tclose = todayStock.getTclose();
    	double topen = todayStock.getTopen();
    	double yclose = chartStocks.get(0).getTclose();
		int vol = todayStock.getVolume();

		return
				(tclose < topen) &&
				(calcRedKRate(tclose, topen) > 0.05f) &&
				(calcRedKRate(tclose, yclose) > 0.05f) &&
				(vol*tclose > 5000*10000);
	}

	private boolean matchMA() {
		double tclose = todayStock.getTclose();
		double ma5 = calcTodayMa(5, tclose);
		double ma10 = calcTodayMa(10, tclose);
		double ma20 = calcTodayMa(20, tclose);
		double ma30 = calcTodayMa(30, tclose);
		double _ma5 = calcMa(5, 0);
		double _ma10 = calcMa(10, 0);

		return (tclose < ma5) &&
				(tclose < ma10) &&
				(tclose < ma20) &&
				(tclose < ma30) &&
				(ma5 < _ma5) &&
				(ma10 < _ma10);
	}

	@Override
    public String getName() {
        return "空头当日冲";
    }

}
