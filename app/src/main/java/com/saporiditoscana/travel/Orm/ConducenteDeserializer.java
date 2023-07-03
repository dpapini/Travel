package com.saporiditoscana.travel.Orm;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ConducenteDeserializer implements JsonDeserializer<Conducente> {

    @Override
    public Conducente deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try{
            JsonObject jsonObject = json.getAsJsonObject();

            String id = jsonObject.get("Id").getAsString();
            String testo = jsonObject.get("Testo").getAsString();

            Conducente conducente = new Conducente();
            conducente.setId(id);
            conducente.setTesto(testo);

            return conducente;
        } catch (Exception e) {
            throw new JsonParseException("Error ConducenteDeserializer: ", e);
        }
    }
}
