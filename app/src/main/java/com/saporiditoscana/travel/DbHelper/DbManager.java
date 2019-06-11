package com.saporiditoscana.travel.DbHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.saporiditoscana.travel.Orm.Terminale;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DbManager {
    private DBhelper dBhelper;
    private static final String TAG = "DbManager";

    public  DbManager(Context ctx){
        dBhelper = new DBhelper(ctx);
    }

    public Cursor GetCursor(String sql, String[] parameters){
        SQLiteDatabase db = dBhelper.getReadableDatabase();

        try {
            return db.rawQuery(sql, parameters);
        }catch (SQLiteException sqle){
//            Log.e(TAG, sqle.getMessage());
            return null;
        }
    }

    public boolean checkDataBase() {
      return dBhelper.checkDataBase();
    }

    public void ResetDataBase(Terminale terminale){
          dBhelper.ResetDataBase(terminale);
    }

    public void ExecuteSql(String sql){

        SQLiteDatabase db = dBhelper.getWritableDatabase();

        db.beginTransaction(); //start transaction
        try {
            db.execSQL(sql);
            db.setTransactionSuccessful(); //sett transaction to successfull
        }catch (SQLiteException sqle){
            throw  sqle;
        }
        finally {
            db.endTransaction(); //set end transaction
        }
    }

    public void ExecuteSql(List<DbQuery> lsDbQuery){

        SQLiteDatabase db = dBhelper.getWritableDatabase();

        db.beginTransaction(); //start transaction
        try {
            String sql;
            Object[] parameters;
            for (DbQuery dbQuery:lsDbQuery) {
                sql = dbQuery.getSql();
                if (dbQuery.getParameters().length >0) {
                    parameters = dbQuery.getParameters();
                    db.execSQL(sql, parameters);
                }else db.execSQL(sql);
            }

            db.setTransactionSuccessful(); //sett transaction to successfull
        }catch (SQLiteException sqle){
            throw  sqle;
        }
        finally {
            db.endTransaction(); //set end transaction
        }
    }

    public int ExecuteSql(String sql, Object[] parameters){
        int result = 0;
        SQLiteDatabase db = dBhelper.getWritableDatabase();

        db.beginTransaction(); //start transaction
        try {
            db.execSQL(sql,parameters);

            Cursor res = db.rawQuery("SELECT changes() 'affected_rows'", null);
            res.moveToFirst();
            result = res.getInt(res.getColumnIndex("affected_rows"));

            db.setTransactionSuccessful(); //sett transaction to successfull
        }catch (SQLiteException sqle){
            throw  sqle;
        }
        finally {
            db.endTransaction(); //set end transaction
        }
        return result;
    }

    public boolean Insert(String NomeTabella, ContentValues Values)
    {
        SQLiteDatabase db = dBhelper.getWritableDatabase();

        db.beginTransaction(); //start transaction
        try {
            db.insert(NomeTabella, null, Values);
            db.setTransactionSuccessful(); //sett transaction to successfull
            return true;
        }catch (SQLiteException sqle){
            return false;
        }
        finally {
            db.endTransaction(); //set end transaction
        }
    }

    public void close() {
        dBhelper.close();
    }

}
