package com.tradeview.stock.calc;


import com.tradeview.stock.config.Constants;
import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.StockData;

import java.util.List;

public class HighVolBreakCalculator extends AbstractCalculator {

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
				matchVol() &&
				// 均线多头向上排列，粘合，收盘价站上MA5
				matchMa();
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
	
	private boolean matchVol() {
		// 找出最高点
		int topVol = 0;
		double highOfTopVol = 0;
		
		// 计算出60日平均量
		int avgVol = 0;

		int totalVol = 0;
		double totalAmount = 0;
//		String topVolDate = null;
		for(int i = 0; i < chartStocks.size(); i++) {
			StockData stockData = chartStocks.get(i);
			double thigh = stockData.gettHigh();
			double tclose = stockData.getTclose();
			int vol = stockData.getVolume();
//			String date = stockData.getDate();
			
			if(vol > topVol) {
				topVol = vol;
				highOfTopVol = thigh;
//				topVolDate = date;
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

	private boolean matchMa() {
		// 今日
		double tclose = todayStock.getTclose();
		int index = 0;
		double ma5 = (calcMa(4, chartStocks, index)*4+tclose)/5;
		double ma10 = (calcMa(9, chartStocks, index)*9+tclose)/10;
		double ma20 = (calcMa(19, chartStocks, index)*19+tclose)/20;
		double ma30 = (calcMa(29, chartStocks, index)*29+tclose)/30;

		// 昨日
		double _ma5 = calcMa(5, chartStocks, index);
		double _ma10 = calcMa(10, chartStocks, index);
		double _ma20 = calcMa(20, chartStocks, index);
		double _ma30 = calcMa(30, chartStocks, index);

		return tclose > ma5 &&
				ma5 > ma10 &&
				ma10 > ma20 &&
				ma20 > ma30 &&
//				ma5 >= _ma5 &&
				ma10 >= _ma10 &&
				ma20 >= _ma20 &&
				ma30 >= _ma30 &&
				calcRate(ma5, ma30) < Param.MA5_MA30_GAP_RATE &&
				calcRate(ma5, ma20) < Param.MA5_MA20_GAP_RATE &&
				calcRate(ma5, ma10) < Param.MA5_MA10_GAP_RATE;
	}

	@Override
	public String getName() {
		return "盘整突破";
	}
}
