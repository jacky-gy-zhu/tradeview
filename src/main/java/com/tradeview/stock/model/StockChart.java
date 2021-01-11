package com.tradeview.stock.model;

import com.tradeview.stock.config.Constants;
import com.tradeview.stock.config.Param;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StockChart {

    private String symbol;
    private String companyName;
    private String latestDate;

    private StockData stockData;
    private List<StockData> chartStocks;

    public StockChart(String symbol, JSONArray chart) {
        this.symbol = symbol;
        this.companyName = "";
        // chart
        this.chartStocks = new ArrayList();
        if (chart.length() > 0) {
//            JSONObject latestObj = chart.getJSONObject(0);
//            String date = latestObj.getString("date");
            int firstIndex = 1;
            firstIndex += Param.T_PLUS;
            int index = 0;
            for (int i = firstIndex; i < chart.length(); i++) {
                if (index > (Param.MONTH_PERIOD * 22 + 30)) {
                    break;
                }
                JSONObject obj = chart.getJSONObject(i);
                StockData stockData = convertStockDataFromJsonObject(obj);
                stockData.setDate(obj.getString("date"));

                this.chartStocks.add(stockData);
                index++;
            }
            if (Param.T_PLUS > 0) {
                JSONObject quoteFormer = chart.getJSONObject(firstIndex - 1);
                this.stockData = convertStockDataFromJsonObject(quoteFormer);
                this.latestDate = quoteFormer.getString("date");
                try {
                    JSONObject quoteFormer2 = chart.getJSONObject(firstIndex - 2);
                    this.stockData.setLclose(quoteFormer2.getDouble("close"));
                } catch (Exception e) {
                    this.stockData.setLclose(quoteFormer.getDouble("close"));
                }
            } else {
                JSONObject quoteFormer = chart.getJSONObject(0);
                this.stockData = convertStockDataFromJsonObject(quoteFormer);
                try {
                    JSONObject quoteFormer2 = chart.getJSONObject(1);
                    this.stockData.setLclose(quoteFormer2.getDouble("close"));
                } catch (Exception e) {
                    this.stockData.setLclose(quoteFormer.getDouble("close"));
                }
            }
        }
    }

    public StockChart(JSONObject quote, JSONArray chart) {
        this.symbol = quote.getString("symbol");
        this.companyName = quote.getString("companyName");
        try {
            Date date = Constants.SDF.parse(quote.getString("latestTime"));
            this.latestDate = Constants.SDF2.format(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // quote
        this.stockData = convertStockDataFromJsonObject(quote);
        this.stockData.setLclose(quote.getDouble("previousClose"));

        // chart
        this.chartStocks = new ArrayList();
		if (chart.length() > 0) {
			JSONObject latestObj = chart.getJSONObject(0);
			String date = latestObj.getString("date");
			boolean duplicateLatestDate = date.equals(latestDate);
			int firstIndex = 0;
			if (duplicateLatestDate) {
				firstIndex = 1;
			}
			firstIndex += Param.T_PLUS;
			int index = 0;
			for (int i = firstIndex; i < chart.length(); i++) {
                if (index > (Param.MONTH_PERIOD * 22 + 30)) {
                    break;
                }
				JSONObject obj = chart.getJSONObject(i);
				StockData stockData = convertStockDataFromJsonObject(obj);
				stockData.setDate(obj.getString("date"));
				this.chartStocks.add(stockData);
				index++;
			}
			if (Param.T_PLUS > 0) {
				JSONObject quoteFormer = chart.getJSONObject(firstIndex - 1);
				this.stockData = convertStockDataFromJsonObject(quoteFormer);
				try {
					JSONObject quoteFormer2 = chart.getJSONObject(firstIndex - 2);
					this.stockData.setLclose(quoteFormer2.getDouble("close"));
				} catch (Exception e) {
					this.stockData.setLclose(quoteFormer.getDouble("close"));
				}
			}
		}
    }

    public StockData getStockData() {
        return this.stockData;
    }

    public List<StockData> getChartStocks() {
        return this.chartStocks;
    }

    public String getLatestDate() {
        return latestDate;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    private StockData convertStockDataFromJsonObject(JSONObject obj) {
        StockData stockData = new StockData();
        stockData.setTclose(obj.getDouble("close"));
        stockData.setThigh(obj.getDouble("high"));
        stockData.setTlow(obj.getDouble("low"));
        stockData.setTopen(obj.getDouble("open"));
        stockData.setVolume(obj.getInt("volume"));
        return stockData;
    }

}
