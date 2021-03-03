package com.tradeview.stock.calc;

import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.StockData;
import com.tradeview.stock.model.StockPoint;
import com.tradeview.stock.model.StockResult;

import java.util.List;

public class HeaderFooterLowerCalculator extends AbstractCalculator {

    public HeaderFooterLowerCalculator(StockData todayStock, List<StockData> chartStocks) {
        super(todayStock, chartStocks);
    }

    @Override
    public boolean match(StockResult stockResult) {
        return
                // 今日黑K身体大于4%，收盘小于昨日低点，今日收盘小于MA5，今日开盘大于MA5，今日收盘小于MA20
                matchTodayK() &&
                // 最近7日均线向下
                matchMA() &&
                // 最近10日的高点和低点大于20%，最近4日的低点就是最近10日的低点，
                // (最近4日的高点-最近10日的低点)是(最近4日的高点-最近10日的低点)的1/3以内
                matchPercentage(stockResult);
    }

    private boolean matchTodayK() {
        StockData ytd = chartStocks.get(0);
        double ytdLow = ytd.getTlow();
        double tclose = todayStock.getTclose();
        double topen = todayStock.getTopen();
        double ma5 = calcTodayMa(5, tclose);
        double ma20 = calcTodayMa(20, tclose);

        return (tclose < topen) &&
                (calcRate(topen, tclose) > Param.SHORT_TODAY_BODY_MIN) &&
                (tclose < ytdLow) &&
                (tclose < ma5) &&
//                (topen > ma5) &&
                (tclose < ma20);
    }

    private boolean matchMA() {
        double m5_0 = calcMa(5, 0);
        double m5_1 = calcMa(5, 1);
        double m5_2 = calcMa(5, 2);
        double m5_3 = calcMa(5, 3);
        double m5_4 = calcMa(5, 4);
        double m5_5 = calcMa(5, 5);

        return (m5_0 < m5_1) &&
                (m5_1 < m5_2) &&
                (m5_2 < m5_3) &&
                (m5_3 < m5_4) &&
                (m5_4 < m5_5);
    }

    private boolean matchPercentage(StockResult stockResult) {
        StockPoint header = findHighByIndexRange(0, 10);
        StockPoint header4 = findHighByIndexRange(0, 4);
        StockPoint footer = findLowByIndexRange(0, 10);
        StockPoint footer4 = findLowByIndexRange(0, 4);
        double high = header.getPrice();
        double high4 = header4.getPrice();
        double low = footer.getPrice();
        double low4 = footer4.getPrice();
        double duration = high - low;
        double duration4 = high4 - low4;

        if (footer.getIndex() == 0 || footer4.getIndex() == 0) {
            return false;
        }
        return (calcRate(high, low) > Param.SHORT_WAVE_MIN) &&
                (low == low4) &&
                calcGapPercentage(duration, duration4) < Param.SHORT_TIMES;
    }

    @Override
    public String getName() {
        return "空头趋势向下";
    }
}
