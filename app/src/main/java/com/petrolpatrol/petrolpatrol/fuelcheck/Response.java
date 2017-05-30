package com.petrolpatrol.petrolpatrol.fuelcheck;

import org.json.JSONArray;
import org.json.JSONObject;

public class Response {

    private boolean success;
    private String message;
    private Object data;

    //check what kind of data is returned in the json
    public boolean dataIsArray() {
        return (data != null && data instanceof JSONArray);
    }

    public boolean dataIsObject() {
        return (data != null && data instanceof JSONObject);
    }

    public boolean dataIsInteger() {
        return (data != null && data instanceof Integer);
    }

    //return the data properly casted
    public JSONArray getDataAsArray() {
        if (this.dataIsArray()) {
            return (JSONArray) this.data;
        } else {
            return null;
        }
    }

    public JSONObject getDataAsObject() {
        if (this.dataIsObject()) {
            return (JSONObject) this.data;
        } else {
            return null;
        }
    }

    public Integer getDataAsInteger() {
        if (this.dataIsInteger()) {
            return (Integer) this.data;
        } else {
            return null;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
