package com.tradeview.stock.calc;

import com.tradeview.stock.model.StockData;
import com.tradeview.stock.model.StockPoint;
import com.tradeview.stock.model.StockResult;

import java.util.List;

public class DayTraderBearCalculator extends AbstractCalculator {

    public DayTraderBearCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

	public boolean match(StockResult stockResult) {
		boolean result =
				// 昨日红K收涨，实体大于5%；今日黑K成母子怀抱，昨日或今日至少有一日爆量
				matchTodayK() ||
				// 今日高点是60日高点，今日收黑，今日收跌，今日上影线大于3%并且是实体线的5倍以上，成交量大于5日均量
				matchTodayK2();
		return result;
	}

	private boolean matchTodayK2() {
		StockData ystStock = chartStocks.get(0);
		double tclose = todayStock.getTclose();
		double topen = todayStock.getTopen();
		double thigh = todayStock.getThigh();
		double yclose = ystStock.getTclose();
		int tvol = todayStock.getVolume();
		int avgVol5 = calcAvgVol(0, 5);
		StockPoint highStockPoint = findHighByIndexRange(0, chartStocks.size()<60?chartStocks.size():60);
		double max = highStockPoint.getPrice();

		return (thigh > max) &&
				(tclose < topen) &&
				(tclose < yclose) &&
				(calcHighShallowRate(topen, thigh, tclose) > 0.8f) &&
				(calcRedKRate(topen, thigh) > 0.03f) &&
				(tvol > avgVol5);

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
				(yclose > dclose) &&
				(yopen < yclose) &&
				(calcRedKRate(yopen, yclose) > 0.05f) &&
				(topen > tclose) &&
				(tlow > yopen) &&
				(thigh < yclose) &&
				((tvol > avgVol5*2) || (yvol > avgVol5*2));
	}
	@Override
    public String getName() {
        return "空头当冲";
    }

}
