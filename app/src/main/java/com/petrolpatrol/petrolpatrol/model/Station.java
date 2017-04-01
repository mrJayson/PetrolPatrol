package com.petrolpatrol.petrolpatrol.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Station implements Parcelable {

    // Distance is only given if there is a location to measure, not all use cases give a location
    public static final int NO_DISTANCE = -1;

    private final Brand brand;
    private final int id;
    private final String name;
    private final String address;
    private Map<String, Price> prices;
    private final double latitude;
    private final double longitude;
    private final double distance;

    public Station(Brand brand, int id, String name, String address, double latitude, double longitude) {
        this(brand, id, name, address, latitude, longitude, NO_DISTANCE);
    }

    public Station(Brand brand, int id, String name, String address, double latitude, double longitude, double distance) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.brand = brand;
        this.prices = new HashMap<>();
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }

    public int getId() {
        return id;
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
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDistance() {
        return distance;
    }

    public Price getPrice(String code) {
        if (prices.containsKey(code)) {
            return prices.get(code);
        } else {
            return null;
        }
    }

    public Collection<Price> getAllPrices() {
        return prices.values();
    }

    public void setPrice(Price price) {
        if (!prices.containsKey(price.getFuelType().getCode())) {
            prices.put(price.getFuelType().getCode(), price);
        }
    }

    protected Station(Parcel in) {
        brand = (Brand) in.readValue(Brand.class.getClassLoader());
        id = in.readInt();
        name = in.readString();
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        distance = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(brand);
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(distance);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Station> CREATOR = new Parcelable.Creator<Station>() {
        @Override
        public Station createFromParcel(Parcel in) {
            return new Station(in);
        }

        @Override
        public Station[] newArray(int size) {
            return new Station[size];
        }
    };
}