package com.tradeview.stock.api;

import com.tradeview.stock.config.Constants;

public class Iexapis {

	public static int count = 0;
	
	/*
		https://cloud.iexapis.com/stable/stock/market/batch?symbols=x&types=chart&range=3m&last=1&token=pk_8b34a5210db94eb3be6f3675277b3746
	 */
	public static String getUrlForDailyK(String symbolGroupStr, int month, int last) {
		if (last == 0) {
			last = month*22;
		}
		String quote = "";
		if (!Constants.allow_override_json_data) {
			quote = "quote,";
		}
		String url = "https://cloud.iexapis.com/stable/stock/market/batch?symbols="+symbolGroupStr+"&types="+quote+"chart&range="+month+"m&last="+last+"&token="+ Constants.iextapis_token;
		if (Constants.throw_if_error_and_print_url) {
			System.out.println(url);
		}
		count += last;
		return url;
	}

}
