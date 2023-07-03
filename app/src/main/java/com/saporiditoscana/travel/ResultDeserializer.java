package com.saporiditoscana.travel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ResultDeserializer implements JsonDeserializer<Result> {
    @Override
    public Result deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Estrarre i valori dal JSON
        String data = jsonObject.get("Data").isJsonNull() ? "": jsonObject.get("Data").getAsString();
        String error = jsonObject.get("Error").isJsonNull() ? "": jsonObject.get("Error").getAsString();
        // ... continua con gli altri valori che desideri estrarre

        // Creare un'istanza di Result con i valori estratti
        Result result = new Result(data, error);

        return result;
    }
}
