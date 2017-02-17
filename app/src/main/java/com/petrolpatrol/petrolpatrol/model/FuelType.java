package com.petrolpatrol.petrolpatrol.model;

/**
 * Created by jason on 17/02/17.
 */
public class FuelType {

    public static final int NO_ID = -1;

    private final int id;
    private final String code;
    private final String name;

    public FuelType(String code, String name) {
        this(NO_ID, code, name);
    }

    public FuelType(int id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
