package com.tradeview.stock.api;

import com.tradeview.stock.config.Constants;
import com.tradeview.stock.model.StockData;
import com.tradeview.stock.util.ConnectionUtil;

import java.util.Date;

public class SinaApi {

    private static String url = "http://hq.sinajs.cn/list=gb_";

    public static StockData getLiveStockData(String symbol) {
        String fullUrl = url + symbol.toLowerCase();
        String result = ConnectionUtil.getInstance().get(fullUrl, "GBK");
        System.out.print("*");
        if (result != null && result.indexOf("\"") != -1) {
            result = result.substring(result.indexOf("\"") + 1, result.length() - 2);
            String[] arr = result.split(",");
            if (arr.length == 34) {
                StockData stockData = new StockData();
                stockData.setDate(Constants.SDF2.format(new Date()));
                stockData.setLclose(Double.parseDouble(arr[26]));
                stockData.setTopen(Double.parseDouble(arr[5]));
                stockData.setTclose(Double.parseDouble(arr[1]));
                stockData.setThigh(Double.parseDouble(arr[6]));
                stockData.setTlow(Double.parseDouble(arr[7]));
                stockData.setVolume(Integer.parseInt(arr[19]));
                return stockData;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        StockData stockData = getLiveStockData("lnc");
        System.out.println(stockData.getThigh());
    }
}
