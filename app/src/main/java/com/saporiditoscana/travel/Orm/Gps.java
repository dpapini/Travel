package com.saporiditoscana.travel.Orm;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import com.saporiditoscana.travel.DbHelper.DbManager;
import com.saporiditoscana.travel.Result;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Gps {
    private static final String TAG = "Giro";
    private Context context;

    @SerializedName("id")
    private int id;
    @SerializedName("tsValidita")
    private String tsValidita = new  String("");
    @SerializedName("latitudine")
    private String latitudine = new String("");
    @SerializedName("longitudine")
    private String longitudine = new String("");

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    public String getTsValidita() {
        return tsValidita;
    }

    public void setTsValidita(String tsValidita) {
        this.tsValidita = tsValidita;
    }

    public void setTsValidita(Long dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.tsValidita =   dateFormat.format(new Timestamp(dateTime));
    }

    public String getLatitudine() {
        return latitudine;
    }

    public void setLatitudine(String latitudine) {
        this.latitudine = latitudine;
    }

    public String getLongitudine() {
        return longitudine;
    }

    public void setLongitudine(String longitudine) {
        this.longitudine = longitudine;
    }

    public static String GetCurrentTimeStamp()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());

    }

    public static String GetCurrentTimeStamp2JsonPrimitive()
    {
//        return  "/Date(" + new Date().getTime() + ")/";

        Calendar calendar = Calendar.getInstance();
        //get timezone
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Rome");
        //set timezone
        calendar.setTimeZone(timeZone);
        //get offset od timezone
        int offsetInMillis = timeZone.getOffset(calendar.getTimeInMillis());

        String offset = String.format("%02d%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
        offset = (offsetInMillis >= 0 ? "+" : "-") + offset;

        return "/Date(" + calendar.getTimeInMillis() + offset + ")/";
    }

    public Gps(){}

    public static  Boolean Insert(Gps gps, Context context){
        Log.d(TAG,"Insert gps");
        Boolean result;
        try{
            StringBuilder sb;

            sb = new StringBuilder();
            sb.append("INSERT INTO t_gps (ts_validita, latitudine, longitudine, fl_inviato) ");
            sb.append("VALUES (?,?,?,?) ");

            String[] parameters = new String[]{ String.valueOf(gps.getTsValidita()).isEmpty() ? String.valueOf(Gps.GetCurrentTimeStamp()): String.valueOf(gps.getTsValidita()),
                    String.valueOf(gps.getLatitudine()),
                    String.valueOf(gps.getLongitudine()),
                    String.valueOf('N'),
            };

            DbManager dbManager;
            dbManager = new DbManager(context);
            dbManager.ExecuteSql(sb.toString(),parameters);

            result = true;
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
            result=false;
        }
        return  result;
    }

    public static  Boolean Update(Gps gps, Context context){
//        Log.d(TAG,"Insert gps");
        Boolean result;
        try{
            StringBuilder sb;

            sb = new StringBuilder();
            sb.append("UPDATE t_gps SET fl_inviato = ? ");
            sb.append("WHERE id = ? ");

            String[] parameters = new String[]{String.valueOf('S'),
                    String.valueOf(gps.getId()),
            };

            DbManager dbManager;
            dbManager = new DbManager(context);
            dbManager.ExecuteSql(sb.toString(),parameters);

            result = true;
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
            result=false;
        }
        return  result;
    }

    public static Cursor GetDati(Context context){
        Cursor c;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM t_gps WHERE IFNULL(fl_inviato,'N') <> 'S' ORDER BY ts_validita ");

            DbManager dbManager = new DbManager(context);
            c = dbManager.GetCursor(sb.toString(), null);
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            c= null;
        }
        return  c;
    }

    public static List<Gps> GetLista (Context context)
    {
        List<Gps> gpsList = new ArrayList<Gps>();
        Cursor c = Gps.GetDati(context);
        try {
            if (c!= null) {
                while (c.moveToNext()) {
                    Gps gps = new Gps();
                    gps.setId(c.getInt(c.getColumnIndex("id")));
                    gps.setTsValidita(c.getString(c.getColumnIndex("ts_validita")));
                    gps.setLatitudine(c.getString(c.getColumnIndex("latitudine")));
                    gps.setLongitudine(c.getString(c.getColumnIndex("longitudine")));

                    gpsList.add(gps);
                }
            }
        }catch (Exception e)
        {
            Log.e(TAG, e.getMessage());

        }
        return gpsList;
    }

    public static void InsertGps(final Gps gps, final Context context) {
        try {
            final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

            Terminale terminale = new Terminale(context);
            Giro giro = new Giro(context);
            String url = terminale.getWebServerUrlErgon() + "InsertGps";

            JsonObject json = new JsonObject();
            json.addProperty("IdConducente", terminale.getIdConducente());
            json.addProperty("DtConsegna", giro.getDtConsegnaddMMyyyy());
            json.addProperty("CdGiro", giro.getCdGiro());
            json.addProperty("Latitudine", gps.getLatitudine());
            json.addProperty("Longitudine",gps.getLongitudine());
            json.addProperty("TsValidita", gps.getTsValidita());
            json.addProperty("IdDevice", terminale.getId());//id del device

            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            urlBuilder.addQueryParameter("EditJson", json.toString());
            url = urlBuilder.build().toString();


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response responses = null;

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();

                        Gson gson = new Gson();
                        final Result result = gson.fromJson(jsonData, Result.class);

                        if (result.Error == null || result.Error == "") {
                            Gps.Update(gps,context);
                        }else{
                            throw new Exception("Aggiornamento fallito");
                        }
                    } catch (Exception e) {
//                        Log.e(TAG, e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
        }
    }


}
