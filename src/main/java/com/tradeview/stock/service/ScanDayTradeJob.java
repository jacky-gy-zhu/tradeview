package com.tradeview.stock.service;

import com.tradeview.stock.config.Constants;
import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.ResultReport;
import com.tradeview.stock.model.StockResult;
import com.tradeview.stock.util.StreamUtils;

import java.util.*;

public class ScanDayTradeJob {

    public ResultReport runScan() {
        long begin = System.currentTimeMillis();
        System.out.println("开始检索...");

        String path;
        if(Constants.is_short) {
            path = Constants.dayTraderBearPath;
        } else {
            path = Constants.dayTraderBullPath;
        }
        String codes = StreamUtils.getFileContent(path);
        String excludeCodes = StreamUtils.getFileContent(Constants.exclusionPath);
        String excludeSmallVolCodes = StreamUtils.getFileContent(Constants.exclusionSmallVolPath);
        if(codes != null) {
            String[] codeArr = codes.split("\n");
            String[] excludeCodeArr = excludeCodes.split("\n");
            String[] excludeSmallVolCodeArr = excludeSmallVolCodes.split("\n");
            List<String> excludeCodeList = Arrays.asList(excludeCodeArr);
            List<String> excludeSmallVolCodeList = Arrays.asList(excludeSmallVolCodeArr);
            List<String> excludeList = new ArrayList<>();
            excludeList.addAll(excludeCodeList);
            excludeList.addAll(excludeSmallVolCodeList);
            if(codeArr != null) {
                ResultReport resultReport = Iextrading.getInstance().selectStocks(codeArr, excludeList);
                if(resultReport != null) {
                    Map<String, List<StockResult>> resultMap =  resultReport.getResultMap();
                    for (Map.Entry<String, List<StockResult>> entry : resultMap.entrySet()) {
                        String strategy = entry.getKey();
                        List<StockResult> stockResults = entry.getValue();
                        Collections.sort(stockResults, (o1, o2) -> {
                            double s1 = o1.getSort();
                            double s2 = o2.getSort();
                            if (s1 > s2) {
                                return -1;
                            } else if (s1 < s2) {
                                return 1;
                            } else {
                                return 0;
                            }
                        });
                        System.out.println("-----------------------------------------");
                        System.out.println(strategy + " (" + stockResults.size() + ")");
                        for(StockResult stock : stockResults) {
                            if (Param.T_PLUS > 0) {
                                System.out.print(stock.getDate() + " : ");
                            }
                            System.out.print(stock.getSymbol());
                            if (stock.getName() != null && stock.getName().length() > 0) {
                                System.out.print(" - " + stock.getName());
                            }
                            if (stock.getRate() != null) {
                                System.out.print(" (" + (float)((int)(stock.getRate()*100))/100f + ") ");
                            }
                            if (stock.getPeriod() != null) {
                                System.out.print(" (" + stock.getPeriod() + ") ");
                            }
                            System.out.println();
                        }
                    }
                }
                return resultReport;
            }
        }
        System.out.println();
        System.out.println("Total cost: "+((System.currentTimeMillis()-begin)/1000)+"s");
        return null;
    }

}
