package com.saporiditoscana.travel.Orm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.saporiditoscana.travel.Logger;
import com.saporiditoscana.travel.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Mail extends BroadcastReceiver {

    public interface Completed{
        void callback(String message, Integer result);
    }

    private static final String TAG = "Mail";
    private Context context;

    public static final String BROADCAST_FILTER = "BROADCAST_FILTER";
    public static final String RESULT = "result";

    @SerializedName("AddressForm")
    private String AddressForm;
    @SerializedName("AddressTo")
    private String AddressTo;
    @SerializedName("AddressCc")
    private String AddressCc;
    @SerializedName("AddressCcn")
    private String AddressCcn;
    @SerializedName("Subject")
    private String Subject;
    @SerializedName("Message")
    private String Message;
    @SerializedName("AttachCollection")
    private List<Attach> AttachCollection;
    @SerializedName("TerminaleId")
    private int TerminaleId;
    @SerializedName("completed")
    private Completed completed;

    public String getAddressForm() {
        return AddressForm;
    }

    public void setAddressForm(String addressForm) {
        AddressForm = addressForm;
    }

    public String getAddressTo() {
        return AddressTo;
    }

    public void setAddressTo(String addressTo) {
        AddressTo = addressTo;
    }

    public String getAddressCc() {
        return AddressCc;
    }

    public void setAddressCc(String addressCc) {
        AddressCc = addressCc;
    }

    public String getAddressCcn() {
        return AddressCcn;
    }

    public void setAddressCcn(String addressCcn) {
        AddressCcn = addressCcn;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public List<Attach> getAttachCollection() {
        return AttachCollection;
    }

    public void setAttachCollection(List<Attach> attachCollection) {
        AttachCollection = attachCollection;
    }

    public int getTerminaleId() {return TerminaleId;}

    public void setTerminaleId(int terminaleId) {TerminaleId = terminaleId;}


    public void AddAttach(Attach a){
        AttachCollection.add(a);
    }

    public Mail(){}

    @Override
    public void onReceive(Context context, Intent intent) {

    }

    public Mail(Context context, Completed completed){
        this.context = context;
        this.completed = completed;
        this.AttachCollection = new ArrayList<Attach>();
    }

    public void SendMail() {
        try {
            final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

            Terminale terminale = new Terminale(context);
            String url = terminale.getApiServerUrl() + "mail/SendMail";

            Gson gson = new Gson();
            Mail m = new Mail();
            m.setTerminaleId(terminale.getId());
            m.setAddressForm(this.AddressForm);
            m.setAddressTo(this.AddressTo);
            m.setAddressCc(this.AddressCc);
            m.setAddressCcn(this.AddressCcn);
            m.setSubject(this.Subject);
            m.setMessage(this.Message+  "</br>" + terminale.getConducente());
            m.setAttachCollection(this.AttachCollection);

            OkHttpClient client = new  OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(mediaType, gson.toJson(m)))
                    .build();
            Response responses = null;

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Logger.d(TAG, "onFailure " + e.getLocalizedMessage());
                    sendMessageToUI(context, "Invio mail non riuscito.", -1);
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();

                        Gson gson = new Gson();
                        final Result result = gson.fromJson(jsonData, Result.class);

                        if (result.Error == null || result.Error == "") {
                            sendMessageToUI(context, "Invio mail avvenuto con successo.", 0);
                        }else{
                            sendMessageToUI(context, "Invio mail non riuscito.", -1);
                        }


                    } catch (Exception e) {
                        Logger.e(TAG, e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
        }
    }

    private  void sendMessageToUI(Context context, String messagge, Integer esito) {
        completed.callback(messagge, esito);
    }
}
