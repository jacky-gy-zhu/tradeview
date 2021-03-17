package com.tradeview.stock.calc;

import com.tradeview.stock.model.StockData;
import com.tradeview.stock.model.StockPoint;
import com.tradeview.stock.model.StockResult;

import java.util.ArrayList;
import java.util.List;

public class BullTrendCalculator extends AbstractCalculator {

    public BullTrendCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    public boolean match(StockResult stockResult) {

        // 1. 找出所有MA5多头拐点
        List<StockPoint> stockPoints = findLowStockPointByMa(5);

		// 2. 以第一个MA5拐点为第一个移动起点，剔除价格大于移动起点的拐点(同时满足平均每20天上涨至少10%)
        stockPoints = filterStockPoints(stockPoints);

        // 3. 以S1，S2为基准，尝试是否可以找到第三个在一条趋势线上的S?
        if (stockPoints.size() > 2) {
            if (succeedToFind3rdPoint(stockPoints.get(0), stockPoints.get(1), stockPoints, 2, stockResult)) {
                return true;
            }
        }

        // 4. 以S1，S3为基准，尝试是否可以找到第三个在一条趋势线上的S?
        if (stockPoints.size() > 3 && isBelowPreviousLine(stockPoints.get(0), stockPoints.get(1), stockPoints.get(2))) {
            if (succeedToFind3rdPoint(stockPoints.get(0), stockPoints.get(2), stockPoints, 3, stockResult)) {
                return true;
            }
        }

        // 5. 以S1，S4为基准，尝试是否可以找到第三个在一条趋势线上的S?
        if (stockPoints.size() > 4 && isBelowPreviousLine(stockPoints.get(0), stockPoints.get(2), stockPoints.get(3))) {
            if (succeedToFind3rdPoint(stockPoints.get(0), stockPoints.get(3), stockPoints, 4, stockResult)) {
                return true;
            }
        }

        // 6. 以S1，S5为基准，尝试是否可以找到第三个在一条趋势线上的S?
        if (stockPoints.size() > 5 && isBelowPreviousLine(stockPoints.get(0), stockPoints.get(3), stockPoints.get(4))) {
            if (succeedToFind3rdPoint(stockPoints.get(0), stockPoints.get(4), stockPoints, 5, stockResult)) {
                return true;
            }
        }

        return false;
    }

    private boolean isBelowPreviousLine(StockPoint s1, StockPoint s2, StockPoint s3) {
        double price3 = s1.getPrice() - (((s3.getIndex()-s1.getIndex())*(s1.getPrice()-s2.getPrice()))/(s2.getIndex()-s1.getIndex()));
        return price3 > s3.getPrice();
    }

    private List<StockPoint> findLowStockPointByMa(int maV) {

        List<StockPoint> list = new ArrayList<>();

        boolean available = true;

        List<StockData> copy = new ArrayList();
        copy.add(todayStock);
        copy.addAll(chartStocks);

        for(int i = 0; i < copy.size()-1; i++) {
            double ma = calcMa(maV, i, copy);
            double _ma = calcMa(maV, i + 1, copy);
            if (ma <= _ma) {
                // 前一个是向上，现在是向下，就是拐点（如果是第一次向下也认为是拐点）
                if (available) {
                    int from = i > maV ? i - maV : i;
                    int to = i + maV;
                    StockPoint lowPoint = findLowByIndexRange(from, to, copy);
                    if (list.stream().map(StockPoint::getIndex)
                            .noneMatch(index -> (index == lowPoint.getIndex()) || (lowPoint.getIndex() - index < 7))) {
                        list.add(lowPoint);
                    }
                    available = false;
                }
            } else {
                // 现在是向上，下一次如果向下就是可以算是拐点
                available = true;
            }
        }

        return list;
    }

    private List<StockPoint> filterStockPoints(List<StockPoint> stockPoints) {

        List<StockPoint> list = new ArrayList<>();

        if (stockPoints != null && stockPoints.size() > 2) {

            StockPoint zeroPoint = stockPoints.get(0);
            list.add(zeroPoint);
            for (int i = 1; i < stockPoints.size(); i++) {
                StockPoint thisPoint = stockPoints.get(i);
                if (isThePercentageGreaterThan(thisPoint, zeroPoint, 20, 12)) {
                    list.add(thisPoint);
                }
            }
        }

        return list;
    }

    private boolean isThePercentageGreaterThan(StockPoint thisPoint, StockPoint zeroPoint, int days, double percentage) {
        if (thisPoint != null && zeroPoint != null) {
            int thisIndex = thisPoint.getIndex();
            double thisPrice = thisPoint.getPrice();
            int zeroIndex = zeroPoint.getIndex();
            double zeroPrice = zeroPoint.getPrice();

            if (zeroPrice > thisPrice) {
                double increase = zeroPrice - thisPrice;
                double duration = thisIndex - zeroIndex;
                double increasePerDay = increase / duration;
                double increase20Day = increasePerDay * days;
                return calcRedKRate(thisPrice, (thisPrice + increase20Day)) > (percentage/100f);
            }

        }
        return false;
    }

    private boolean succeedToFind3rdPoint(StockPoint s1, StockPoint s2, List<StockPoint> stockPoints, int fromIndex, StockResult stockResult) {
        if (stockPoints != null) {
            for (int i = fromIndex; i < stockPoints.size(); i++) {
                StockPoint s3 = stockPoints.get(i);
                double price3 = s1.getPrice() - (((s3.getIndex()-s1.getIndex())*(s1.getPrice()-s2.getPrice()))/(s2.getIndex()-s1.getIndex()));
                if (calcMarginPercentage(price3, s3.getPrice()) < 0.007 && s3.getIndex() < chartStocks.size()-1) {
                    // 之后的拐点不能低于趋势线
                    boolean success = false;
                    if (i < stockPoints.size() - 1) {
                        for (int j = i + 1; j < stockPoints.size(); j++) {
                            StockPoint s4 = stockPoints.get(j);
                            if (isBelowPreviousLine(s1, s2, s4)) {
                                success = false;
                                break;
                            }
                        }
                    } else {
                        success = true;
                    }

                    if (success) {
                        stockResult.setPeriod("["+s1.getIndex()+","+s1.getPrice()+"],["+s2.getIndex()+","+s2.getPrice()+"],["+s3.getIndex()+","+s3.getPrice()+"]");
                        return true;
                    }
                } else if (price3 > s3.getPrice() && calcRedKRate(s3.getPrice(), price3) > 0.007) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "多头趋势线";
    }

}
