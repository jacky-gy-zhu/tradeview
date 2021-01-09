package com.tradeview.stock.service;

import com.tradeview.stock.config.Constants;
import com.tradeview.stock.model.StockChart;
import com.tradeview.stock.util.StreamUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                List<StockChart> stockList = Iextrading.getInstance().findUSStockForHighVol(codeArr, excludeList, isTest);
                if(stockList != null) {
                    for(StockChart stock : stockList) {
                        System.out.println(stock.getSymbol() + " - " + stock.getCompanyName());
                    }
                }
            }
        }
        System.out.println();
        System.out.println("Total cost: "+((System.currentTimeMillis()-begin)/1000)+"s");
    }

}
