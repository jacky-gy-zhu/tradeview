package com.tradeview.stock.calc;

import com.tradeview.stock.model.StockData;

import java.util.List;

public abstract class AbstractCalculator implements Calculator {

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
    protected double calcMa(int ma, List<StockData> stockList, int index) {
        if (stockList != null && stockList.size() > (ma + index)) {
            double total = 0;
            for (int i = 0; i < ma; i++) {
                int k = index + i;
                if (k < stockList.size()) {
                    total += stockList.get(k).getTclose();
                }
            }
            return total / ma;
        } else {
            return 0;
        }
    }


}
