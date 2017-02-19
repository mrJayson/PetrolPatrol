package com.petrolpatrol.petrolpatrol.model;

/**
 * Created by jason on 17/02/17.
 */
public class Price {

    private final int stationID;
    private final FuelType fuelType;
    private final double price;
    private final String lastUpdated;

    public Price(int stationID, FuelType fuelType, double price, String lastUpdated) {
        this.stationID = stationID;
        this.fuelType = fuelType;
        this.price = price;
        this.lastUpdated = lastUpdated;
    }

    public int getStationID() {
        return stationID;
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
