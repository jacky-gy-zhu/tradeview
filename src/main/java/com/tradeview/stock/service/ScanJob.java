package com.tradeview.stock.service;

import com.tradeview.stock.config.Constants;
import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.ResultReport;
import com.tradeview.stock.model.StockResult;
import com.tradeview.stock.util.StreamUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ScanJob {

    public void runScan(boolean isTest) {
        long begin = System.currentTimeMillis();
        System.out.println("开始检索...");

        String path;
        if(isTest) {
            path = Constants.testPath;
        } else {
            path = Constants.codesPath;
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
            }
        }
        System.out.println();
        System.out.println("Total cost: "+((System.currentTimeMillis()-begin)/1000)+"s");
    }

}
