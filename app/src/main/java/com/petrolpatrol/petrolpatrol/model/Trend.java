package com.petrolpatrol.petrolpatrol.model;

public class Trend {
    private String period;
    private String captured;
    private double price;

    public Trend(String period, String captured, double price) {
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
