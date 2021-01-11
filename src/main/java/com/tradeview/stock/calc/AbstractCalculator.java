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
        return (higher - lower)/lower;
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
     * @param stockList
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


}
