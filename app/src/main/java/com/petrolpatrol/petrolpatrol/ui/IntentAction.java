package com.petrolpatrol.petrolpatrol.ui;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class IntentAction implements Parcelable {

    // type definitions
    public static final String FIND_BY_GPS = "FIND_BY_GPS";
    public static final String FIND_BY_LOCATION = "FIND_BY_LOCATION";

    public final String action;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({FIND_BY_GPS, FIND_BY_LOCATION})
    public @interface ActionDef{}

    public IntentAction(@ActionDef String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return action;
    }


    protected IntentAction(Parcel in) {
        action = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(action);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<IntentAction> CREATOR = new Parcelable.Creator<IntentAction>() {
        @Override
        public IntentAction createFromParcel(Parcel in) {
            return new IntentAction(in);
        }

        @Override
        public IntentAction[] newArray(int size) {
            return new IntentAction[size];
        }
    };
}