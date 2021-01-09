package com.tradeview.stock.calc;


import com.tradeview.stock.config.Constants;
import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.StockData;

import java.util.List;

public class HighVolBreakCalculator implements Calculator {
	
	private StockData todayStock;
	private List<StockData> chartStocks;
	
	public HighVolBreakCalculator(StockData todayStock, List<StockData> chartStocks) {
		this.todayStock = todayStock;
		this.chartStocks = chartStocks;
	}
	
	public boolean match() {
		return
				// 今日收盘价大于60日内的最高点（但不超过2%），并且红K（超过4%），并且上影线很小
				matchTodayK() &&
				// 60日内（除了今日）的最大量那天是平均量的2倍以上，并且那天的最高点距离今日不超过5%
				matchVol();
	}
	
	private boolean matchTodayK() {
		// 今日收盘价
		double tclose = todayStock.getTclose();
		
		// 60日内的最高价（除了今日）
		double topPrice = 0;
		for(int i = 0; i < chartStocks.size(); i++) {
			StockData stockData = chartStocks.get(i);
			double high = stockData.gettHigh();
			
			if(high > topPrice) {
				topPrice = high;
			}
		}
		
		// 今日开盘价，今日红K
		double topen = todayStock.getTopen();
		
		// 今日最高价，今日上影线很小
		double thigh = todayStock.gettHigh();
		
		return (tclose > topPrice && calcRate(tclose, topPrice) < Param.EXCEED_RATE)
				&& (tclose > topen)
				&& (calcHighShallowRate(topen, thigh, tclose) < Param.TODAY_HIGH_SHADOW_REATE)
				&& (calcRedKRate(topen, tclose) > Param.TODAY_RED_K_RATE);
	}
	
	private double calcRate(double higher, double lower) {
		return (higher - lower)/lower;
	}

	private double calcRedKRate(double tOpen, double tClose) {
		return (tClose-tOpen)/tOpen;
	}

	private double calcHighShallowRate(double topen, double thigh, double tclose) {
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
	
	private boolean matchVol() {
		// 找出最高点
		int topVol = 0;
		double highOfTopVol = 0;
		
		// 计算出60日平均量
		int avgVol = 0;

		int totalVol = 0;
		double totalAmount = 0;
		String topVolDate = null;
		for(int i = 0; i < chartStocks.size(); i++) {
			StockData stockData = chartStocks.get(i);
			double thigh = stockData.gettHigh();
			double tclose = stockData.getTclose();
			int vol = stockData.getVolume();
			String date = stockData.getDate();
			
			if(vol > topVol) {
				topVol = vol;
				highOfTopVol = thigh;
				topVolDate = date;
			}
			
			totalVol += vol;
			totalAmount += (tclose*vol);
		}

		double avgAmount = totalAmount/chartStocks.size();
		if (!Constants.only_read_local && avgAmount < Param.AVG_AMOUNT_VOL_TOLERANCE) {
			throw new RuntimeException("small amount vol stock");
		}
		
		avgVol = totalVol/chartStocks.size();

		return topVol > avgVol*Param.HUGE_VOL_RATE &&
				(calcRate(todayStock.getTclose(), highOfTopVol) < Param.HUGE_VOL_PRICE_TO_TODAY_GAP_RATE);
	}

}
