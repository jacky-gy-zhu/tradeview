package com.tradeview.stock.model;

public class StockData {

	private String date;
	private double lclose;
	private double topen;
	private double tclose;
	private double tHigh;
	private double tlow;
	private int volume;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public double getLclose() {
		return lclose;
	}
	public void setLclose(double lclose) {
		this.lclose = lclose;
	}
	public double getTopen() {
		return topen;
	}
	public void setTopen(double topen) {
		this.topen = topen;
	}
	public double getTclose() {
		return tclose;
	}
	public void setTclose(double tclose) {
		this.tclose = tclose;
	}
	public double gettHigh() {
		return tHigh;
	}
	public void settHigh(double tHigh) {
		this.tHigh = tHigh;
	}
	public double getTlow() {
		return tlow;
	}
	public void setTlow(double tlow) {
		this.tlow = tlow;
	}
	public int getVolume() {
		return volume;
	}
	public void setVolume(int volume) {
		this.volume = volume;
	}
	
}
