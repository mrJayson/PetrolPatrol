package com.petrolpatrol.petrolpatrol.model;

/**
 * Created by jason on 17/02/17.
 */
public class Price {

    private final FuelType fuelType;
    private final double price;
    private final String lastUpdated;

    public Price(FuelType fuelType, double price, String lastUpdated) {
        this.fuelType = fuelType;
        this.price = price;
        this.lastUpdated = lastUpdated;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public double getPrice() {
        return price;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }
}
