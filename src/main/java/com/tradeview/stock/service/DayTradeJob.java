package com.tradeview.stock.service;

import com.tradeview.stock.config.Constants;
import com.tradeview.stock.model.ResultReport;
import com.tradeview.stock.model.StockResult;
import com.tradeview.stock.util.StreamUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DayTradeJob {

    public ResultReport runScan(boolean isTest) {

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
                    if (resultMap.values() != null) {
                        List<String> nameList = resultMap.values().stream()
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList())
                                .stream()
                                .map(StockResult::getSymbol)
                                .collect(Collectors.toList());
                        String row = String.join("\n", nameList);
                        if (Constants.is_short) {
                            StreamUtils.writeFileContent(Constants.dayTraderBearPath, row);
                        } else {
                            StreamUtils.writeFileContent(Constants.dayTraderBullPath, row);
                        }
                    }
                }
                return resultReport;
            }
        }
        return null;
    }

}
