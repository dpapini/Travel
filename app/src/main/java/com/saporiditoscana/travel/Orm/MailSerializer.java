package com.saporiditoscana.travel.Orm;

import android.util.Log;

import com.google.gson.*;
import com.saporiditoscana.travel.Orm.Mail;

import java.lang.reflect.Type;
import java.util.List;

public class MailSerializer implements JsonSerializer<Mail> {
    private static final String TAG = "Mail";

    @Override
    public JsonElement serialize(Mail mail, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        try {
            addPropertyIfNotNull(jsonObject, "AddressForm", mail.getAddressForm());
            addPropertyIfNotNull(jsonObject, "AddressTo", mail.getAddressTo());
            addPropertyIfNotNull(jsonObject, "AddressCc", mail.getAddressCc());
            addPropertyIfNotNull(jsonObject, "AddressCcn", mail.getAddressCcn());
            addPropertyIfNotNull(jsonObject, "Subject", mail.getSubject());
            addPropertyIfNotNull(jsonObject, "Message", mail.getMessage());
            addPropertyIfNotNull(jsonObject, "TerminaleId", mail.getTerminaleId());

            JsonArray attachArray = new JsonArray();
            List<Attach> attachCollection = mail.getAttachCollection();

            if (attachCollection != null) {
                for (Attach attach : attachCollection) {
                    JsonObject attachObject = new JsonObject();
                    addPropertyIfNotNull(attachObject, "FileName", attach.getFileName());
                    addPropertyIfNotNull(attachObject, "FileBase64", attach.getFileBase64());
                    addPropertyIfNotNull(attachObject, "MediaType", attach.getFileType());
                    attachArray.add(attachObject);
                }
            }
            jsonObject.add("AttachCollection", attachArray);

            return jsonObject;
        } catch (Exception e) {
            Log.e(TAG, "Error during Mail serialization", e);
            throw new JsonParseException("Error MailSerializer: ", e);
        }
    }

    private void addPropertyIfNotNull(JsonObject jsonObject, String propertyName, Object value) {
        if (value != null) {
            if (value instanceof String) {
                jsonObject.addProperty(propertyName, (String) value);
            } else if (value instanceof Integer) {
                jsonObject.addProperty(propertyName, (Integer) value);
            } else if (value instanceof Boolean) {
                jsonObject.addProperty(propertyName, (Boolean) value);
            } else if (value instanceof Double) {
                jsonObject.addProperty(propertyName, (Double) value);
            } else if (value instanceof Long) {
                jsonObject.addProperty(propertyName, (Long) value);
            } else if (value instanceof Character) {
                jsonObject.addProperty(propertyName, (Character) value);
            } else if (value instanceof JsonElement) {
                jsonObject.add(propertyName, (JsonElement) value);
            }
        } else {
            jsonObject.add(propertyName, JsonNull.INSTANCE);
        }
    }
}
