package com.tradeview.stock.calc;

import com.tradeview.stock.model.StockData;
import com.tradeview.stock.model.StockResult;

import java.util.List;

public class HighRiskDayTradeReviewCalculator extends AbstractCalculator {

    public HighRiskDayTradeReviewCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match(StockResult stockResult) {

		double tclose = todayStock.getTclose();
		double topen = todayStock.getTopen();
		double high = todayStock.getThigh();
		double low = todayStock.getTlow();
		int tvol = todayStock.getVolume();

		double yclose = chartStocks.get(0).getTclose();
		double yopen = chartStocks.get(0).getTopen();
		double yhigh = chartStocks.get(0).getThigh();
		double ylow = chartStocks.get(0).getTlow();
		int yvol = chartStocks.get(0).getVolume();

		double bclose = chartStocks.get(1).getTclose();

		double ywave = (yhigh - ylow) / ylow;
		double topenWave = (topen - yclose) / yclose;

		// 今日开盘标准
		// 开盘大于在20%以上
		boolean result = (topen > yclose) && (topenWave > 0.2) &&
				// 价格大于5
				(tclose > 5) &&
				// 成交额在100万以上
				(topen * tvol > (100*10000));
		if (result) {
			stockResult.setSort(topenWave);
			stockResult.setPeriod("[今日开盘："+(int)(topenWave*100)+"%]");
			return true;
		}

		// 昨日振福标准
		 result = (yopen > bclose) && (yclose > bclose) &&
				// 开盘高开5%以上
				(calcRedKRate(bclose, yopen) > 0.05f) &&
				// 价格大于5
				(yclose > 5) &&
				// 成交额在100万以上
				(yopen * yvol > (100*10000)) &&
				// 振福在40%以上
				(ywave >= 0.4);
		if (result) {
			stockResult.setSort(ywave);
			stockResult.setPeriod("[昨日振福："+(int)(ywave*100)+"%]");
			return true;
		}

		return false;
    }

	@Override
    public String getName() {
        return "高风险收益日内交易";
    }

}
