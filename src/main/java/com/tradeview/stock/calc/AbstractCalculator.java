package com.tradeview.stock.calc;

import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.StockData;
import com.tradeview.stock.model.StockPoint;

import java.util.List;

public abstract class AbstractCalculator implements Calculator {

    protected StockData todayStock;
    protected List<StockData> chartStocks;

    public AbstractCalculator(StockData todayStock, List<StockData> chartStocks) {
        this.todayStock = todayStock;
        this.chartStocks = chartStocks;
    }

    /**
     * 计算2个位置的涨幅
     * @param higher
     * @param lower
     * @return
     */
    protected double calcRate(double higher, double lower) {
        if (higher > lower) {
            return (higher - lower)/lower;
        } else {
            return (lower - higher)/higher;
        }
    }

    /**
     * 计算红K的涨幅
     * @param tOpen
     * @param tClose
     * @return
     */
    protected double calcRedKRate(double tOpen, double tClose) {
        return (tClose-tOpen)/tOpen;
    }

    /**
     * 计算上影线占据上半身的比例
     * @param topen
     * @param thigh
     * @param tclose
     * @return
     */
    protected double calcHighShallowRate(double topen, double thigh, double tclose) {
        double t1;
        double t2;
        if(tclose > topen) {
            t1 = thigh - tclose;
            t2 = tclose - topen;
        } else {
            t1 = thigh - topen;
            t2 = topen - tclose;
        }
        return t1/(t1+t2);
    }

    /**
     * 计算均线
     * @param ma
     * @param index
     * @return
     */
    protected double calcMa(int ma, int index) {
        if (chartStocks != null && chartStocks.size() > (ma + index)) {
            double total = 0;
            for (int i = 0; i < ma; i++) {
                int k = index + i;
                if (k < chartStocks.size()) {
                    total += chartStocks.get(k).getTclose();
                }
            }
            return total / ma;
        } else {
            return 0;
        }
    }

    /**
     * 计算今日均线
     * @param ma
     * @param tclose
     * @return
     */
    protected double calcTodayMa(int ma, double tclose) {
        int index = 0;
        return (calcMa((ma-1), index)*(ma-1)+tclose)/ma;
    }

    /**
     * 计算3个脚成一线的第三个脚的价格
     * @param f1
     * @param f2
     * @param period1 f1 ~ f2
     * @param period2 f2 ~ f3
     * @return
     */
    protected double calc3rdFootPrice(double f1, double f2, int period1, int period2) {
        double p1 = period1;
        double p2 = period2;
        double x = f2 - f1;
        return ((p1 + p2) * x) / p1 + f1;
    }

    /**
     * 计算2个值之间的差距比例（小于等于1）
     * @param p1
     * @param p2
     * @return
     */
    protected double calcGapPercentage(double p1, double p2) {
        if (p1 < p2) {
            return p1 / p2;
        } else {
            return p2 / p1;
        }
    }

    /**
     * 计算是否连成一线
     * @param stockPoints
     * @return
     */
    protected Boolean isInOneLine(List<StockPoint> stockPoints) {
        if (stockPoints == null || stockPoints.size() < 3) {
            return null;
        }
        if ((stockPoints.get(1).getIndex() - stockPoints.get(0).getIndex()) <= 2) {
            return false;
        }
        if ((stockPoints.get(2).getIndex() - stockPoints.get(1).getIndex()) <= 2) {
            return false;
        }
        StockPoint zeroPoint = stockPoints.get(0);
        double zeroPrice = zeroPoint.getPrice();
        int zeroIndex = zeroPoint.getIndex();

        StockPoint farPoint = stockPoints.get(stockPoints.size() - 1);
        double farPrice = farPoint.getPrice();
        int farIndex = farPoint.getIndex();

        double ratio = (farPrice - zeroPrice) / (farIndex - zeroIndex);

        for (int i = 1; i < stockPoints.size() - 1; i++) {
            StockPoint stockPoint = stockPoints.get(i);
            double price = stockPoint.getPrice();
            int index = stockPoint.getIndex();

            if (price <= zeroPrice) {
                // 价格小于等于0轴价格，肯定不会匹配
                return false;
            } else {
                // 如果在连线上方，则不需要继续匹配，返回null
                double thisRatio = (price - zeroPrice) / (index - zeroIndex);
                if (thisRatio > ratio) {
                    return null;
                } else if (calcGapPercentage(thisRatio, ratio) < Param.THREE_FOOTER_MAX_GAP_RATE) {
                    // 匹配失败
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 计算指定范围内的高点
     * @param from
     * @param to
     * @return
     */
    protected StockPoint findHighByIndexRange(int from, int to) {
        double high = 0;
        int index = 0;
        for(int i = from; i < to; i++) {
            StockData stockData = chartStocks.get(i);
            double thigh = stockData.getThigh();

            if (thigh > high) {
                high = thigh;
                index = i;
            }
        }
        return new StockPoint(high, index);
    }

    /**
     * 计算指定范围内的低点
     * @param from
     * @param to
     * @return
     */
    protected StockPoint findLowByIndexRange(int from, int to) {
        double low = 999999;
        int index = 0;
        for(int i = from; i < to; i++) {
            StockData stockData = chartStocks.get(i);
            double tlow = stockData.getTlow();

            if (tlow < low) {
                low = tlow;
                index = i;
            }
        }
        return new StockPoint(low, index);
    }

}
