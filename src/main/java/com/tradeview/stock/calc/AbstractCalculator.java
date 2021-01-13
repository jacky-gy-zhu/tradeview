package com.tradeview.stock.calc;

import com.tradeview.stock.model.StockData;

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

}
