package com.saporiditoscana.travel.DbHelper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.saporiditoscana.travel.Orm.Terminale;

import java.util.List;

public class DbManager {
    private final DBhelper dBhelper;

    public  DbManager(Context ctx){
        dBhelper = new DBhelper(ctx);
    }

    public Cursor GetCursor(String sql, String[] parameters){
        SQLiteDatabase db = null;

        try {
            db = dBhelper.getReadableDatabase();
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

        SQLiteDatabase db = null;

        try {
            db=dBhelper.getWritableDatabase();
            db.beginTransaction(); //start transaction

            db.execSQL(sql);
            db.setTransactionSuccessful(); //sett transaction to successfull
        }catch (SQLiteException sqle){
            throw  sqle;
        }
        finally {
            if (db!=null) {
                db.endTransaction(); //set end transaction
                db.close();
            }
        }
    }

    public void ExecuteSql(List<DbQuery> lsDbQuery){

        SQLiteDatabase db = null;
        try {
            db= dBhelper.getWritableDatabase();
            db.beginTransaction(); //start transaction

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
            if(db!=null) {
                db.endTransaction(); //set end transaction
                db.close();
            }
        }
    }

    @SuppressLint("Range")
    public int ExecuteSql(String sql, Object[] parameters){
        int result = 0;
        Cursor res=null;
        SQLiteDatabase db = null;
        try {
            db=dBhelper.getWritableDatabase();
            db.beginTransaction(); //start transaction

            db.execSQL(sql,parameters);

            res = db.rawQuery("SELECT changes() 'affected_rows'", null);
            res.moveToFirst();
            result = res.getInt(res.getColumnIndex("affected_rows"));

            db.setTransactionSuccessful(); //sett transaction to successfull
        }catch (SQLiteException sqle){
            throw  sqle;
        }
        finally {
            if(db!=null) {
                db.endTransaction(); //set end transaction
                db.close();
            }
            if (res!=null){
                res.close();
            }
        }
        return result;
    }

    public boolean Insert(String NomeTabella, ContentValues Values)
    {
        SQLiteDatabase db =null;
        try {
            db=dBhelper.getWritableDatabase();
            db.beginTransaction(); //start transaction

            db.insert(NomeTabella, null, Values);
            db.setTransactionSuccessful(); //sett transaction to successfull
            return true;
        }catch (SQLiteException sqle){
            return false;
        }
        finally {
            if(db!=null) {
                db.endTransaction(); //set end transaction
                db.close();
            }
        }
    }

    public void close() {
        dBhelper.close();
    }

}
