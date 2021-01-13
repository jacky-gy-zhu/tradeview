package com.tradeview.stock.config;

public class Param {
	
	// 向前推演
	public static int T_PLUS = 0;

	// 黄金分割
	public static float GOLD_CUT_RATE = 0.618f;
	public static float GOLD_CUT_RATE2 = 0.809f;

	// 排查小量交易股票
	public static float AVG_AMOUNT_VOL_TOLERANCE = 500 * 10000;
	
	// 计算周期
	public static int MONTH_PERIOD = 2;
	
	// 今日收盘价大于60日内的最高点（但不超过5%）
	public static float EXCEED_RATE = 0.05f;
	
	// 今日的上影线不能过长
	public static float TODAY_HIGH_SHADOW_RATE = 0.3f;

	// 今日红K（超过3%）
	public static float TODAY_RED_K_RATE = 0.03f;

	// 最大量是平均量的倍数（2倍）
	public static float HUGE_VOL_RATE = 2f;

	// 今日涨幅
	public static float TODAY_UP_RATE = 0.03f;

	// 突破涨幅不得超过（5%）
	public static float BREAK_EXCEED_RATE = 0.05f;

	// 均线粘合（MA5和其他均线的差距比例）
	public static float MA5_MA30_GAP_RATE = 0.2f;
	public static float MA5_MA20_GAP_RATE = 0.1f;
	public static float MA5_MA10_GAP_RATE = 0.02f;

	// N字型走势回调最小幅度(最高点，最低点)
	public static float N_BACK_HIGH_LOW_MIN_RATE = 0.1f;
	// N字型走势回调最小幅度(MA5)
	public static float N_BACK_MA5_MIN_RATE = 0.01f;
	// N字型走势回调比例最大值
	public static float N_BACK_BODY_MAX_RATE = 0.382f;
	// N字型走势回调比例最小值
	public static float N_BACK_BODY_MIN_RATE = 0.236f;
	// 调整时间最大值
	public static int N_BACK_MIN_DAYS = 10;

	// 3脚连线误差比率
	public static float THREE_FOOTER_MAX_GAP_RATE = 0.9f;
	// 前2次回调幅度差不多
	public static float THREE_FOOTER_PERIOD_EVEN_RATE = 0.5f;
	// 高点和低点的震动幅度
	public static float THREE_FOOTER_HIGH_LOW_WAVE_RATE = 0.15f;
	// 最大波浪间隔
	public static int THREE_FOOTER_PERIOD_GAP_DAYS = 20;

	// ABC修正高点最大范围
	public static int ABC_CALLBACK_MAX_DAY_RANGE = 22;
	public static int ABC_CALLBACK_MIN_GAP = 3;
}
