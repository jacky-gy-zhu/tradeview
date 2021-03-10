package com.tradeview.job;

import com.tradeview.stock.config.Constants;
import com.tradeview.stock.config.Param;
import com.tradeview.stock.service.DayTradeJob;
import com.tradeview.stock.service.ScanJob;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

@Configuration
@EnableScheduling
public class SpringConfig {

    // 中国14点 = 澳洲17点
    @Scheduled(cron = "0 0 17 * * 2-6")
    public void updateStockData() {

        System.out.println("schedule tasks begin to updateStockData - " + Constants.SDF2.format(new Date()));

        if (!Constants.OPERATION_LOCKED) {
            try {
                Constants.OPERATION_LOCKED = true;

                Constants.is_day_trade = false;
                Constants.is_short = false;
                Constants.allow_override_json_data = true; // 仅在收盘后设置true
                Constants.only_read_local = false;
                Constants.throw_if_error_and_print_url = false;
                Param.T_PLUS = 0;

                ScanJob scanJob = new ScanJob();
                scanJob.runScan(false);

                Constants.is_save = true;
                Constants.is_day_trade = true;
                Constants.is_short = false;
                DayTradeJob dayTradeJob = new DayTradeJob();
                dayTradeJob.runScan(false);

                Constants.is_save = true;
                Constants.is_day_trade = true;
                Constants.is_short = true;
                DayTradeJob dayTradeJob2 = new DayTradeJob();
                dayTradeJob2.runScan(false);

                System.out.println("schedule tasks updateStockData done - " + Constants.SDF2.format(new Date()));
            } finally {
                Constants.OPERATION_LOCKED = false;
            }

        }
    }

}
