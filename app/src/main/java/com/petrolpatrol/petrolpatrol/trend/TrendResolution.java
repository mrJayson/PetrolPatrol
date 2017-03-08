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

    public static TrendResolution toEnum(String handle) {
        switch (handle) {
            case "Week":
                return WEEK;
            case "Month":
                return MONTH;
            case "Year":
                return YEAR;
        }
        return WEEK;
    }
}
