package com.tradeview.stock.calc;

import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.StockData;
import com.tradeview.stock.model.StockResult;

import java.util.List;

public class VbackCalculator extends AbstractCalculator {

    public VbackCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match(StockResult stockResult) {
        return
				// 今日收盘大于昨日最高点
				matchTodayK() &&
				// 最近10日内跌幅超过20%
				matchLeftBear() &&
				// 最近10日内至少有3天的跌幅大于5%
				match3DaysBigDown() &&
				// 最近3日内有一日是大量
				matchBigVolIn3Days() &&
				// 均线空头排列
				matchMAShort() &&
				// 最近10日MA5向下
				matchMA5DropIn10Days();
    }

	private boolean matchMA5DropIn10Days() {
    	double previousMa5 = 0;
		for(int i = 0; i < 10; i++) {
			double ma5 = calcMa(5, i);
			if (previousMa5 > ma5) {
				return false;
			}
			previousMa5 = ma5;
		}
		return true;
	}

	private boolean matchMAShort() {
		// 昨日
		int index = 0;
		double ma5 = calcMa(5, index);
		double ma10 = calcMa(10, index);
		double ma20 = calcMa(20, index);
		double ma30 = calcMa(30, index);

		return (ma5 < ma10) && (ma10 < ma20) && (ma20 < ma30);
	}

	private boolean matchBigVolIn3Days() {

    	int highVol = 0;
		for(int i = 0; i < 3; i++) {
			StockData stockData = chartStocks.get(i);
			int vol = stockData.getVolume();
			if (vol > highVol) {
				highVol = vol;
			}
		}

		int totalVol = 0;
		for(int i = 0; i < 40; i++) {
			StockData stockData = chartStocks.get(i);
			int vol = stockData.getVolume();
			totalVol += vol;
		}
		int avgVol = totalVol/40;

		return highVol > avgVol*Param.HUGE_VOL_RATE;
	}

	private boolean match3DaysBigDown() {
    	int count = 0;
		for(int i = 0; i < 10; i++) {
			StockData stockData = chartStocks.get(i);
			StockData yStockData = chartStocks.get(i+1);
			double tclose = stockData.getTclose();
			double yclose = yStockData.getTclose();
			if (tclose < yclose && calcRate(yclose, tclose) > Param.BEAR_DROP_BIG_DROP_MIN) {
				count++;
			}
		}
		return count >= Param.BEAR_DROP_BIG_DROP_DAYS;
	}

	private boolean matchLeftBear() {
		double lower = chartStocks.get(0).getTlow();
		double highPrice = 0;
		for(int i = 0; i < 10; i++) {
			StockData stockData = chartStocks.get(i);
			double thigh = stockData.getThigh();
			if (thigh > highPrice) {
				highPrice = thigh;
			}
		}
		return calcRate(highPrice, lower) > Param.BEAR_DROP_MIN;
	}

	private boolean matchTodayK() {
		double tclose = todayStock.getTclose();

		double ma5 = calcTodayMa(5, tclose);

		return (tclose > chartStocks.get(0).getThigh()) && (tclose > ma5);
	}


	@Override
    public String getName() {
        return "V型反弹";
    }
}
