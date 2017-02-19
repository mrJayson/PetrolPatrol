package com.petrolpatrol.petrolpatrol.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jason on 17/02/17.
 */
public class Station implements Parcelable {

    public static final int NO_ID = -1;
    public static final int NO_DISTANCE = 0;

    private final Brand brand;
    private final int id;
    private final String name;
    private final String address;
    private final Location location;
    private Map<String, Price> prices;


    public static class Location implements Parcelable {
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

        protected Location(Parcel in) {
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
            dest.writeDouble(latitude);
            dest.writeDouble(longitude);
            dest.writeDouble(distance);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
            @Override
            public Location createFromParcel(Parcel in) {
                return new Location(in);
            }

            @Override
            public Location[] newArray(int size) {
                return new Location[size];
            }
        };
    }

    public Station(Brand brand, String name, String address, double latitude, double longitude) {
        this(brand, NO_ID, name, address, latitude, longitude, NO_DISTANCE);
    }

    public Station(Brand brand, int id, String name, String address, double latitude, double longitude) {
        this(brand, id, name, address, latitude, longitude, NO_DISTANCE);
    }

    public Station(Brand brand, String name, String address, double latitude, double longitude, double distance) {
        this(brand, NO_ID, name, address, latitude, longitude, distance);
    }

    public Station(Brand brand, int id, String name, String address, double latitude, double longitude, double distance) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.brand = brand;
        this.location = new Location(latitude, longitude, distance);
        this.prices = new HashMap<String, Price>();
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

    protected Station(Parcel in) {
        brand = (Brand) in.readValue(Brand.class.getClassLoader());
        id = in.readInt();
        name = in.readString();
        address = in.readString();
        location = (Location) in.readValue(Location.class.getClassLoader());
        prices = new HashMap<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            prices.put(in.readString(), (Price) in.readValue(Price.class.getClassLoader()));
        }
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
        dest.writeValue(location);
        dest.writeInt(prices.size());
        for(Map.Entry<String, Price> entry : prices.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeValue(entry.getValue());
        }
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