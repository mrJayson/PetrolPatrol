package com.petrolpatrol.petrolpatrol.map;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

class Marker implements ClusterItem {

    private final LatLng position;
    private final String title;
    private final String snippet;
    private final double price;

    Marker(double price, LatLng position) {
        this(price, position, null, null);
    }

    Marker(double price, double latitude, double longitude) {
        this(price, new LatLng(latitude, longitude), null, null);
    }

    Marker(double price, double latitude, double longitude, String title, String snippet) {
        this(price, new LatLng(latitude, longitude), title, snippet);
    }

    Marker(double price, LatLng position, String title, String snippet) {
        this.price = price;
        this.position = position;
        this.title = title;
        this.snippet = snippet;
    }

    public double getPrice() {
        return price;
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
