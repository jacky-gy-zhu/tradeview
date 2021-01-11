package com.tradeview.stock.calc;

import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.StockData;

import java.util.List;

public class BackToRaiseCalculator extends AbstractCalculator {

    public BackToRaiseCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match() {
        return
				// 均线多头向上排列，粘合，收盘价站上MA5
				matchMa() &&
				// 股价和MA5形成N字型向上
				matchNUpForm();
    }

	private boolean matchNUpForm() {
    	int step = 0;
    	double x = 0;
    	double y = 0;
    	double z = 0;
		double highPrice = 0;
		double lowPrice = 9999999;
		double highMa = 0;
		double lowMa = 9999999;
		int highIndex = 0;
		int lowIndex = 0;
		for(int i = 0; i < chartStocks.size(); i++) {
			StockData stockData = chartStocks.get(i);
			double thigh = stockData.getThigh();
			double tlow = stockData.getTlow();
			double ma = calcMa(5, i);

			if (i > 2) {
				if (step == 0) {
					if (calcRate(highPrice, lowPrice) > Param.N_BACK_HIGH_LOW_MIN_RATE &&
							calcRate(highMa, lowMa) > Param.N_BACK_MA5_MIN_RATE) {
						step = 1;
						if (highIndex < lowIndex) {
							// 当前走势已经在相对高位
							return false;
						}
					}
				}
				if (step == 1) {
					if (ma < lowMa) {
						step = 2;
						y = highPrice;
						z = lowPrice;
					}
				}
				if (step == 2) {
					if (ma > calcMa(5, i - 1)) {
						step = 3;
						x = lowPrice;
					}
				}
				if (step == 3) {
					double rate = (y - z) / (y - x);
					return rate >= Param.N_BACK_BODY_MIN_RATE && rate <= Param.N_BACK_BODY_MAX_RATE;
				}
			}

			if (thigh > highPrice) {
				highPrice = thigh;
			}
			if (tlow < lowPrice) {
				lowPrice = tlow;
			}
			if (ma > highMa) {
				highIndex = i;
				highMa = ma;
			}
			if (ma < lowMa) {
				lowIndex = i;
				lowMa = ma;
			}
		}
		return false;
	}

	protected boolean matchMa() {
		// 今日
		double tclose = todayStock.getTclose();
		double ma5 = calcTodayMa(5, tclose);
		double ma10 = calcTodayMa(10, tclose);
		double ma20 = calcTodayMa(20, tclose);
		double ma30 = calcTodayMa(30, tclose);

		return tclose > ma5 &&
				calcRate(ma5, ma30) < Param.MA5_MA30_GAP_RATE &&
				calcRate(ma5, ma20) < Param.MA5_MA20_GAP_RATE &&
				calcRate(ma5, ma10) < Param.MA5_MA10_GAP_RATE;
	}

	@Override
    public String getName() {
        return "回后起涨";
    }
}
