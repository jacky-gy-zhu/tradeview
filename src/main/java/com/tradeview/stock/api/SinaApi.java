package com.tradeview.stock.api;

import com.tradeview.stock.config.Constants;
import com.tradeview.stock.model.StockData;
import com.tradeview.stock.util.ConnectionUtil;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SinaApi {

    // http://image.sinajs.cn/newchart/v5/usstock/daily/x.gif

    // http://hq.sinajs.cn/list=gb_x

    public static String SYMBOL_PREFIX = "gb_";
    private static String url = "http://hq.sinajs.cn/list=";

    public static StockData getLiveStockData(String symbol) {
        String fullUrl = url + SYMBOL_PREFIX + symbol.toLowerCase();
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

    public static Map<String, JSONObject> getLiveStockDataMap(String symbols) {
        long lastTime = new Date().getTime();
        Map<String,JSONObject> map = new HashMap();
        String fullUrl = url + symbols.toLowerCase();
        String results = ConnectionUtil.getInstance().get(fullUrl, "GBK");
        System.out.print("*");
        if (results != null) {
            String[] array = results.split(";");
            if (array != null && array.length > 0) {
                for (String result : array) {
                    if (result != null && result.indexOf("\"") != -1) {
                        String symbol = result.substring("var hq_str_gb_".length(), result.indexOf("\"")-1);
                        try {
                            if (result != null && result.length() > 30) {
                                result = result.substring(result.indexOf("\"") + 1, result.length() - 2);
                                String[] arr = result.split(",");
                                if (arr.length == 34) {
                                    JSONObject obj = new JSONObject();
                                    String name = arr[0];
                                    obj.put("symbol", symbol);
                                    obj.put("companyName", name);
                                    obj.put("latestTime", String.valueOf(lastTime));
                                    obj.put("previousClose", Double.parseDouble(arr[26]));
                                    obj.put("close", Double.parseDouble(arr[1]));
                                    obj.put("high", Double.parseDouble(arr[6]));
                                    obj.put("low", Double.parseDouble(arr[7]));
                                    obj.put("open", Double.parseDouble(arr[5]));
                                    obj.put("volume", Long.parseLong(arr[19]));
                                    map.put(symbol, obj);
                                }
                            }
                        } catch (Exception e) {
//                            System.err.println(symbol);
                        }
                    }
                }
            }
        }

        return map;
    }

    public static void main(String[] args) {
        StockData stockData = getLiveStockData("lnc");
        System.out.println(stockData.getThigh());
    }
}
