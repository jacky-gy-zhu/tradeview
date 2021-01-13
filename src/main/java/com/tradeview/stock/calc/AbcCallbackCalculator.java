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
				// 最高点的右侧最低点大于最高点的左侧最低点
				matchRightHigher() &&
				// 高点连成一线（大于3个）
				matchHighLine(stockResult);
    }

	private boolean matchTodayK() {
		return todayStock.getThigh() > chartStocks.get(0).getThigh();
	}

	private boolean matchRightHigher() {
		StockPoint header = findHighByIndexRange(0, chartStocks.size() - 1);
		int headerIndex = header.getIndex();
		StockPoint leftPoint = findLowByIndexRange(headerIndex, chartStocks.size() - 1);
		StockPoint rightPoint = findLowByIndexRange(0, headerIndex);
		double high = header.getPrice();
		double leftLow = leftPoint.getPrice();
		double rightLow = rightPoint.getPrice();

		return
				(leftLow < rightLow) &&
				((high-rightLow)/(high-leftLow) <= Param.GOLD_CUT_RATE);
	}

	private boolean matchHighLine(StockResult stockResult) {
		int count = 0;
		int farRangeIndex = chartStocks.size() - 1;
		do {
			StockPoint header = findHighByIndexRange(0, farRangeIndex);
			int headerIndex = header.getIndex();
			StockPoint zeroPoint = new StockPoint(chartStocks.get(0).getThigh(), 0);

			if (calcRate(header.getPrice(), chartStocks.get(0).getThigh()) < Param.ABC_CALLBACK_MIN_HIGH_LOW_RATE) {
				return false;
			}

			boolean matchAtLeastOne = false;
			if (headerIndex <= Param.ABC_CALLBACK_MAX_DAY_RANGE && headerIndex > Param.ABC_CALLBACK_MIN_GAP) {
				for (int i = 1; i < headerIndex - Param.ABC_CALLBACK_MIN_GAP; i++) {
					StockData stockData = chartStocks.get(i);
					List<StockPoint> stockPoints = new ArrayList<>();
					stockPoints.add(zeroPoint);
					stockPoints.add(new StockPoint(stockData.getThigh(), i));
					stockPoints.add(header);
					if (isInOneLine(stockPoints) == null) {
						matchAtLeastOne = false;
						break;
					} else if (isInOneLine(stockPoints)) {
						StringBuilder sb = new StringBuilder();
						sb.append(stockPoints.get(0).getPrice() + "[" + stockPoints.get(0).getIndex() + "]");
						sb.append(" ~ ");
						sb.append(stockPoints.get(1).getPrice() + "[" + stockPoints.get(1).getIndex() + "]");
						sb.append(" ~ ");
						sb.append(stockPoints.get(2).getPrice() + "[" + stockPoints.get(2).getIndex() + "]");
						stockResult.setPeriod(sb.toString());
						matchAtLeastOne = true;
					}
				}
			}

			if (matchAtLeastOne) {
				return true;
			}

			farRangeIndex = headerIndex;
			count++;
		} while (count < 5);

		return false;
	}

	@Override
    public String getName() {
        return "ABC修正";
    }
}
