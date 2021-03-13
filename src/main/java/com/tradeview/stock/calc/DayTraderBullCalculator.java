package com.tradeview.stock.calc;

import com.tradeview.stock.model.StockData;
import com.tradeview.stock.model.StockResult;

import java.util.List;

public class DayTraderBullCalculator extends AbstractCalculator {

    public DayTraderBullCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match(StockResult stockResult) {
        boolean result =
				// 昨日黑K收跌，实体大于5%；今日红K成母子怀抱，昨日或今日至少有一日爆量
				matchTodayK();
        return result;
    }

	private boolean matchTodayK() {
		StockData dbyStock = chartStocks.get(1);
		StockData ystStock = chartStocks.get(0);
		double tclose = todayStock.getTclose();
		double topen = todayStock.getTopen();
		double thigh = todayStock.getThigh();
		double tlow = todayStock.getTlow();
		double yclose = ystStock.getTclose();
		double yopen = ystStock.getTopen();
		double dclose = dbyStock.getTclose();
		int tvol = todayStock.getVolume();
		int yvol = ystStock.getVolume();
		int avgVol5 = calcAvgVol(0, 5);

		return
				(yclose < dclose) &&
				(yclose < yopen) &&
				(calcRedKRate(yclose, yopen) > 0.05f) &&
				(tclose > topen) &&
				(thigh < yopen) &&
				(tlow > yclose) &&
				((tvol > avgVol5*2) || (yvol > avgVol5*2));
	}

	@Override
    public String getName() {
        return "多头当冲";
    }

}
