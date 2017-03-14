package com.petrolpatrol.petrolpatrol.util;

public class Constants {

    // Unique app wide IDs to identify a permission request
    public static final int PERMISSION_REQUEST_ACCESS_LOCATION = 1;

    // Intent identifiers for broadcast receivers
    public static final String NEW_LOCATION_AVAILABLE = "new location available";

    // Maximum zoom allowed for map view
    public static final float MAX_ZOOM = 16;

    // Western most longitude of NSW
    public static final float WEST_BOUND = (float) 141;
    public static final float NORTH_BOUND = (float) -28.157088;
    public static final float EAST_BOUND = (float) 153.638723;
    public static final float SOUTH_BOUND = (float) -37.505033;
}
