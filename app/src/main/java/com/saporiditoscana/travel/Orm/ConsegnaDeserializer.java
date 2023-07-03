package com.saporiditoscana.travel.Orm;

import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class ConsegnaDeserializer implements JsonDeserializer<Consegna> {
    private static final String TAG = "MainActivity";

    @Override
    public Consegna deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try{
            JsonObject jsonObject = json.getAsJsonObject();
            Consegna consegna = new Consegna();

            consegna.setAnnoReg(jsonObject.get("AnnoReg").getAsInt());
            consegna.setNrReg(jsonObject.get("NrReg").getAsInt());
            consegna.setCdCli(jsonObject.get("CdCli").getAsInt());
            consegna.setRagioneSociale(jsonObject.get("RagioneSociale").getAsString());
            consegna.setIndirizzo(jsonObject.get("Indirizzo").getAsString());
            consegna.setLocalita(jsonObject.get("Localita").getAsString());
            consegna.setCdAge(jsonObject.get("CdAge").getAsString());
            consegna.setCdCapoArea(jsonObject.get("CdCapoArea").getAsString());
            consegna.setMailAge(jsonObject.get("MailAge").getAsString());
            consegna.setMailCapoArea(jsonObject.get("MailCapoArea").getAsString());
            consegna.setSequenza(jsonObject.get("Sequenza").getAsString());
            consegna.setIdEsitoConsegna(jsonObject.get("IdEsitoConsegna").getAsInt());
            consegna.setTsValidita(jsonObject.get("TsValidita").getAsString());
            if (jsonObject.has("flInviato") && !jsonObject.get("flInviato").isJsonNull()) {
                consegna.setFlInviato(jsonObject.get("flInviato").getAsString());
            }else {
                consegna.setFlInviato("N");
            }
            if (jsonObject.has("Testo") && !jsonObject.get("Testo").isJsonNull()) {
                consegna.setTesto(jsonObject.get("Testo").getAsString());
            }else {
                consegna.setTesto("");
            }
            consegna.setTipoDocumento(jsonObject.get("TipoDocumento").getAsString());
            if (jsonObject.has("NumeroDocumento") && !jsonObject.get("NumeroDocumento").isJsonNull()) {
                consegna.setNumeroDocumento(jsonObject.get("NumeroDocumento").getAsInt());
            }
            consegna.setPagamentoContanti(jsonObject.get("PagamentoContanti").getAsBoolean());
            consegna.setMailVettore(jsonObject.get("MailVettore").getAsString());

            if (jsonObject.has("FlUploaded") && !jsonObject.get("FlUploaded").isJsonNull()) {
                consegna.setFlUploaded(jsonObject.get("FlUploaded").getAsString());
            }else{ consegna.setFlUploaded("N");}
            if (jsonObject.has("Commento") && !jsonObject.get("Commento").isJsonNull()) {
                consegna.setCommento(jsonObject.get("Commento").getAsString());
            }else {consegna.setCommento("");}
            if (jsonObject.has("IdDevice") && !jsonObject.get("IdDevice").isJsonNull()) {
                Log.d("ConsegnaDeserializer", "Deserializing IdDevice");
                consegna.setIdDevice(jsonObject.get("IdDevice").getAsInt());
            }
            if (jsonObject.has("FileName") && !jsonObject.get("FileName").isJsonNull()) {
                consegna.setFileName(jsonObject.get("FileName").getAsString());
            }
            if (jsonObject.has("FileType") && !jsonObject.get("FileType").isJsonNull()) {
                consegna.setFileType(jsonObject.get("FileType").getAsString());
            }
            if (jsonObject.has("FileBase64") && !jsonObject.get("FileBase64").isJsonNull()) {
                consegna.setFileBase64(jsonObject.get("FileBase64").getAsString());
            }

            return consegna;
    } catch (Exception e) {
            Log.e(TAG, "Error during Consegna deserialization", e);
        throw new JsonParseException("Error ConsegnaDeserializer: ", e);
    }
    }
}



