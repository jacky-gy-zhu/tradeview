package com.tradeview.controller;

import com.tradeview.stock.config.Constants;
import com.tradeview.stock.config.Param;
import com.tradeview.stock.model.ResultReport;
import com.tradeview.stock.model.StockResult;
import com.tradeview.stock.service.ScanJob;
import com.tradeview.stock.util.StreamUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @ResponseBody
    @RequestMapping("/live")
    public String live() {

        if (!Constants.OPERATION_LOCKED) {
            try {
                Constants.OPERATION_LOCKED = true;

                Constants.allow_override_json_data = false; // 仅在收盘后设置true
                Constants.only_read_local = false;
                Constants.throw_if_error_and_print_url = false;
                Param.T_PLUS = 0;

                return generateHtml("Live US Stock Select", true);
            } finally {
                Constants.OPERATION_LOCKED = false;
            }
        } else {
            return "locked";
        }
    }

    @ResponseBody
    @RequestMapping("/review")
    public String review() {

        if (!Constants.OPERATION_LOCKED) {
            try {
                Constants.OPERATION_LOCKED = true;

                Constants.allow_override_json_data = false; // 仅在收盘后设置true
                Constants.only_read_local = true;
                Constants.throw_if_error_and_print_url = false;
                Param.T_PLUS = 0;

                return generateHtml("Review latest US Stock (Data updated after 17:00)", false);
            } finally {
                Constants.OPERATION_LOCKED = false;
            }
        } else {
            return "locked";
        }
    }

    @ResponseBody
    @RequestMapping("/review/{t}")
    public String reviewT(@PathVariable("t") int t) {

        if (!Constants.OPERATION_LOCKED) {
            try {
                Constants.OPERATION_LOCKED = true;

                Constants.allow_override_json_data = false; // 仅在收盘后设置true
                Constants.only_read_local = true;
                Constants.throw_if_error_and_print_url = false;
                Param.T_PLUS = t;

                return generateHtml("Review US Stock : " + t + " days ago", false);
            } finally {
                Constants.OPERATION_LOCKED = false;
            }
        } else {
            return "locked";
        }
    }


    @ResponseBody
    @RequestMapping("/update")
    public String update() {

        if (!Constants.OPERATION_LOCKED) {
            try {
                Constants.OPERATION_LOCKED = true;

                Constants.allow_override_json_data = true; // 仅在收盘后设置true
                Constants.only_read_local = false;
                Constants.throw_if_error_and_print_url = false;
                Param.T_PLUS = 0;

                ScanJob scanJob = new ScanJob();
                scanJob.runScan(false);

            } finally {
                Constants.OPERATION_LOCKED = false;
            }
            return "done";
        } else {
            return "locked";
        }
    }

    private String generateHtml(String title, boolean live) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>" + title + "</title>" +
                "<link href=\"data:image/png;base64,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\" rel=\"icon\">"+
                "<link rel=\"apple-touch-icon\" href=\"data:image/png;base64,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\"/>"+
                "<style type=\"text/css\">@media only screen and (max-width: 1290px) {\n" +
                "  img {\n" +
                "    width: 100%;\n" +
                "  }\n" +
                "}</style>"+
                "</head><body>");
        if (!live) {
            html.append("<script\n" +
                    "  src=\"https://code.jquery.com/jquery-2.2.4.min.js\"\n" +
                    "  crossorigin=\"anonymous\"></script>\n" +
                    "\n" +
                    "<div id=\"white-cover\" style=\"\n" +
                    "    position: absolute;\n" +
                    "    width: "+(36+10*Param.T_PLUS)+"px;\n" +
                    "    height: 100000px;\n" +
                    "    right: 0px;\n" +
                    "    z-index: 999999999;\n" +
                    "    background-color: #fff;\n" +
                    "\"></div>");
            String xContent = StreamUtils.getFileContent(Constants.stockDataJsonFolder+"/x.json");
            // [{"date":"2021-01-22"
            String latestDate = xContent.substring(10, 20);
            try {
                Date lastDate = Constants.SDF2.parse(latestDate);
                html.append("<h2>" + Constants.SDF3.format(lastDate) + "</h2>");
            } catch (ParseException e) {
            }
        } else {
            html.append("<h2>盘中行情</h2>");
        }
        ScanJob scanJob = new ScanJob();
        ResultReport resultReport = scanJob.runScan(false);
        if(resultReport != null) {
            Map<String, List<StockResult>> resultMap =  resultReport.getResultMap();
            if (resultMap != null && resultMap.size() > 0) {
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
            } else {
                html.append("<h5>无筛选结果</h5>");
            }
            if (!live) {
                html.append("<script type=\"text/javascript\">\n" +
                        "    $('img').click(function(){\n" +
                        "        $('#white-cover').toggle();\n" +
                        "    });\n" +
                        "</script>");
            }
            html.append("</body></html>");
        }
        return html.toString();
    }

}
