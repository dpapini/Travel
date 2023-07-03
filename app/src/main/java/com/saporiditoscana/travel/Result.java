package com.saporiditoscana.travel;

import androidx.annotation.NonNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.StringJoiner;

public class Result {

    public Result() {
        // Costruttore di default
    }
    public Result(String data, String error) {
        this.setData(data);
        this.setError(error);
    }
    @SerializedName("Data")
    public String Data;
    @SerializedName("Error")
    public String Error;

    public String getData() {
        return Data;
    }

    public void setData(String data) {
        Data = data;
    }

    public String getError() {
        return Error;
    }

    public void setError(String error) {
        Error = error;
    }

    @NonNull
    @Override
    public String toString() {
        return new StringJoiner(", ", Result.class.getSimpleName() + "[", "]")
                .add("Data=" + String.valueOf(Data))
                .add("Error=" + String.valueOf(Error))
                .toString();
    }
}



