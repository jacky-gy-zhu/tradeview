package com.tradeview.job;

import com.tradeview.stock.config.Constants;
import com.tradeview.stock.config.Param;
import com.tradeview.stock.service.ScanJob;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

@Configuration
@EnableScheduling
public class SpringConfig {

    // 中国14点 = 澳洲17点
    @Scheduled(cron = "0 0 14 * * 2-6")
    public void updateStockData() {

        if (!Constants.OPERATION_LOCKED) {
            try {
                Constants.OPERATION_LOCKED = true;

                Constants.allow_override_json_data = true; // 仅在收盘后设置true
                Constants.only_read_local = false;
                Constants.throw_if_error_and_print_url = false;
                Param.T_PLUS = 0;

                ScanJob scanJob = new ScanJob();
                scanJob.runScan(false);

                System.out.println("schedule tasks updateStockData done - " + Constants.SDF2.format(new Date()));
            } finally {
                Constants.OPERATION_LOCKED = false;
            }

        }
    }

}
