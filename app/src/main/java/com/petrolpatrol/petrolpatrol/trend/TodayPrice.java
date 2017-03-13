package com.petrolpatrol.petrolpatrol.trend;

import com.google.gson.annotations.SerializedName;
import com.petrolpatrol.petrolpatrol.model.FuelType;

public class TodayPrice {

    private final FuelType fuelType;
    private final double price;
    private final double variance;

    public TodayPrice(FuelType fuelType, double price, double variance) {
        this.fuelType = fuelType;
        this.price = price;
        this.variance = variance;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public double getPrice() {
        return price;
    }

    public double getVariance() {
        return variance;
    }
}
