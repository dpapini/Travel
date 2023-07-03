package com.saporiditoscana.travel.Orm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.saporiditoscana.travel.DbHelper.DbManager;
import android.util.Log;
import com.saporiditoscana.travel.Result;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Giro {
    private static final String TAG = "Giro";
    private Context context;

    @SerializedName("CdDep")
    private String CdDep = "";
    @SerializedName("CdGiro")
    private String CdGiro = "";
    @SerializedName("DsGiro")
    private String DsGiro = "";
    @SerializedName("DtConsegna")
    private String DtConsegna = "";

    public Giro(){}

    @SuppressLint("Range")
    public Giro(Context context){
        this.context = context;
        Cursor c = GetGiro();

        try {
            if (c!= null) {
                while (c.moveToNext()) {
                    this.CdDep = c.getString(c.getColumnIndex("cd_dep"));
                    this.CdGiro = c.getString(c.getColumnIndex("cd_giro"));
                    this.DsGiro = c.getString(c.getColumnIndex("ds_giro"));
                    this.DtConsegna = c.getString(c.getColumnIndex("dt_consegna"));
                }
            }
        }catch (Exception e)
        {
//            Log.e(TAG, e.getMessage());
        }
        finally {
            if (c!=null){
                c.close();
            }
        }
    }

    private Cursor GetGiro(){
        Cursor c;
        try {

            DbManager dbManager = new DbManager(this.context);
            c = dbManager.GetCursor("SELECT * FROM t_giro ", null);
        }catch (Exception e){
            c= null;
        }
        return  c;
    }

    public String getDsGiro() {
        return DsGiro;
    }

    public void setDsGiro(String dsGiro) {
        this.DsGiro = dsGiro;
    }

    public String getDtConsegna() {
        return DtConsegna;
    }

    public String getDtConsegnaFormatted()
    {
        Date d = new Date(Long.parseLong(DtConsegna.replaceAll("[^\\d.]", "")));
        String myFormat = "dd MMMM yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ITALIAN);

        return sdf.format(d);
    }

    public String getDtConsegnaddMMyyyy()
    {
        Date d = new Date(Long.parseLong(DtConsegna.replaceAll("[^\\d.]", "")));
        String myFormat = "yyyy/MM/dd hh:mm:ss"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ITALIAN);

        return sdf.format(d);
    }

    public String getDtConsegnayyyyMMdd()
    {
        Date d = new Date(Long.parseLong(DtConsegna.replaceAll("[^\\d.]", "")));
        String myFormat = "yyyyMMdd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ITALIAN);

        return sdf.format(d);
    }
    public void setDtConsegna(String dtConsegna) {
        this.DtConsegna = dtConsegna;
    }

    public String getCdDep() {
        return CdDep;
    }

    public void setCdDep(String cdDep) {
        this.CdDep = cdDep;
    }

    public String getCdGiro() {
        return CdGiro;
    }

    public void setCdGiro(String cdGiro) {
        this.CdGiro = cdGiro;
    }

    public static  Boolean Insert(Giro giro, Context context){
        boolean result;
        try{
            DbManager dbManager;
            dbManager = new DbManager(context);

            StringBuilder sb;
            sb = new StringBuilder();
            sb.append("DELETE FROM t_giro ");
            dbManager.ExecuteSql(sb.toString());

            sb = new StringBuilder();
            sb.append("INSERT INTO t_giro (cd_dep, cd_giro, ds_giro, dt_consegna) ");
            sb.append("VALUES (?,?,?,?) ");

            String[] parameters = new String[]{String.valueOf(giro.getCdDep()),
                    String.valueOf(giro.getCdGiro()),
                    String.valueOf(giro.getDsGiro()),
                    String.valueOf(giro.getDtConsegna())};

            dbManager.ExecuteSql(sb.toString(),parameters);

            result = true;
        }
        catch (Exception e)
        {
//            Log.e(TAG, e.getMessage());
            result=false;
        }
        return  result;
    }


    public static  Boolean Delete(Context context){
        boolean result;
        try{
            StringBuilder sb;
            sb = new StringBuilder();
            sb.append("DELETE FROM t_giro ");

            DbManager dbManager;
            dbManager = new DbManager(context);
            dbManager.ExecuteSql(sb.toString());

            result = true;
        }
        catch (Exception e)
        {
//            Log.e(TAG, e.getMessage());
            result=false;
        }
        return  result;
    }

    public static void UpdateEndGiro(final Giro giro, final Context context) {
        try {
            Log.d(TAG, "UpdateEndGiro");
            final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

            Terminale terminale = new Terminale(context);
            String url = terminale.getWebServerUrlErgon() + "EndGiroConsegna";

            JsonObject json = new JsonObject();
            json.addProperty("CdDep", giro.getCdDep());
            json.addProperty("CdGiro", giro.getCdGiro());
            json.addProperty("DtConsegna", giro.getDtConsegnaddMMyyyy());
            json.addProperty("TsEnd", Gps.GetCurrentTimeStamp());

            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            urlBuilder.addQueryParameter("EditJson", json.toString());
            url = urlBuilder.build().toString();


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: " + e.getLocalizedMessage());
                }

                @Override
                public void onResponse(Call call, final Response response) {
                    try {
                        String jsonData = response.body().string();

                        Gson gson = new Gson();
                        final Result result = gson.fromJson(jsonData, Result.class);

                        if (result.Error != null)
                            Log.e(TAG, result.getError().toString());

                    } catch (Exception e) {
                        Log.e(TAG, "one error occurred: " + e.getLocalizedMessage());
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "one error occurred: " + e.getLocalizedMessage());
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Giro.class.getSimpleName() + "[", "]")
                .add("CdDep='" + CdDep + "'")
                .add("CdGiro='" + CdGiro + "'")
                .add("DsGiro='" + DsGiro + "'")
                .add("DtConsegna='" + DtConsegna + "'")
                .toString();
    }
}


