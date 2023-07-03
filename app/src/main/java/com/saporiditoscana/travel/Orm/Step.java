package com.saporiditoscana.travel.Orm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;

import com.google.gson.annotations.SerializedName;
import com.saporiditoscana.travel.DbHelper.DbManager;
import com.saporiditoscana.travel.DbHelper.DbQuery;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Step {
    private static final String TAG = "Step";
    private Context context;

    @SerializedName("id")
    private int id = -1;
    @SerializedName("testo")
    private String testo= "";
    @SerializedName("flEseguito")
    private String flEseguito = "N";
    @SerializedName("tsValidita")
    private String tsValidita = "";


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

    public String getFlEseguito() {
        return flEseguito;
    }

    public void setFlEseguito(String flEseguito) {
        this.flEseguito = flEseguito;
    }

    public String getTsValidita() {

        return tsValidita;
    }

    public void setTsValidita(String tsValidita) {

        this.tsValidita = tsValidita;
    }

    public static String GetCurrentTimeStamp()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());

    }

    public static  Boolean Insert(Step step, Context context){
        boolean result;
        try{
            StringBuilder sb;
            sb = new StringBuilder();
            sb.append("INSERT INTO t_step (id, testo, fl_eseguito, ts_validita) ");
            sb.append("VALUES (?,?,?,?) ");

            String[] parameters = new String[]{String.valueOf(step.getId()),
                    String.valueOf(step.getTesto()),
                    String.valueOf(step.getFlEseguito()),
                    String.valueOf(step.getTsValidita())};

            DbManager dbManager;
            dbManager = new DbManager(context);
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

    public static DbQuery Insert(Step step){
        DbQuery dbQuery = new DbQuery();
        try{
            StringBuilder sb;
            sb = new StringBuilder();
            sb.append("INSERT INTO t_step (id, testo, fl_eseguito, ts_validita) ");
            sb.append("VALUES (?,?,?,?) ");

            String[] parameters = new String[]{String.valueOf(step.getId()),
                    String.valueOf(step.getTesto()),
                    String.valueOf(step.getFlEseguito()),
                    String.valueOf(step.getTsValidita())};

            dbQuery.setSql(sb.toString());
            dbQuery.setParameters(parameters);
        }
        catch (Exception e)
        {
//            Log.e(TAG, e.getMessage());
        }
        return dbQuery;
    }

    public static  Boolean Update(int Id, Context context){
        boolean result;
        try{
            StringBuilder sb;
            sb = new StringBuilder();
            sb.append("UPDATE t_step SET fl_eseguito = ?, ts_validita = ? ");
            sb.append(" WHERE id = ? ");

            String[] parameters = new String[]{
                    "S",
                    Step.GetCurrentTimeStamp(),
                    String.valueOf(Id)};

            DbManager dbManager;
            dbManager = new DbManager(context);
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
            sb.append("DELETE FROM t_step ");

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

    @SuppressLint("Range")
    public static Step Last(Context context){
        Cursor c = null;
        Step step = new Step();
        try {
            String sb = "SELECT * FROM t_step WHERE ts_validita = (SELECT MAX(ts_validita) FROM t_step) " +
                    "   AND fl_eseguito = 'S' ";

            DbManager dbManager = new DbManager(context);
            c = dbManager.GetCursor(sb, null);


            if (c != null && c.getCount()>0) {
                while (c.moveToNext()) {
                    step.id = c.getInt(c.getColumnIndex("id"));
                    step.testo = c.getString(c.getColumnIndex("testo"));
                    step.flEseguito = c.getString(c.getColumnIndex("fl_eseguito"));
                    step.tsValidita = c.getString(c.getColumnIndex("ts_validita"));
                }
            }else{
                step.id = 0;
            }
            dbManager.close();
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
            c= null;
        }finally {
            if (c != null) {
                c.close();
            }
        }
        return step;
    }

}
