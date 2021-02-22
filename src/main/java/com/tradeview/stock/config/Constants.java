package com.tradeview.stock.config;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {

    public static boolean OPERATION_LOCKED = false;

    public static boolean allow_override_json_data = false;
    public static boolean only_read_local = false;
    public static boolean throw_if_error_and_print_url = false;

    public static String iextapis_token = "pk_8b34a5210db94eb3be6f3675277b3746";

    public static String localDataRootPath = "/Users/jackyzhu/Documents/workspace/projects/private/tradeview-data/data";

    public static String codesPath = localDataRootPath + "/valid-stock-uscodes-2019-06-08.txt";
    public static String exclusionPath = localDataRootPath + "/exclusionData.txt";
    public static String exclusionSmallVolPath = localDataRootPath + "/excludeSmallVolData.txt";
    public static String testPath = localDataRootPath + "/test.txt";
    public static String stockDataJsonFolder = localDataRootPath + "/json";

    public static SimpleDateFormat SDF = new SimpleDateFormat("MMMMM d, yyyy", new Locale("en", "AU"));
    public static SimpleDateFormat SDF2 = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat SDF3 = new SimpleDateFormat("yyyy-MM-dd EEEE");
    public static DecimalFormat DF = new DecimalFormat("0.00");

}
