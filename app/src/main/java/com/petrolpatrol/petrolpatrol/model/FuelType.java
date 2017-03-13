package com.petrolpatrol.petrolpatrol.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FuelType implements Parcelable {

    public static final int NO_ID = -1;

    private final int id;
    private final String code;
    private final String name;

    public FuelType(String code, String name) {
        this(NO_ID, code, name);
    }

    public FuelType(int id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    protected FuelType(Parcel in) {
        id = in.readInt();
        code = in.readString();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(code);
        dest.writeString(name);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FuelType> CREATOR = new Parcelable.Creator<FuelType>() {
        @Override
        public FuelType createFromParcel(Parcel in) {
            return new FuelType(in);
        }

        @Override
        public FuelType[] newArray(int size) {
            return new FuelType[size];
        }
    };
}