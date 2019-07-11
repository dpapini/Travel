package com.saporiditoscana.travel.Orm;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.saporiditoscana.travel.DbHelper.DbManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.StringJoiner;

public class Giro {
    private static final String TAG = "Giro";
    private Context context;

    @SerializedName("CdDep")
    private String CdDep = new  String("");
    @SerializedName("CdGiro")
    private String CdGiro = new String("");
    @SerializedName("DsGiro")
    private String DsGiro = new String("");
    @SerializedName("DtConsegna")
    private String DtConsegna = new String("");

    public Giro(){}

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
    }

    private Cursor GetGiro(){
        Cursor c;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM t_giro ");

            DbManager dbManager = new DbManager(this.context);
            c = dbManager.GetCursor(sb.toString(), null);
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
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
        Boolean result;
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
        Boolean result;
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
