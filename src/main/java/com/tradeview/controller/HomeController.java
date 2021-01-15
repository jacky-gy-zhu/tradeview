package com.tradeview.controller;

import com.tradeview.stock.config.Constants;
import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.ResultReport;
import com.tradeview.stock.model.StockResult;
import com.tradeview.stock.service.ScanJob;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @ResponseBody
    @RequestMapping("/live")
    public String live() {

        Constants.allow_override_json_data = false; // 仅在收盘后设置true
        Constants.only_read_local = false;
        Constants.throw_if_error_and_print_url = false;
        Param.T_PLUS = 0;

        return generateHtml("Live US Stock Select");
    }

    @ResponseBody
    @RequestMapping("/review")
    public String review() {

        Constants.allow_override_json_data = false; // 仅在收盘后设置true
        Constants.only_read_local = true;
        Constants.throw_if_error_and_print_url = false;
        Param.T_PLUS = 0;

        return generateHtml("Review US Stock Select");
    }

    @ResponseBody
    @RequestMapping("/update")
    public String update() {

        Constants.allow_override_json_data = true; // 仅在收盘后设置true
        Constants.only_read_local = false;
        Constants.throw_if_error_and_print_url = false;
        Param.T_PLUS = 0;

        return "update is done : " + Constants.SDF2.format(new Date());
    }

    private String generateHtml(String title) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>" + title + "</title></head><body>");
        ScanJob scanJob = new ScanJob();
        ResultReport resultReport = scanJob.runScan(false);
        if(resultReport != null) {
            Map<String, List<StockResult>> resultMap =  resultReport.getResultMap();
            for (Map.Entry<String, List<StockResult>> entry : resultMap.entrySet()) {
                String strategy = entry.getKey();
                List<StockResult> stockResults = entry.getValue();
                html.append("<hr>");
                html.append("<h1>"+strategy + " (" + stockResults.size() + ")</h1>");
                for(StockResult stock : stockResults) {
                    html.append("<div>");
                    if (Param.T_PLUS > 0) {
                        html.append(stock.getDate() + " : ");
                    }
                    html.append(stock.getSymbol());
                    if (stock.getName() != null && stock.getName().length() > 0) {
                        html.append(" - " + stock.getName());
                    }
                    if (stock.getRate() != null) {
                        html.append(" (" + (float)((int)(stock.getRate()*100))/100f + ") ");
                    }
                    if (stock.getPeriod() != null) {
                        html.append(" (" + stock.getPeriod() + ") ");
                    }
                    html.append("<br><img src=\"http://image.sinajs.cn/newchart/v5/usstock/daily/" + stock.getSymbol() + ".gif\">");
                    html.append("</div>");
                }
            }
            html.append("</body></html>");
        }
        return html.toString();
    }

}
