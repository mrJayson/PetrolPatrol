package com.petrolpatrol.petrolpatrol.ui;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Action {

    // type definitions
    public static final String FIND_BY_GPS = "find_by_gps";
    public static final String FIND_BY_LOCATION = "find_by_location";

    public final String action;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({FIND_BY_GPS, FIND_BY_LOCATION})
    public @interface ActionDef{}

    public Action(@ActionDef String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
