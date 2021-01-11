package com.tradeview.stock.calc;

import com.tradeview.stock.model.StockResult;

public interface Calculator {

	boolean match(StockResult stockResult);

	String getName();
	
}
