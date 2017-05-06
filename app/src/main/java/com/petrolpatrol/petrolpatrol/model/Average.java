package com.petrolpatrol.petrolpatrol.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Average implements Parcelable {

    private final FuelType fuelType;
    private final double price;
    private final double variance;

    public Average(FuelType fuelType, double price, double variance) {
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

    protected Average(Parcel in) {
        fuelType = (FuelType) in.readValue(FuelType.class.getClassLoader());
        price = in.readDouble();
        variance = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(fuelType);
        dest.writeDouble(price);
        dest.writeDouble(variance);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Average> CREATOR = new Parcelable.Creator<Average>() {
        @Override
        public Average createFromParcel(Parcel in) {
            return new Average(in);
        }

        @Override
        public Average[] newArray(int size) {
            return new Average[size];
        }
    };
}
