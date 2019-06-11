package com.saporiditoscana.travel.Orm;


import android.database.Cursor;
import android.content.Context;
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.saporiditoscana.travel.DbHelper.DbManager;

public class Terminale {
    private static final String TAG = "Terminale";
    private Context context;

    @SerializedName("id")
    private int id = -1;
    @SerializedName("testo")
    private String testo= new  String("");
    @SerializedName("ftpServer")
    private String ftpServer = new String("");
    @SerializedName("webServer")
    private String webServer = new String("");
    @SerializedName("targa")
    private String targa = new String("");
    @SerializedName("conducente")
    private String conducente = new String("");
    @SerializedName("idConducente")
    private String idConducente = new String("");
    @SerializedName("idAutomezzo")
    private String idAutomezzo = new String("");


    public String getConducente() {
        return conducente;
    }

    public void setConducente(String conducente) {
        this.conducente = conducente;
    }

    public Terminale(Context context) {
        this.context = context;
        Cursor c = GetTerminale();

        try {
            if (c!= null) {
                while (c.moveToNext()) {
                    this.id = c.getInt(c.getColumnIndex("id"));
                    this.testo = c.getString(c.getColumnIndex("testo"));
                    this.ftpServer = c.getString(c.getColumnIndex("ftp_server"));
                    this.webServer = c.getString(c.getColumnIndex("web_server"));
                    this.targa = c.getString(c.getColumnIndex("targa"));
                    this.conducente = c.getString(c.getColumnIndex("conducente"));
                    this.idAutomezzo = c.getString(c.getColumnIndex("id_automezzo"));
                    this.idConducente = c.getString(c.getColumnIndex("id_conducente"));
                }
            }
        }catch (Exception e)
        {
//            Log.e(TAG, e.getMessage());

        }
    }

    public Terminale(Context context, int id, String testo) {
        this.id = id;
        this.testo = testo;
    }

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

    public String getFtpServer() {
        return ftpServer;
    }

    public String getFtpServerUrl() {
        return ftpServer.toLowerCase().replace("http://","");
    }

    public void setFtpServer(String ftpServer) {
        this.ftpServer = ftpServer;
    }

    public String getWebServer() {
        return webServer;
    }

    public String getWebServerUrlErgon() {
        //http://85.39.149.205/ErgonService/ServiceErgon.svc/
        return webServer + "/ErgonService/ServiceErgon.svc/";
    }

    public String getWebServerUrlMvc() {
        //http://85.39.149.205/ErgonService/ServiceErgon.svc/
        return webServer + "/MvcService/ErpService.svc/";
    }

    public String getWebServerUrlTest() {
        return "http://10.0.127.12/ServiceErgon/ServiceErgon.svc/";
    }

    public String getApiServerUrl() {
        return webServer + ":8080/api/";
    }

    public void setWebServer(String webServer) {
        this.webServer = webServer;
    }

    public String getTarga() {
        return targa;
    }

    public void setTarga(String targa) {
        this.targa = targa;
    }

    public String getIdConducente() { return idConducente; }

    public void setIdConducente(String idConducente) {
        this.idConducente = idConducente;
    }

    public String getIdAutomezzo() {
        return idAutomezzo;
    }

    public void setIdAutomezzo(String idAutomezzo) {
        this.idAutomezzo = idAutomezzo;
    }

    private Cursor GetTerminale(){
        Cursor c;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM t_terminale ");

            DbManager dbManager = new DbManager(this.context);
            c = dbManager.GetCursor(sb.toString(), null);
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
            c= null;
        }
        return  c;
    }

    public  Boolean InsertTerminale(int id){
        Boolean result;
        try{
            StringBuilder sb;
            sb = new StringBuilder();
            sb.append("INSERT INTO t_terminale (id) ");
            sb.append("VALUES (?) ");

            String[] parameters = new String[]{String.valueOf(id)};

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

    //aggiorna le informazioni del terminale
    public  Boolean UpdateTerminale(Terminale terminale){
        Boolean result;
        try{
            StringBuilder sb;
            sb = new StringBuilder();
            sb.append("UPDATE t_terminale");
            sb.append("   SET testo = ?");
            sb.append("     , ftp_server = ?");
            sb.append("     , web_server = ?");
            sb.append("     , targa = ?");
            sb.append("     , conducente = ?");
//            sb.append("     , id_automezzo = ?");
            sb.append("     , id_conducente = ?");
            sb.append(" WHERE id = ?");

            String[] parameters = new String[]{
            String.valueOf(terminale.testo),
            String.valueOf(terminale.ftpServer),
            String.valueOf(terminale.webServer),
            String.valueOf(terminale.targa),
            String.valueOf(terminale.conducente),
//            String.valueOf(terminale.idAutomezzo),
            String.valueOf(terminale.idConducente),
            String.valueOf(terminale.id)};

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


    //Pulisce le informazioni del terminale
    public  Boolean DeleteTerminale(Terminale terminale){
        Boolean result;
        try{
            StringBuilder sb;
            sb = new StringBuilder();
            sb.append("UPDATE terminale");
            sb.append("   SET testo = ?");
            sb.append("     , ftp_server = ?");
            sb.append("     , web_server = ?");
            sb.append("     , targa = ?");
            sb.append("     , conducente = ?");
//            sb.append("     , id_automezzo = ?");
            sb.append("     , id_conducente = ?");
            sb.append(" WHERE id = ?");

            String[] parameters = new String[]{
            String.valueOf(""),
            String.valueOf(""),
            String.valueOf(""),
            String.valueOf(""),
            String.valueOf(""),
            String.valueOf(""),
            String.valueOf(terminale.id)};

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
}
