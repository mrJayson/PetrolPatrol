package com.petrolpatrol.petrolpatrol.home;

enum ChartResolution {

    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year");

    private String handle;

    ChartResolution(String handle) {
        this.handle = handle;
    }

    public String getHandle() {
        return handle;
    }

    public static ChartResolution toEnum(String handle) {
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
