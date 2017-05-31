package com.petrolpatrol.petrolpatrol.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class Brand implements Parcelable {

    public static final int NO_ID = -1;

    private final int id;
    private final String name;

    public Brand(String name) {
        this(NO_ID, name);
    }

    public Brand(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageName() {
        // need to convert image name into one that adheres to drawable naming conventions
        return "logo_" + name.replaceAll("[ -]", "_").toLowerCase();
    }

    protected Brand(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Brand> CREATOR = new Parcelable.Creator<Brand>() {
        @Override
        public Brand createFromParcel(Parcel in) {
            return new Brand(in);
        }

        @Override
        public Brand[] newArray(int size) {
            return new Brand[size];
        }
    };
}