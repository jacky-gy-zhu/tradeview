package com.tradeview.stock.config;

public class Param {
	
	// 向前推演
	public static int T_PLUS = 0;

	// 排查小量交易股票
	public static float AVG_AMOUNT_VOL_TOLERANCE = 500 * 10000;
	
	// 计算周期
	public static int MONTH_PERIOD = 1;
	
	// 今日收盘价大于60日内的最高点（但不超过5%）
	public static float EXCEED_RATE = 0.05f;
	
	// 今日的上影线不能过长
	public static float TODAY_HIGH_SHADOW_REATE = 0.3f;

	// 今日红K（超过3%）
	public static float TODAY_RED_K_RATE = 0.03f;

	// 最大量是平均量的倍数（2倍）
	public static float HUGE_VOL_RATE = 2f;

	// 最大量的最高点距离今日不超过10%
	public static float HUGE_VOL_PRICE_TO_TODAY_GAP_RATE = 0.1f;

	// 均线粘合（MA5和其他均线的差距比例）
	public static float MA5_MA30_GAP_RATE = 0.2f;
	public static float MA5_MA20_GAP_RATE = 0.1f;
	public static float MA5_MA10_GAP_RATE = 0.02f;

}
