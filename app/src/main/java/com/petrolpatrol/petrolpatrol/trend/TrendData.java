package com.petrolpatrol.petrolpatrol.trend;

public class TrendData {
    private String period;
    private String captured;
    private double price;

    public TrendData(String period, String captured, double price) {
        this.period = period;
        this.captured = captured;
        this.price = price;
    }

    public String getPeriod() {
        return period;
    }

    public String getCaptured() {
        return captured;
    }

    public double getPrice() {
        return price;
    }
}
