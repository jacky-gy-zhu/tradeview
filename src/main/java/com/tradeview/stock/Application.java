package com.tradeview.stock;

import com.tradeview.stock.api.Iexapis;
import com.tradeview.stock.config.Constants;
import com.tradeview.stock.config.Param;
import com.tradeview.stock.service.ScanJob;

import java.text.ParseException;

public class Application {

    public static void main(String[] args) throws ParseException {

        Constants.is_short = false;
        // Please note this config!!! (交易期间必须设置 allow_override_json_data=false )
        Constants.allow_override_json_data = false; // 仅在收盘后设置true
        Constants.only_read_local = true;
        Constants.throw_if_error_and_print_url = false;
        Param.T_PLUS = 0;

        ScanJob scanJob = new ScanJob();
        scanJob.runScan(false);

//        for (int i = 0; i < 20; i++) {
//            Param.T_PLUS = i;
//            ScanJob scanJob = new ScanJob();
//            scanJob.runScan(false);
//        }

        System.out.println("API called times : " + Iexapis.count);

    }
}
