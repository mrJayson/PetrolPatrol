package com.petrolpatrol.petrolpatrol.trend;

/**
 * Created by jason on 5/03/17.
 */
public enum TrendResolution {

    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year");

    private String handle;

    TrendResolution(String handle) {
        this.handle = handle;
    }

    public String getHandle() {
        return handle;
    }
}
