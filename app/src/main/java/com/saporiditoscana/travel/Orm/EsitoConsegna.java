package com.saporiditoscana.travel.Orm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.saporiditoscana.travel.DbHelper.DbManager;

public class EsitoConsegna {
    private static final String TAG = "Giro";

    @SerializedName("id")
    private int id;
    @SerializedName("testo")
    private String testo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    @SuppressLint("Range")
    public  static String GetTesto(Context context, int id){
        StringBuilder result = new StringBuilder();
        Cursor c;

        try {

            String[] parameters = new String[]{
                    String.valueOf(id)
            };

            DbManager dbManager = new DbManager(context);
            c = dbManager.GetCursor("SELECT testo  FROM d_esito_consegna WHERE id = ? ", parameters);

            if (c!= null){
                while (c.moveToNext()){
                    result.append(c.getString(c.getColumnIndex("testo")));
                }
            }
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
        return result.toString();
    }
}
