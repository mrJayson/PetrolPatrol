package com.petrolpatrol.petrolpatrol.fuelcheck;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RequestTag {

    // type definitions
    public static final String GET_FUELPRICES_WITHIN_RADIUS = "get fuel prices within radius";

    public final String tag;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({GET_FUELPRICES_WITHIN_RADIUS})
    public @interface RequestTagDef {}

    public RequestTag(@RequestTagDef String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
