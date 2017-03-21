package com.petrolpatrol.petrolpatrol.util;

public class Constants {

    // Unique app wide IDs to identify a permission request
    public static final int PERMISSION_REQUEST_ACCESS_LOCATION = 1;
    public static final int PERMISSION_REQUEST_ENABLE_MY_LOCATION = 2;

    // Intent identifiers for broadcast receivers
    public static final String NEW_LOCATION_AVAILABLE = "new location available";

    // Maximum zoom allowed for map view
    public static final float MAX_ZOOM = 16;

    // Default zoom for map view if none have been specified
    public static final float DEFAULT_ZOOM = 13;

    // Minimum zoom allowed for map view
    public static final float MIN_ZOOM = 8;

    // Western most longitude of NSW
    public static final double WEST_BOUND = (double) 141;
    public static final double NORTH_BOUND = (double) -28.157088;
    public static final double EAST_BOUND = (double) 153.638723;
    public static final double SOUTH_BOUND = (double) -37.505033;

    // Sydney location co-ordinates
    public static final double SYDNEY_LAT = -33.86882;
    public static final double SYDNEY_LONG = 151.209296;
}
