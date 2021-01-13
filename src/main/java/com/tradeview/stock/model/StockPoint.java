package com.tradeview.stock.model;

public class StockPoint {

    public StockPoint(double price, int index) {
        this.price = price;
        this.index = index;
    }

    private double price;
    private int index;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
