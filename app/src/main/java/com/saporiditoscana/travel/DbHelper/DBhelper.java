package com.saporiditoscana.travel.DbHelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.SyncStateContract;
import android.util.Log;

import com.saporiditoscana.travel.Logger;
import com.saporiditoscana.travel.MainActivity;
import com.saporiditoscana.travel.Orm.Step;
import com.saporiditoscana.travel.Orm.Terminale;


public class DBhelper extends SQLiteOpenHelper {
    private static final String TAG = "DBhelper";
    public static final String DBNAME="TRAVELDB";
    public static final Integer DBVERSION=3;

    public static final String T_TERMINALE = "t_terminale";
    public static final String T_CONSEGNA = "t_consegna";
    public static final String T_GPS = "t_gps";
    public static final String T_GIRO = "t_giro";
    public static final String T_STEP = "t_step";
    public static final String D_ESITO_CONSEGNA = "d_esito_consegna";

    public  Context context;

    public DBhelper(Context context) {
        super(context, DBNAME, null, DBVERSION);
        this.context = context;
//        Log.i(TAG,"DBhelper");
        getWritableDatabase();
    }

    public boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase(DBNAME, null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {

        }
        return checkDB != null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //crea il database se non presente nell'applicazione
//        Log.i(TAG,"onCreate");
        boolean transectionStarted = db.inTransaction();
        if (!transectionStarted) db.beginTransaction();
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS " + T_TERMINALE + " (");
            sql.append("  \"id\" INTEGER PRIMARY KEY NOT NULL,\n");
            sql.append("  \"testo\" nvarchar(250),\n");
            sql.append("  \"ftp_server\" nvarchar(250),\n");
            sql.append("  \"web_server\" nvarchar(250),\n");
            sql.append("  \"id_automezzo\" char(03),\n");
            sql.append("  \"targa\" char(7),\n");
            sql.append("  \"id_conducente\" char(3),\n");
            sql.append("  \"conducente\" nvarchar(128)\n");
            sql.append(");");
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS " + T_GIRO + "(");
            sql.append("  \"cd_dep\" char(03) NOT NULL,");
            sql.append("  \"cd_giro\" char(03) NOT NULL,");
            sql.append("  \"ds_giro\" nvarchar(250) NOT NULL,");
            sql.append("  \"dt_consegna\" nvarchar(250) NOT NULL, ");
            sql.append(" PRIMARY KEY(\"cd_giro\") ");
            sql.append(");");
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS " + T_CONSEGNA + "(");
            sql.append("  \"anno_reg\" INTEGER NOT NULL,");
            sql.append("  \"nr_reg\" integer NOT NULL, ");
            sql.append("  \"numero_documento\" integer NOT NULL, ");
            sql.append("  \"tipo_documento\" nvarchar(250) NOT NULL, ");
            sql.append("  \"cod_cli\" integer NOT NULL, ");
            sql.append("  \"rag_soc\" nvarchar(250) NOT NULL,");
            sql.append("  \"indirizzo\" nvarchar(250) NOT NULL,");
            sql.append("  \"localita\" nvarchar(250) NOT NULL, ");
            sql.append("  \"cod_age\" char(3) NOT NULL, ");
            sql.append("  \"cod_capo_area\" char(3) NOT NULL,");
            sql.append("  \"mail_agente\" nvarchar(250) NOT NULL,");
            sql.append("  \"mail_capo_area\" nvarchar(250) NOT NULL,");
            sql.append("  \"sequenza\" integer, ");
            sql.append("  \"id_esito_consegna\" integer,");
            sql.append("  \"ts_validita\" text,");
            sql.append("  \"fl_inviato\" char(01),");
            sql.append("  \"testo\" nvarchar(250), ");
            sql.append("  \"pagamento_contanti\" integer NOT NULL DEFAULT 0, ");
            sql.append("  \"mail_vettore\" nvarchar(250) NOT NULL DEFAULT '', ");
            sql.append("  \"fl_uploaded\" char(01) NOT NULL DEFAULT 'N' ,");
            sql.append("  \"commento\" nvarchar(250) NOT NULL DEFAULT '', ");
            sql.append(" PRIMARY KEY(\"anno_reg\", \"nr_reg\") ");
            sql.append(");");
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            sql.append(" CREATE TABLE IF NOT EXISTS " + T_GPS +" (");
            sql.append("   \"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,");
            sql.append("   \"ts_validita\" text NOT NULL,");
            sql.append("   \"latitudine\" float NOT NULL,");
            sql.append("   \"longitudine\" float NOT NULL,");
            sql.append("   \"fl_inviato\" char(01) ");
            sql.append(");");
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            sql.append(" CREATE TABLE IF NOT EXISTS " + T_STEP +" (");
            sql.append("   \"id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,");
            sql.append("   \"testo\" nvarchar(250) NOT NULL,");
            sql.append("   \"fl_eseguito\" char(1) NOT NULL,");
            sql.append("   \"ts_validita\" text ");
            sql.append(");");
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            sql.append("CREATE TABLE IF NOT EXISTS \"d_esito_consegna\" ( ");
            sql.append("   \"id\" integer PRIMARY KEY AUTOINCREMENT NOT NULL, ");
            sql.append("   \"testo\" nvarchar(250) ");
            sql.append(")");
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            sql.append("DELETE FROM sqlite_sequence;");
            db.execSQL(sql.toString());

            sql = new StringBuilder();
            sql.append("INSERT INTO sqlite_sequence VALUES('d_esito_consegna',NULL);");
            db.execSQL(sql.toString());

            if (!transectionStarted) db.setTransactionSuccessful();
        }catch (SQLiteException sqle)
        {
            Logger.e(TAG, "one error occurred: " + sqle.getLocalizedMessage());
        }
        finally {
            if (!transectionStarted) db.endTransaction();

            initializeTable(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //per aggiornare il database
        if (tableExists(db,T_CONSEGNA)) {
            if (!fieldExists(db,T_CONSEGNA, "pagamento_contanti"))
                db.execSQL(" ALTER TABLE "+ T_CONSEGNA +" ADD pagamento_contanti INTEGER NOT NULL DEFAULT 0 ");
            if (!fieldExists(db,T_CONSEGNA, "mail_vettore"))
                db.execSQL(" ALTER TABLE "+ T_CONSEGNA +" ADD mail_vettore nvarchar(250) NOT NULL DEFAULT '' ");
            if (!fieldExists(db,T_CONSEGNA, "fl_uploaded"))
                db.execSQL(" ALTER TABLE "+ T_CONSEGNA +" ADD fl_uploaded char(01) NOT NULL DEFAULT 'N' ");
            if (!fieldExists(db,T_CONSEGNA, "commento"))
                db.execSQL(" ALTER TABLE "+ T_CONSEGNA +" ADD commento nvarchar(250) NOT NULL DEFAULT '' ");

        }

    }

    public boolean tableExists(SQLiteDatabase db,  String nomeTabella) {
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.rawQuery(" SELECT * FROM sqlite_master   WHERE type = 'table'     AND name = '" + nomeTabella + "' ", null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                exists = true;
            }
            if (cursor != null) {
                cursor.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }catch (SQLiteException sqle){
            if (cursor != null) cursor.close();

            throw  sqle;
        }
        return exists;
    }

    public boolean fieldExists(SQLiteDatabase db,  String nomeTabella, String nomeCampo) {

        Cursor cursor = null;
        boolean exists = false;
        try {
//            cursor = db.rawQuery(" SELECT sql FROM sqlite_master   WHERE type = 'table'     AND name = '" + nomeTabella + "' ", null);
            cursor = db.rawQuery("PRAGMA table_info("+nomeTabella+")",null);
            if (cursor!= null) {
                try{
                    while (cursor.moveToNext() && !exists) {
                        exists =  cursor.getString(cursor.getColumnIndex("name")).equals(nomeCampo);
                    }
                }
                finally {
                    cursor.close();
                }
            }

        } catch (SQLiteException sqle){
            if (cursor != null) cursor.close();

            throw  sqle;
        }
        return exists;
    }

    public void ResetDataBase(Terminale terminale){
        SQLiteDatabase db = getWritableDatabase();
        try {//http://85.39.149.205

            db.beginTransaction();
            db.execSQL("DROP TABLE IF EXISTS " + T_TERMINALE);
            db.execSQL("DROP TABLE IF EXISTS " + T_CONSEGNA);
            db.execSQL("DROP TABLE IF EXISTS " + T_GPS);
            db.execSQL("DROP TABLE IF EXISTS " + T_GIRO);
            db.execSQL("DROP TABLE IF EXISTS " + T_STEP);
            db.execSQL("DROP TABLE IF EXISTS " + D_ESITO_CONSEGNA);
            onCreate(db);

            if (terminale.getId() >0)
                db.execSQL("INSERT INTO " + T_TERMINALE + "( id , web_server, ftp_server ) VALUES (\'" + String.valueOf(terminale.getId()) + "\' , \'" + terminale.getWebServer() + "\' , \'" +  terminale.getFtpServer() + "\')");

            db.setTransactionSuccessful();
        } catch (SQLiteException sqle) {
//            Log.e(TAG, sqle.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    private void initializeTable(SQLiteDatabase db){
        try {
            for (MainActivity.STATO stato : MainActivity.STATO.values()) {
                Step step = new Step();
                step.setId(stato.getId());
                step.setTesto(stato.toString());
                step.setFlEseguito("N");
                DbQuery dbQuery = Step.Insert(step);
                db.execSQL(dbQuery.getSql(), dbQuery.getParameters());
            }
            db.execSQL("INSERT INTO " + D_ESITO_CONSEGNA + " (id, testo) VALUES (1, \'OK\') ");
            db.execSQL("INSERT INTO " + D_ESITO_CONSEGNA + " (id, testo) VALUES (2, \'MERCE MANCANTE\') ");
            db.execSQL("INSERT INTO " + D_ESITO_CONSEGNA + " (id, testo) VALUES (3, \'MERCE DANNEGGIATA\') ");
            db.execSQL("INSERT INTO " + D_ESITO_CONSEGNA + " (id, testo) VALUES (4, \'ALTRO\') ");
            db.execSQL("INSERT INTO " + D_ESITO_CONSEGNA + " (id, testo) VALUES (5, \'OK NO CLIENTE\') ");
            db.execSQL("INSERT INTO " + D_ESITO_CONSEGNA + " (id, testo) VALUES (6, \'MERCE MANCANTE NO CLIENTE\') ");
            db.execSQL("INSERT INTO " + D_ESITO_CONSEGNA + " (id, testo) VALUES (7, \'MERCE DANNEGGIATA NO CLIENTE\') ");

        }catch (SQLiteException sqle) {
            Step.Delete(this.context);
        }
    }
}
