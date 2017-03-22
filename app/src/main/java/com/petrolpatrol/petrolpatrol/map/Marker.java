package com.petrolpatrol.petrolpatrol.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Marker implements ClusterItem {

    private final LatLng position;
    private final String title;
    private final String snippet;

    public Marker(LatLng position) {
        this(position, null, null);
    }

    public Marker(double latitude, double longitude) {
        this(latitude, longitude, null, null);
    }

    public Marker(double latitude, double longitude, String title, String snippet) {
        this.position = new LatLng(latitude, longitude);
        this.title = title;
        this.snippet = snippet;
    }

    public Marker(LatLng position, String title, String snippet) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }
}
