package com.saporiditoscana.travel.Orm;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class GiroDeserializer implements JsonDeserializer<Giro> {

    @Override
    public Giro deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            JsonObject jsonObject = json.getAsJsonObject();

            Giro giro = new Giro();
            giro.setCdDep(jsonObject.get("CdDep").getAsString());
            giro.setCdGiro(jsonObject.get("CdGiro").getAsString());
            giro.setDsGiro(jsonObject.get("DsGiro").getAsString());
            giro.setDtConsegna(jsonObject.get("DtConsegna").getAsString());

            return giro;
        } catch (Exception e) {
            throw new JsonParseException("Error GiroDeserializer: ", e);
        }
    }
}
