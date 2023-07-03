package com.saporiditoscana.travel.Orm;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class ConsegnaSerializer implements JsonSerializer<Consegna> {

    @Override
    public JsonElement serialize(Consegna consegna, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("AnnoReg", consegna.getAnnoReg());
        jsonObject.addProperty("NrReg", consegna.getNrReg());
        jsonObject.addProperty("CdCli", consegna.getCdCli());
        jsonObject.addProperty("RagioneSociale", consegna.getRagioneSociale());
        jsonObject.addProperty("Indirizzo", consegna.getIndirizzo());
        jsonObject.addProperty("Localita", consegna.getLocalita());
        jsonObject.addProperty("CdAge", consegna.getCdAge());
        jsonObject.addProperty("CdCapoArea", consegna.getCdCapoArea());
        jsonObject.addProperty("MailAge", consegna.getMailAge());
        jsonObject.addProperty("MailCapoArea", consegna.getMailCapoArea());
        jsonObject.addProperty("Sequenza", consegna.getSequenza());
        jsonObject.addProperty("IdEsitoConsegna", consegna.getIdEsitoConsegna());
        jsonObject.addProperty("TsValidita", consegna.getTsValidita());
        jsonObject.addProperty("flInviato", consegna.getFlInviato());
        jsonObject.addProperty("Testo", consegna.getTesto());
        jsonObject.addProperty("TipoDocumento", consegna.getTipoDocumento());
        jsonObject.addProperty("NumeroDocumento", consegna.getNumeroDocumento());
        jsonObject.addProperty("PagamentoContanti", consegna.isPagamentoContanti());
        jsonObject.addProperty("MailVettore", consegna.getMailVettore());
        jsonObject.addProperty("FlUploaded", consegna.getFlUploaded());
        jsonObject.addProperty("Commento", consegna.getCommento());
        jsonObject.addProperty("IdDevice", consegna.getIdDevice());
        jsonObject.addProperty("FileName", consegna.getFileName());
        jsonObject.addProperty("FileType", consegna.getFileType());
        jsonObject.addProperty("FileBase64", consegna.getFileBase64());

        return jsonObject;
    }
}
