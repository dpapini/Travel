package com.saporiditoscana.travel;

import com.google.gson.annotations.SerializedName;

import java.util.StringJoiner;

public class Result {
    @SerializedName("Data")
    public Object Data;
    @SerializedName("Error")
    public Object Error;

    public Object getData() {
        return Data;
    }

    public void setData(Object data) {
        Data = data;
    }

    public Object getError() {
        return Error;
    }

    public void setError(Object error) {
        Error = error;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Result.class.getSimpleName() + "[", "]")
                .add("Data=" + Data)
                .add("Error=" + Error)
                .toString();
    }
}
