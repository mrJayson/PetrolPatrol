package com.petrolpatrol.petrolpatrol.model;

/**
 * Created by jason on 17/02/17.
 */
public class Brand {

    public static final int NO_ID = -1;

    private final int id;
    private final String name;

    public Brand(String name) {
        this(NO_ID, name);
    }

    public Brand(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
