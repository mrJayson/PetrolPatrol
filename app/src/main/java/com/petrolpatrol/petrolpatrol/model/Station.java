package com.petrolpatrol.petrolpatrol.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jason on 17/02/17.
 */
public class Station {

    private static final int NO_DISTANCE = 0;

    private final int id;
    private final String name;
    private final String address;
    private final Brand brand;
    private final Location location;
    private Map<String, Price> prices;


    private class Location {
        private final double latitude;
        private final double longitude;
        private final double distance;

        public Location(double latitude, double longitude, double distance) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.distance = distance;
        }

        public Location(double latitude, double longitude) {
            this(latitude,longitude, NO_DISTANCE);
        }
    }

    public Station(int id, String name, String address, Brand brand, Location location) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.brand = brand;
        this.location = location;
        this.prices = new HashMap<String, Price>();
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Brand getBrand() {
        return brand;
    }

    public double getLatitude() {
        return location.latitude;
    }

    public double getLongitude() {
        return location.longitude;
    }

    public double getDistance() {
        return location.distance;
    }

    public Price getPrice(String code) {
        if (prices.containsKey(code)) {
            return prices.get(code);
        } else {
            return null;
        }
    }

    public void setPrice(Price price) {
        if (!prices.containsKey(price.getFuelType().getCode())) {
            prices.put(price.getFuelType().getCode(), price);
        }
    }
}
