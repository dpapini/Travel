package com.saporiditoscana.travel;

import com.google.gson.*;
import com.saporiditoscana.travel.Orm.Conducente;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ConducenteListDeserializer implements JsonDeserializer<List<Conducente>> {
    @Override
    public List<Conducente> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
        List<Conducente> conducenteList = new ArrayList<>();

        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();

            for (JsonElement element : jsonArray) {
                Conducente conducente = context.deserialize(element, Conducente.class);
                conducenteList.add(conducente);
            }
        }

        return conducenteList;
        } catch (Exception e) {
            throw new JsonParseException("Error ConducenteListDeserializer: ", e);
        }
    }
}
