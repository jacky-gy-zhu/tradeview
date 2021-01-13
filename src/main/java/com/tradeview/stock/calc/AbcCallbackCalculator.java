package com.tradeview.stock.calc;

import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.StockData;
import com.tradeview.stock.model.StockPoint;
import com.tradeview.stock.model.StockResult;

import java.util.ArrayList;
import java.util.List;

public class AbcCallbackCalculator extends AbstractCalculator {

    public AbcCallbackCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match(StockResult stockResult) {
        return
				// 今日最高点大于昨日最高点
				matchTodayK() &&
				// 高点连成一线（大于3个）
				matchHighLine();
    }

	private boolean matchHighLine() {
		StockPoint header = findHighByIndexRange(0, chartStocks.size() - 1);
		int headerIndex = header.getIndex();
		StockPoint zeroPoint = new StockPoint(chartStocks.get(0).getThigh(), 0);

		if (headerIndex <= Param.ABC_CALLBACK_MAX_DAY_RANGE && headerIndex > Param.ABC_CALLBACK_MIN_GAP) {
			for (int i = Param.ABC_CALLBACK_MIN_GAP; i < headerIndex - Param.ABC_CALLBACK_MIN_GAP; i++) {
				StockData stockData = chartStocks.get(i);
				List<StockPoint> stockPoints = new ArrayList<>();
				stockPoints.add(zeroPoint);
				stockPoints.add(new StockPoint(stockData.getThigh(), i));
				stockPoints.add(header);
				if (isInOneLine(stockPoints)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean matchTodayK() {
		return todayStock.getThigh() > chartStocks.get(0).getThigh();
	}


	@Override
    public String getName() {
        return "ABC修正";
    }
}