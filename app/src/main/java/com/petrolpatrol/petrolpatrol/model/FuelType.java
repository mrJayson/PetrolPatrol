package com.petrolpatrol.petrolpatrol.model;

/**
 * Created by jason on 17/02/17.
 */
public class FuelType {

    private final Integer id;
    private final String code;
    private final String name;

    public FuelType(Integer id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
