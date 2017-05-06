package com.petrolpatrol.petrolpatrol.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class AverageParcel implements Parcelable {

    public static final String ARG_AVERAGE = "ARG_AVERAGE";

    private Map<String, Average> averages;

    public AverageParcel(Map<String, Average> averages) {
        this.averages = averages;
    }

    public Map<String, Average> getAverages() {
        return averages;
    }

    protected AverageParcel(Parcel in) {
        averages = new HashMap<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            averages.put(in.readString(), (Average) in.readValue(Average.class.getClassLoader()));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(averages.size());
        for(Map.Entry<String, Average> entry : averages.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeValue(entry.getValue());
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AverageParcel> CREATOR = new Parcelable.Creator<AverageParcel>() {
        @Override
        public AverageParcel createFromParcel(Parcel in) {
            return new AverageParcel(in);
        }

        @Override
        public AverageParcel[] newArray(int size) {
            return new AverageParcel[size];
        }
    };
}
