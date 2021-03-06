package com.tradeview.stock.service;

import com.tradeview.stock.api.Iexapis;
import com.tradeview.stock.api.SinaApi;
import com.tradeview.stock.calc.*;
import com.tradeview.stock.config.Constants;
import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.ResultReport;
import com.tradeview.stock.model.StockChart;
import com.tradeview.stock.model.StockResult;
import com.tradeview.stock.util.ConnectionUtil;
import com.tradeview.stock.util.StreamUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class Iextrading {
	
	public static int batch = 50;
	
	private Iextrading(){
	}
	
	private static Iextrading instance = null;
	
	private static synchronized void syncInit() {  
        if (instance == null) {  
            instance = new Iextrading();  
        }  
    }
	
	public static Iextrading getInstance() {  
        if (instance == null) {  
            syncInit();  
        }  
        return instance;  
    }

	public ResultReport selectStocks(String[] codeArr, List<String> excludeCodeList) {
		boolean readLatestStockFromSina = Constants.allow_override_json_data == false && Constants.only_read_local == false;
		ResultReport resultReport = new ResultReport();
		Map<String, List<StockResult>> resultMap = new HashMap<>();
		resultReport.setResultMap(resultMap);
		if(codeArr != null) {
			List<String> urlList = getAvailableUrlList(codeArr, excludeCodeList);
			int count = 0;
			System.out.println();
			for(String symbolGroupStr : urlList) {
				System.out.print(".");
				String[] symbols = symbolGroupStr.split(",");
				int last = calcLastFromToday(symbolGroupStr);
				String iextapisUrl = Iexapis.getUrlForDailyK(symbolGroupStr, Param.MONTH_PERIOD, last);
				if(iextapisUrl != null) {
					JSONObject contentJson = null;
					if (!Constants.only_read_local && !readLatestStockFromSina) {
						contentJson = ConnectionUtil.getInstance().getJsonObject(iextapisUrl, "UTF-8");
					}
					Map<String, JSONObject> quoteMap = new HashMap();
					if (readLatestStockFromSina) {
						quoteMap = SinaApi.getLiveStockDataMap(SinaApi.SYMBOL_PREFIX + symbolGroupStr.replace(",", "," + SinaApi.SYMBOL_PREFIX));
					}
					for(String symbol : symbols) {
						if(contentJson != null ) {
							JSONObject symbolObj = contentJson.getJSONObject(symbol.toUpperCase());
							if(symbolObj != null) {
								try {
									if (Constants.allow_override_json_data) {
										JSONArray chart = symbolObj.getJSONArray("chart");
										JSONArray totalChart = addJSONArrayToStore(symbol, chart);
										if(totalChart != null) {
											StockChart stockChart = new StockChart(symbol, totalChart);
											if (stockChart.getStockData() == null) {
												System.out.println("symbol error :" + symbol);
											}
											matchStrategies(resultMap, symbol, stockChart);

											count++;
										}
									} else {
										JSONObject quote = symbolObj.getJSONObject("quote");
										JSONArray chart = symbolObj.getJSONArray("chart");
										JSONArray totalChart = addJSONArrayToStore(symbol, chart);
										if(quote != null && totalChart != null) {
											StockChart stockChart = new StockChart(quote, totalChart);
											if (stockChart.getStockData() == null) {
												System.out.println("symbol error :" + symbol);
											}
											matchStrategies(resultMap, symbol, stockChart);

											count++;
										}
									}
								} catch(Exception e) {
									if (Constants.throw_if_error_and_print_url) {
										throw e;
									} else {
										System.err.println(symbol);
									}
								}

							}
						} else {
							try {
								if (readLatestStockFromSina) {
									// read current
									JSONObject quote = quoteMap.get(symbol);
									JSONArray totalChart = addJSONArrayToStore(symbol, null);
									if(quote != null && totalChart != null) {
										StockChart stockChart = new StockChart(quote, totalChart);
										if (stockChart.getStockData() == null) {
											System.out.println("symbol error :" + symbol);
										}
										matchStrategies(resultMap, symbol, stockChart);

										count++;
									}
								} else {
									// review history
									JSONArray totalChart = addJSONArrayToStore(symbol, null);
									if(totalChart != null) {
										StockChart stockChart = new StockChart(symbol, totalChart);
										if (stockChart.getStockData() == null) {
											System.out.println("symbol error : " + symbol);
											continue;
										}
										matchStrategies(resultMap, symbol, stockChart);

										count++;
									}
								}
							} catch(Exception e) {
								if (Constants.throw_if_error_and_print_url) {
									throw e;
								} else {
									System.err.println(symbol);
								}
							}
						}
					}
				}
			}
			System.out.println();
			System.out.println("总共扫描个股数量："+count);
			System.out.println();
		}

		return resultReport;
	}

	private void matchStrategies(Map<String, List<StockResult>> resultMap, String symbol, StockChart stockChart) {

		if (Constants.is_short) {
			handleResultMap(resultMap, symbol, stockChart, new HeaderFooterLowerCalculator(stockChart.getStockData(), stockChart.getChartStocks()));
		} else {
//			handleResultMap(resultMap, symbol, stockChart, new HighVolBreakCalculator(stockChart.getStockData(), stockChart.getChartStocks()));
//			handleResultMap(resultMap, symbol, stockChart, new BackToRaiseAndBreakTopCalculator(stockChart.getStockData(), stockChart.getChartStocks()));
			handleResultMap(resultMap, symbol, stockChart, new HeaderFooterHigherCalculator(stockChart.getStockData(), stockChart.getChartStocks()));
//			handleResultMap(resultMap, symbol, stockChart, new AbcCallbackCalculator(stockChart.getStockData(), stockChart.getChartStocks()));
//			handleResultMap(resultMap, symbol, stockChart, new VbackCalculator(stockChart.getStockData(), stockChart.getChartStocks()));
		}
	}

	private void handleResultMap(Map<String, List<StockResult>> resultMap, String symbol, StockChart stockChart, Calculator calculator) {
		StockResult stockResult = new StockResult();
		if (calculator.match(stockResult)) {
			stockResult.setDate(stockChart.getLatestDate());
			stockResult.setSymbol(symbol);
			stockResult.setName(stockChart.getCompanyName());

			resultMap.computeIfAbsent(calculator.getName(), o -> new ArrayList<>());
			resultMap.get(calculator.getName()).add(stockResult);
		}
	}

	private int calcLastFromToday(String symbolGroupStr) {
		try {
			if (symbolGroupStr != null && symbolGroupStr.length() > 0) {
				String sampleSymbol = symbolGroupStr.split(",")[0];
				JSONArray localJsonArray = getLocalStorageJSONArray(sampleSymbol);
				if (localJsonArray != null && localJsonArray.length() > 0) {
					JSONObject latestLocalJsonObj = localJsonArray.getJSONObject(0);
					String latestLocalDateStr = latestLocalJsonObj.getString("date");
					Date latestLocalDate = Constants.SDF2.parse(latestLocalDateStr);
					return calcDayGapFromToday(latestLocalDate);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	private int calcDayGapFromToday(Date latestLocalDate) {
		if (latestLocalDate != null) {
			Date nowDate = new Date();
			return daysBetween(latestLocalDate, nowDate);
		} else {
			return 0;
		}
	}

	/**
	 * 前提条件是第二日的下午5点运行程序
	 * 比实际天数要少一天，因为美国和澳洲的时差关系
	 * @param smdate
	 * @param bdate
	 * @return
	 */
	public int daysBetween(Date smdate,Date bdate) {
		DateTime start = new DateTime(smdate).withTimeAtStartOfDay();
		DateTime end = new DateTime(bdate).withTimeAtStartOfDay();
		if (start.isEqual(end)) {
			return 0;
		} else {
			int count = 0;
			do {
				start = start.plusDays(1);
				if (start.getDayOfWeek() != 6 && start.getDayOfWeek() != 7) {
					count++;
				}
			} while (start.isBefore(end));
			if (count > 1) {
				return count-1;
			} else {
				return count;
			}
		}
	}

	private JSONArray addJSONArrayToStore(String symbol, JSONArray chart) {
		if (chart == null) {
			chart = new JSONArray();
		}
		String jsonPath = Constants.stockDataJsonFolder + "/" + symbol.toLowerCase() + ".json";
		JSONArray localJsonArray = getLocalStorageJSONArray(symbol);
		if (localJsonArray != null) {
			List<String> newDateList = new ArrayList<>();
			// loop new json array data
			for (int i = 0; i < chart.length(); i++) {
				JSONObject oneDayObj = chart.getJSONObject(i);
				String date = oneDayObj.getString("date");
				newDateList.add(date);
			}

			// loop local json array data
			for (int i = 0; i < localJsonArray.length(); i++) {
				JSONObject oneDayObj = localJsonArray.getJSONObject(i);
				String date = oneDayObj.getString("date");
				if (!newDateList.contains(date)) {
					chart.put(oneDayObj);
				}
			}
		}
		// save into local storage
		if (!Constants.only_read_local && Constants.allow_override_json_data) {
			StreamUtils.writeFileContent(jsonPath, chart.toString().replace("},", "},\n"));
		}
		return chart;
	}

	private JSONArray getLocalStorageJSONArray(String symbol) {
		String jsonPath = Constants.stockDataJsonFolder + "/" + symbol.toLowerCase() + ".json";
		if (StreamUtils.isFileExist(jsonPath)) {
			String jsonContent = StreamUtils.getFileContent(jsonPath);
			if (jsonContent != null && jsonContent.length() > 0 && jsonContent.startsWith("[")) {
				return new JSONArray(jsonContent);
			}
		}
		return null;
	}

	private List<String> getAvailableUrlList(String[] codeArr, List<String> excludeCodeList) {
		List<String> urlList = new ArrayList();
		Set<String> tmpCodeList = new HashSet();
		for(String code : codeArr) {
			if(code.indexOf("+") == -1 && !excludeCodeList.contains(code)) {
				if(tmpCodeList.size() < batch) {
					tmpCodeList.add(code);
				}else {
					urlList.add(String.join(",", tmpCodeList));
					tmpCodeList.clear();
					tmpCodeList.add(code);
				}
			}
		}
		if(tmpCodeList.size() > 0) {
			urlList.add(String.join(",", tmpCodeList));
		}
		return urlList;
	}

}
