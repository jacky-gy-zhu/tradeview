package com.tradeview.stock.calc;

import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.StockData;
import com.tradeview.stock.model.StockResult;
import com.tradeview.stock.model.ThreeFooter;

import java.util.List;

public class HeaderFooterHigherCalculator extends AbstractCalculator {

    public HeaderFooterHigherCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match(StockResult stockResult) {
		ThreeFooter threeFooter = new ThreeFooter();
        return
				// 今日收盘大于MA5，今日红K，今日收盘大于昨日高点，昨日低点在MA5下方
				matchTodayK() &&
				// MA5头头高底底高
				matchMA5HeaderFooterHigher() &&
				// K线（高点和低点）头头高底底高
				matchHighLowHeaderFooterHigher(threeFooter) &&
				// 3个脚点低点可以连成一线
				matchFooterInOneLine(threeFooter);
    }

	private boolean matchTodayK() {
    	double tclose = todayStock.getTclose();
    	double topen = todayStock.getTopen();
    	double yhigh = chartStocks.get(0).getThigh();
    	double ylow = chartStocks.get(0).getTlow();
    	double tMa5 = calcTodayMa(5, tclose);
		double yMa5 = calcMa(5, 0);
		return
				(tclose > tMa5) &&
				(tclose > topen) &&
				(tclose > yhigh) &&
				(ylow < yMa5);
	}

	private boolean matchMA5HeaderFooterHigher() {
		return false;
	}

	private boolean matchHighLowHeaderFooterHigher(ThreeFooter threeFooter) {
		return false;
	}

	private boolean matchFooterInOneLine(ThreeFooter threeFooter) {
    	double f1= threeFooter.getF1();
    	double f2 = threeFooter.getF2();
    	double f3 = threeFooter.getF3();
    	int period1 = threeFooter.getPeriod1();
    	int period2 = threeFooter.getPeriod2();

		double expectingF3 = calc3rdFootPrice(f1, f2, period1, period2);
		return calcRate(expectingF3, f3) < Param.THREE_FOOTER_MAX_GAP_RATE;
	}

	@Override
    public String getName() {
        return "头头高低低高";
    }

}
