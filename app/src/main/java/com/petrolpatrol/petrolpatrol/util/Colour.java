package com.petrolpatrol.petrolpatrol.util;

import android.graphics.Color;

public class Colour {
    public final String hex;
    public final int r;
    public final int g;
    public final int b;
    public final int integer;

    public Colour(String hex) {
        this.hex = hex;
        this.r = Integer.parseInt(hex.substring(0,2),16);
        this.g = Integer.parseInt(hex.substring(2,4),16);
        this.b = Integer.parseInt(hex.substring(4,6),16);
        this.integer = Color.rgb(r,g,b);
    }

    public Colour(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.hex = (Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b)).toUpperCase();
        this.integer = Color.rgb(r,g,b);
    }
}
