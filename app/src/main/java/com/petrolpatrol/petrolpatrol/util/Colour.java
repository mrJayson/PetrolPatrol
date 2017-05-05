package com.petrolpatrol.petrolpatrol.util;

public class Colour {
    public final String hex;
    public final int r;
    public final int g;
    public final int b;

    public Colour(String hex) {
        this.hex = hex;
        this.r = Integer.parseInt(hex.substring(0,2),16);
        this.g = Integer.parseInt(hex.substring(2,4),16);
        this.b = Integer.parseInt(hex.substring(4,6),16);
    }

    public Colour(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.hex = (Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b)).toUpperCase();
    }
}
