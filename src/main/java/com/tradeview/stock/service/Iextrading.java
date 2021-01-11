package com.tradeview.stock.service;

import com.tradeview.stock.api.Iexapis;
import com.tradeview.stock.calc.Calculator;
import com.tradeview.stock.calc.HighVolBreakCalculator;
import com.tradeview.stock.config.Constants;
import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.ResultReport;
import com.tradeview.stock.model.StockChart;
import com.tradeview.stock.model.StockResult;
import com.tradeview.stock.util.ConnectionUtil;
import com.tradeview.stock.util.StreamUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.*;

public class Iextrading {
	
	public static int batch = 100;
	
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

	public ResultReport findUSStockForHighVol(String[] codeArr, List<String> excludeCodeList) {

		ResultReport resultReport = new ResultReport();
		Map<String, List<StockResult>> resultMap = new HashMap<>();
		resultReport.setResultMap(resultMap);
		if(codeArr != null) {
			List<StockChart> result = new ArrayList();
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
					if (!Constants.only_read_local) {
						contentJson = ConnectionUtil.getInstance().getJsonObject(iextapisUrl, "UTF-8");
					}
					for(String symbol : symbols) {
						if(contentJson != null) {
							JSONObject symbolObj = contentJson.getJSONObject(symbol.toUpperCase());
							if(symbolObj != null) {
								try {
									JSONObject quote = symbolObj.getJSONObject("quote");
									JSONArray chart = symbolObj.getJSONArray("chart");
									JSONArray totalChart = addJSONArrayToStore(symbol, chart);
									if(quote != null && totalChart != null) {
										StockChart stockChart = new StockChart(quote, totalChart);
										if (stockChart.getStockData() == null) {
											System.out.println("symbol error :" + symbol);
										}
										Calculator calculator = new HighVolBreakCalculator(stockChart.getStockData(), stockChart.getChartStocks());
										if(calculator.match()) {
											StockResult stockResult = new StockResult();
											stockResult.setDate(stockChart.getLatestDate());
											stockResult.setSymbol(symbol);
											stockResult.setName(stockChart.getCompanyName());

											resultMap.computeIfAbsent(calculator.getName(), o -> new ArrayList<>());
											resultMap.get(calculator.getName()).add(stockResult);
										}

										count++;
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
								JSONArray totalChart = addJSONArrayToStore(symbol, null);
								if(totalChart != null) {
									StockChart stockChart = new StockChart(symbol, totalChart);
									if (stockChart.getStockData() == null) {
										System.out.println("symbol error : " + symbol);
										continue;
									}
									Calculator calculator = new HighVolBreakCalculator(stockChart.getStockData(), stockChart.getChartStocks());
									if(calculator.match()) {
										StockResult stockResult = new StockResult();
										stockResult.setDate(stockChart.getLatestDate());
										stockResult.setSymbol(symbol);
										stockResult.setName(stockChart.getCompanyName());

										resultMap.computeIfAbsent(calculator.getName(), o -> new ArrayList<>());
										resultMap.get(calculator.getName()).add(stockResult);
									}

									count++;
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
			return daysBetween(latestLocalDate, nowDate) + 1;
		} else {
			return 0;
		}
	}

	public int daysBetween(Date smdate,Date bdate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();
		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);
		return Integer.parseInt(String.valueOf(between_days));
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
