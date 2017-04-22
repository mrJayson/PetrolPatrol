package com.petrolpatrol.petrolpatrol.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Price implements Parcelable {

    public static final int NO_ID = -1;

    private final int id;

    private final int stationID;
    private final FuelType fuelType;
    private final double price;
    private final String lastUpdated;
    public Price(int stationID, FuelType fuelType, double price, String lastUpdated) {
        this(NO_ID, stationID, fuelType, price, lastUpdated);
    }

    public Price(int id, int stationID, FuelType fuelType, double price, String lastUpdated) {
        this.id = id;
        this.stationID = stationID;
        this.fuelType = fuelType;
        this.price = price;
        this.lastUpdated = lastUpdated;
    }

    public int getId() {
        return id;
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

    protected Price(Parcel in) {
        id = in.readInt();
        stationID = in.readInt();
        fuelType = (FuelType) in.readValue(FuelType.class.getClassLoader());
        price = in.readDouble();
        lastUpdated = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(stationID);
        dest.writeValue(fuelType);
        dest.writeDouble(price);
        dest.writeString(lastUpdated);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Price> CREATOR = new Parcelable.Creator<Price>() {
        @Override
        public Price createFromParcel(Parcel in) {
            return new Price(in);
        }

        @Override
        public Price[] newArray(int size) {
            return new Price[size];
        }
    };
}