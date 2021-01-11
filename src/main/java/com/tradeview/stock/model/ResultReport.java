package com.tradeview.stock.model;

import java.util.List;
import java.util.Map;

public class ResultReport {

    private Map<String, List<StockResult>> resultMap;

    public Map<String, List<StockResult>> getResultMap() {
        return resultMap;
    }

    public void setResultMap(Map<String, List<StockResult>> resultMap) {
        this.resultMap = resultMap;
    }
}
