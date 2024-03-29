package com.saporiditoscana.travel.Orm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.saporiditoscana.travel.DbHelper.DbManager;
import com.saporiditoscana.travel.DbHelper.DbQuery;
import com.saporiditoscana.travel.Result;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Consegna  implements Serializable {
    private static final String TAG = "MainActivity";
    private Context context;

    @SerializedName("AnnoReg")
    private int AnnoReg;
    @SerializedName("NrReg")
    private int NrReg;
    @SerializedName("CdCli")
    private int CdCli;
    @SerializedName("RagioneSociale")
    private String RagioneSociale;
    @SerializedName("Indirizzo")
    private String Indirizzo;
    @SerializedName("Localita")
    private String Localita;
    @SerializedName("CdAge")
    private String CdAge;
    @SerializedName("CdCapoArea")
    private String CdCapoArea;
    @SerializedName("MailAge")
    private String MailAge;
    @SerializedName("MailCapoArea")
    private String  MailCapoArea;
    @SerializedName("Sequenza")
    private String Sequenza;
    @SerializedName("IdEsitoConsegna")
    private int IdEsitoConsegna;
    @SerializedName("TsValidita")
    private String TsValidita;
    @SerializedName("flInviato")
    private String flInviato;
    @SerializedName("Testo")
    private String testo;
    @SerializedName("TipoDocumento")
    private String TipoDocumento;
    @SerializedName("NumeroDocumento")
    private Integer NumeroDocumento;
    @SerializedName("PagamentoContanti")
    private Boolean PagamentoContanti;
    @SerializedName("MailVettore")
    private String MailVettore;
    @SerializedName("FlUploaded")
    private String FlUploaded;
    @SerializedName("Commento")
    private String Commento;
    @SerializedName("IdDevice")
    private int IdDevice;
    @SerializedName("FileName")
    private String FileName;
    @SerializedName("FileType")
    private String FileType;
    @SerializedName("FileBase64")
    private String FileBase64;

    public Consegna(){}

    public Consegna(Context context){
        this.context = context;
    }

    @SuppressLint("Range")
    public Consegna(int AnnoReg, int NrReg, Context context){
        this.context = context;
        Cursor c = getConsegna(AnnoReg,NrReg);

        try {
            if (c!= null) {
                while (c.moveToNext()) {
                    this.AnnoReg = c.getInt(c.getColumnIndex("anno_reg"));
                    this.NrReg = c.getInt(c.getColumnIndex("nr_reg"));
                    this.CdCli = c.getInt(c.getColumnIndex("cod_cli"));
                    this.RagioneSociale = c.getString(c.getColumnIndex("rag_soc"));
                    this.Indirizzo = c.getString(c.getColumnIndex("indirizzo"));
                    this.Localita = c.getString(c.getColumnIndex("localita"));
                    this.CdAge = c.getString(c.getColumnIndex("cod_age"));
                    this.CdCapoArea =c.getString(c.getColumnIndex("cod_capo_area"));
                    this.MailAge = c.getString(c.getColumnIndex("mail_agente"));
                    this.MailCapoArea = c.getString(c.getColumnIndex("mail_capo_area"));
                    this.IdEsitoConsegna = c.getInt(c.getColumnIndex("id_esito_consegna"));
                    this.TsValidita = c.getString(c.getColumnIndex("ts_validita"));
                    this.flInviato = c.getString(c.getColumnIndex("flInviato"));
                    this.testo = c.getString(c.getColumnIndex("testo"));
                    this.Sequenza = c.getString(c.getColumnIndex("sequenza"));
                    this.NumeroDocumento = c.getInt(c.getColumnIndex("numero_documento"));
                    this.TipoDocumento = c.getString(c.getColumnIndex("tipo_documento"));
                    this.PagamentoContanti =    c.getInt(c.getColumnIndex("pagamento_contanti"))==1; //TRUE
                    this.MailVettore = c.getString(c.getColumnIndex("mail_vettore"));
                    this.FlUploaded = c.getString(c.getColumnIndex("fl_uploaded"));
                    this.Commento = c.getString(c.getColumnIndex("commento"));
                    this.FileName = c.getString(c.getColumnIndex("file_name"));
                    this.FileType = c.getString(c.getColumnIndex("file_type"));
                    this.FileBase64 = c.getString(c.getColumnIndex("file_base64"));
                }
            }
        }catch (Exception e)        {
            Log.e(TAG, e.getMessage());        }
        finally {
            if (c!=null){
                c.close();
            }
        }
    }

    public static Cursor GetDati(Context context){
        Cursor c;
        try {
            DbManager dbManager = new DbManager(context);
            c = dbManager.GetCursor("SELECT * FROM t_consegna WHERE cod_cli > 0 ORDER BY sequenza ", null);
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
            c= null;
        }
        return  c;
    }

    public static Cursor GetDatiToUpload(Context context){
        Cursor c;
        try {

            DbManager dbManager = new DbManager(context);
            c = dbManager.GetCursor("SELECT * FROM t_consegna WHERE cod_cli > 0 AND fl_uploaded <> 'S' ORDER BY sequenza ", null);
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
            c= null;
        }
        return  c;
    }

    private Cursor getConsegna(int ignoredAnnoReg, int ignoredNrReg)
    {
        Cursor c;
        try {
            String sb = "SELECT * FROM t_consegna " +
                    " WHERE anno_reg = ? " +
                    "   AND nr_reg = ? ";

            String[] parameters = new String[]{
                    String.valueOf(this.AnnoReg),
                    String.valueOf(this.NrReg)};

            DbManager dbManager = new DbManager(this.context);
            c = dbManager.GetCursor(sb, parameters);
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
            c= null;
        }
        return  c;
    }

    public int getAnnoReg() {
        return AnnoReg;
    }

    public void setAnnoReg(int annoReg) {
        this.AnnoReg = annoReg;
    }

    public int getNrReg() {
        return NrReg;
    }

    public void setNrReg(int nrReg) {
        this.NrReg = nrReg;
    }

    public int getCdCli() {
        return CdCli;
    }

    public String getCliente(){
        return CdCli + " - " + RagioneSociale.trim();
    }

    public void setCdCli(int cdCli) {
        this.CdCli = cdCli;
    }

    public String getRagioneSociale() {
        return RagioneSociale;
    }

    public void setRagioneSociale(String ragioneSociale) {
        this.RagioneSociale = ragioneSociale;
    }

    public String getIndirizzo() {
        return Indirizzo;
    }

    public void setIndirizzo(String indirizzo) {
        this.Indirizzo = indirizzo;
    }

    public String getLocalita() {
        return Localita;
    }

    public void setLocalita(String localita) {
        this.Localita = localita;
    }

    public String getCdAge() {
        return CdAge;
    }

    public void setCdAge(String cdAge) {
        this.CdAge = cdAge;
    }

    public String getCdCapoArea() {
        return CdCapoArea;
    }

    public void setCdCapoArea(String cdCapoArea) {
        this.CdCapoArea = cdCapoArea;
    }

    public String getMailAge() {
        return MailAge + ";";
    }

    public void setMailAge(String mailAge) {
        this.MailAge = mailAge;
    }

    public String getMailCapoArea() {return MailCapoArea + ";";}

    public void setMailCapoArea(String mailCapoArea) {
        this.MailCapoArea = mailCapoArea;
    }

    public int getIdEsitoConsegna() {
        return IdEsitoConsegna;
    }

    public void setIdEsitoConsegna(int idEsitoConsegna) {
        this.IdEsitoConsegna = idEsitoConsegna;
    }

    public String getSequenza() {
        return Sequenza;
    }

    public void setSequenza(String sequenza) {
        Sequenza = sequenza;
    }

    public String getTesto() {return testo;}

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public String getFlInviato() {return flInviato; }

    public void setFlInviato(String flInviato) {this.flInviato = flInviato;}

    public String getTsValidita() {return TsValidita;}

    public String getTsValiditaFromJsonPrimitive() {
        Date d = new Date(Long.parseLong(TsValidita.replaceAll("[^\\d.]", "")));
        String myFormat = "yyyy/MM/dd hh:mm:ss"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ITALIAN);

        return sdf.format(d);
    }

    public void setTsValidita(String tsValidita) {this.TsValidita = tsValidita;}

    public Integer getNumeroDocumento() {
        return NumeroDocumento;
    }

    public void setNumeroDocumento(Integer numeroDocumento) {
        NumeroDocumento = numeroDocumento;
    }

    public String getTipoDocumento() {
        return TipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        TipoDocumento = tipoDocumento;
    }

    public Boolean getPagamentoContanti() {return PagamentoContanti;}

    public void setPagamentoContanti(Boolean pagamentoContanti) {PagamentoContanti = pagamentoContanti;}

    public String getMailVettore() {return MailVettore+ ";";}

    public void setMailVettore(String mailVettore) {this.MailVettore = mailVettore;}

    public String getFlUploaded() {return FlUploaded;}

    public void setFlUploaded(String flUploaded) {this.FlUploaded = flUploaded;}

    public String getCommento() {return Commento;}

    public void setCommento(String commento) {this.Commento = commento;}

    public int getIdDevice() {return IdDevice;}

    public void setIdDevice(int idDevice) {IdDevice = idDevice;  }

    public String getFileName() {return FileName;}

    public void setFileName(String fileName) {this.FileName = fileName;}

    public String getFileType() {return FileType;}

    public void setFileType(String fileType) {this.FileType = fileType;}

    public String getFileBase64() {return FileBase64;}

    public void setFileBase64(String fileBase64) {this.FileBase64 = fileBase64;}

    public static DbQuery Insert(int AnnoReg, int NrReg){

        Log.d(TAG, "Insert " + AnnoReg  );
        Log.d(TAG, "Insert " + NrReg  );
        DbQuery dbQuery = new DbQuery();
        try{
            StringBuilder sb;

            sb = new StringBuilder();
            sb.append("INSERT INTO t_consegna (anno_reg, nr_reg, numero_documento, tipo_documento, cod_cli, rag_soc, indirizzo, localita, cod_age, cod_capo_area, mail_agente, mail_capo_area, pagamento_contanti, mail_vettore, commento, file_name, file_type, file_base64)");
            sb.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
            dbQuery.setSql(sb.toString());
            String[] parameters = new String[]{String.valueOf(AnnoReg)
                    , String.valueOf(NrReg)
                    , String.valueOf(-1) //numerodocumento
                    , ("") //tipodocumento
                    , String.valueOf(-1) //codcli
                    , ("") //ragsoc
                    , ("") //Indirizzo
                    , ("") //Localita
                    , ("") //cod_age
                    , ("") //cod_capo_area
                    , ("") //mail_agente
                    , ("") //mail_capo_area
                    , String.valueOf(0)  //pagamento_contanti
                    , ("") //mailvettore
                    , ("") //commento
                    , ("") //file_name
                    , ("") //file_type
                    , ("") //file_base64
            };
            dbQuery.setParameters(parameters);

        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
        return dbQuery;
    }

    public static boolean Check(Context context){
        boolean result = false;
        try{
            DbManager dbManager;
            dbManager = new DbManager(context);

            StringBuilder sb;
            sb = new StringBuilder();
            sb.append("SELECT * FROM t_consegna");
            Cursor c = dbManager.GetCursor(sb.toString(), null);
            if (c!= null) {
                if (c.getCount()==0)
                    return false; // non è stata caricata nessuna lista
            } else return false;

            sb = new StringBuilder();
            sb.append("SELECT * FROM t_consegna");
            sb.append(" WHERE cod_cli < 0 ");

            c = dbManager.GetCursor(sb.toString(), null);
            if (c!= null) {
                if (c.getCount()==0)
                    result = true;
            }
        }
        catch (Exception e)
        {
//            Log.e(TAG, e.getMessage());
        }
        return result;
    }


    public static boolean Update(Consegna consegna, Context context, String TsValidita){
        boolean result;
        try{

            int rows = getResultExecuteUpdate(consegna, context, TsValidita);

            if (rows==0) result = false;
            else {
                result = true;
                consegna.setTsValidita(TsValidita);
            }
        }
        catch (Exception e)
        {
            result = false;
            Log.e(TAG,e.getLocalizedMessage(), e);
        }
        return result;
    }

    public static Boolean Update(Consegna consegna, Context context){
        boolean result;
        try{
            String tsCurrent = Gps.GetCurrentTimeStamp2JsonPrimitive(); //Gps.GetCurrentTimeStamp();

            int rows = getResultExecuteUpdate(consegna, context, tsCurrent);

            if (rows==0) result = false;
            else {
                result = true;
                consegna.setTsValidita(tsCurrent);
            }
        }
        catch (Exception e)
        {
            result = false;
            Log.e(TAG,e.getLocalizedMessage(), e);
        }
        return result;
    }

    private static int getResultExecuteUpdate(Consegna consegna, Context context, String tsCurrent) {
        try {
            StringBuilder sb;
            sb = new StringBuilder();
            sb.append("UPDATE t_consegna SET cod_cli = ? ");
            sb.append("     , rag_soc = ? ");
            sb.append("     , Indirizzo = ? ");
            sb.append("     , Localita = ? ");
            sb.append("     , cod_age = ? ");
            sb.append("     , cod_capo_area = ? ");
            sb.append("     , mail_agente = ? ");
            sb.append("     , mail_capo_area = ? ");
            sb.append("     , sequenza = ? ");
            sb.append("     , numero_documento = ? ");
            sb.append("     , tipo_documento = ? ");
            sb.append("     , id_esito_consegna = ? ");
            sb.append("     , ts_validita = ? ");
            sb.append("     , fl_inviato = ? ");
            sb.append("     , testo = ? ");
            sb.append("     , pagamento_contanti = ? ");
            sb.append("     , mail_vettore = ? ");
            sb.append("     , fl_uploaded = ? ");
            sb.append("     , commento = ? ");
            sb.append("     , file_name = ? ");
            sb.append("     , file_type = ? ");
            sb.append("     , file_base64 = ? ");

            sb.append(" WHERE anno_reg = ? ");
            sb.append("   AND nr_reg = ? ");

            String[] parameters = new String[]{
                    String.valueOf(consegna.getCdCli())
                    , String.valueOf(consegna.getRagioneSociale())
                    , String.valueOf(consegna.getIndirizzo())
                    , String.valueOf(consegna.getLocalita())
                    , String.valueOf(consegna.getCdAge())
                    , String.valueOf(consegna.getCdCapoArea())
                    , String.valueOf(consegna.getMailAge())
                    , String.valueOf(consegna.getMailCapoArea())
                    , String.valueOf(consegna.getSequenza())
                    , String.valueOf(consegna.getNumeroDocumento())
                    , String.valueOf(consegna.getTipoDocumento())
                    , String.valueOf(consegna.getIdEsitoConsegna())
                    , String.valueOf(tsCurrent)
                    , String.valueOf(consegna.getFlInviato())
                    , String.valueOf(consegna.getTesto())
                    , String.valueOf(consegna.getPagamentoContanti() ? 1 : 0)
                    , String.valueOf(consegna.getMailVettore())
                    , String.valueOf(consegna.getFlUploaded())
                    , String.valueOf(consegna.getCommento())
                    , String.valueOf(consegna.getFileName())
                    , String.valueOf(consegna.getFileType())
                    , String.valueOf(consegna.getFileBase64())
                    , String.valueOf(consegna.getAnnoReg())
                    , String.valueOf(consegna.getNrReg())
            };

            DbManager dbManager;
            dbManager = new DbManager(context);
            return dbManager.ExecuteSql(sb.toString(), parameters);
        }
        catch (Exception e)
        {
            Log.e(TAG,e.getLocalizedMessage(), e);
            return 0;
        }
    }

    public static boolean BoolUpdateStato(Consegna consegna, Context context){
        boolean result;
        try{
            StringBuilder sb;

            sb = new StringBuilder();
            sb.append("UPDATE t_consegna SET fl_uploaded = 'S' ");
            sb.append(" WHERE anno_reg = ? ");
            sb.append("   AND nr_reg = ? ");

            String[] parameters = new String[]{
                      String.valueOf(consegna.getAnnoReg())
                    , String.valueOf(consegna.getNrReg())
            };

            DbManager dbManager;
            dbManager = new DbManager(context);
            dbManager.ExecuteSql(sb.toString(),parameters);
            int rows = dbManager.ExecuteSql(sb.toString(),parameters);
//            result = rows != 0;

            result = true;
        }
        catch (Exception e)
        {
//            Log.e(TAG, e.getMessage());
            result = false;
        }
        return result;
    }

    public static Boolean Delete(Context context){
        boolean result;
        try{
            StringBuilder sb;
            sb = new StringBuilder();
            sb.append("DELTE FROM t_consegna ");

            DbManager dbManager;
            dbManager = new DbManager(context);
            dbManager.ExecuteSql(sb.toString());

            result = true;
        }
        catch (Exception e)
        {
//            Log.e(TAG, e.getMessage());
            result = false;
        }
        return result;
    }

    public static Boolean Insert(List<DbQuery> lsDbQueries, Context context) {
        boolean result;
        try{

            DbManager dbManager;
            dbManager = new DbManager(context);
            dbManager.ExecuteSql(lsDbQueries);

            result = true;
        }
        catch (Exception e)
        {
//            Log.e(TAG, e.getMessage());
            result = false;
        }
        return result;
    }

    @SuppressLint("Range")
    public static List<Consegna> GetListaToUpload (Context context)
    {
        List<Consegna> consegnas = new ArrayList<>();
        Cursor c = Consegna.GetDatiToUpload(context);
        try {
            if (c!= null) {
                while (c.moveToNext()) {
                    Consegna consegna = new Consegna();
                    consegna.setAnnoReg(c.getInt(c.getColumnIndex("anno_reg")));
                    consegna.setNrReg(c.getInt(c.getColumnIndex("nr_reg")));
                    consegna.setCdCli(c.getInt(c.getColumnIndex("cod_cli")));
                    consegna.setRagioneSociale(c.getString(c.getColumnIndex("rag_soc")));
                    consegna.setIndirizzo(c.getString(c.getColumnIndex("indirizzo")));
                    consegna.setLocalita(c.getString(c.getColumnIndex("localita")));
                    consegna.setCdAge(c.getString(c.getColumnIndex("cod_age")));
                    consegna.setCdCapoArea(c.getString(c.getColumnIndex("cod_capo_area")));
                    consegna.setMailAge(c.getString(c.getColumnIndex("mail_agente")));
                    consegna.setMailCapoArea(c.getString(c.getColumnIndex("mail_capo_area")));
                    consegna.setIdEsitoConsegna(c.getInt(c.getColumnIndex("id_esito_consegna")));
                    consegna.setTesto(c.getString(c.getColumnIndex("testo")));
                    consegna.setTsValidita(c.getString(c.getColumnIndex("ts_validita")));
                    consegna.setFlInviato(c.getString(c.getColumnIndex("fl_inviato")));
                    consegna.setSequenza(c.getString(c.getColumnIndex("sequenza")));
                    consegna.setTipoDocumento(c.getString(c.getColumnIndex("tipo_documento")));
                    consegna.setNumeroDocumento(c.getInt(c.getColumnIndex("numero_documento")));
                    consegna.setPagamentoContanti(c.getInt(c.getColumnIndex("pagamento_contanti"))==1);
                    consegna.setMailVettore(c.getString(c.getColumnIndex("mail_vettore")));
                    consegna.setFlUploaded(c.getString(c.getColumnIndex("fl_uploaded")));
                    consegna.setCommento(c.getString(c.getColumnIndex("commento")));
                    consegna.setFileName(c.getString(c.getColumnIndex("file_name")));
                    consegna.setFileType(c.getString(c.getColumnIndex("file_type")));
                    consegna.setFileBase64(c.getString(c.getColumnIndex("file_base64")));

                    consegnas.add(consegna);
                }
            }
        }catch (Exception e)
        {
            Log.e(TAG, "one error occurred:" + e.getLocalizedMessage());

        }
        return consegnas;
    }
    @SuppressLint("Range")
    public static List<Consegna> GetLista (Context context)
    {
        List<Consegna> consegnas = new ArrayList<>();
        Cursor c = Consegna.GetDati(context);
        try {
            if (c!= null) {
                while (c.moveToNext()) {
                    Consegna consegna = new Consegna();
                    consegna.setAnnoReg(c.getInt(c.getColumnIndex("anno_reg")));
                    consegna.setNrReg(c.getInt(c.getColumnIndex("nr_reg")));
                    consegna.setCdCli(c.getInt(c.getColumnIndex("cod_cli")));
                    consegna.setRagioneSociale(c.getString(c.getColumnIndex("rag_soc")));
                    consegna.setIndirizzo(c.getString(c.getColumnIndex("indirizzo")));
                    consegna.setLocalita(c.getString(c.getColumnIndex("localita")));
                    consegna.setCdAge(c.getString(c.getColumnIndex("cod_age")));
                    consegna.setCdCapoArea(c.getString(c.getColumnIndex("cod_capo_area")));
                    consegna.setMailAge(c.getString(c.getColumnIndex("mail_agente")));
                    consegna.setMailCapoArea(c.getString(c.getColumnIndex("mail_capo_area")));
                    consegna.setIdEsitoConsegna(c.getInt(c.getColumnIndex("id_esito_consegna")));
                    consegna.setTesto(c.getString(c.getColumnIndex("testo")));
                    consegna.setTsValidita(c.getString(c.getColumnIndex("ts_validita")));
                    consegna.setFlInviato(c.getString(c.getColumnIndex("fl_inviato")));
                    consegna.setSequenza(c.getString(c.getColumnIndex("sequenza")));
                    consegna.setTipoDocumento(c.getString(c.getColumnIndex("tipo_documento")));
                    consegna.setNumeroDocumento(c.getInt(c.getColumnIndex("numero_documento")));
                    consegna.setPagamentoContanti(c.getInt(c.getColumnIndex("pagamento_contanti"))==1);
                    consegna.setMailVettore(c.getString(c.getColumnIndex("mail_vettore")));
                    consegna.setFlUploaded(c.getString(c.getColumnIndex("fl_uploaded")));
                    consegna.setCommento(c.getString(c.getColumnIndex("commento")));
                    consegna.setFileName(c.getString(c.getColumnIndex("file_name")));
                    consegna.setFileType(c.getString(c.getColumnIndex("file_type")));
                    consegna.setFileBase64(c.getString(c.getColumnIndex("file_base64")));

                    consegnas.add(consegna);
                }
            }
        }catch (Exception e)
        {
            Log.e(TAG, "one error occurred:" + e.getLocalizedMessage());
        }finally {
            if (c!=null){
                c.close();
            }
        }
        return consegnas;
    }

    /**
     *
     * Restituisce una stringa contenente tutti gli indirizzi mail dei capi area conivolti
     */
    @SuppressLint("Range")
    public static String GetMailCapoArea(Context context)
    {
        StringBuilder result = new StringBuilder();
        Cursor c;
        try {

            DbManager dbManager = new DbManager(context);
            c = dbManager.GetCursor("SELECT DISTINCT  mail_capo_area FROM t_consegna ", null);

            if (c!= null){
                while (c.moveToNext()){
                    result.append(c.getString(c.getColumnIndex("mail_capo_area"))).append(';');
                }
            }
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
        }
        return result.toString();
    }

    /**
     *
     * Restituisce una stringa contenente tutti gli indirizzi mail dei capi area conivolti
     */
    @SuppressLint("Range")
    public static String GetMailVettore(Context context)
    {
        StringBuilder result = new StringBuilder();
        Cursor c;
        try {

            DbManager dbManager = new DbManager(context);
            c = dbManager.GetCursor("SELECT DISTINCT  mail_vettore FROM t_consegna ", null);

            if (c!= null){
                while (c.moveToNext()){
                    result.append(c.getString(c.getColumnIndex("mail_vettore"))+';');
                }
            }
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
        }
        return result.toString();
    }

    /**
     *
     * {Restituisce una stringa contenente tutti gli indirizzi mail degli agenti conivolti}
     */
    @SuppressLint("Range")
    public static String GetMailAgente(Context context)
    {
        StringBuilder result = new StringBuilder();
        Cursor c;
        try {

            DbManager dbManager = new DbManager(context);
            c = dbManager.GetCursor("SELECT DISTINCT  mail_agente FROM t_consegna ", null);

            if (c!= null){
                while (c.moveToNext()){
                    result.append(c.getString(c.getColumnIndex("mail_agente"))).append(';');
                }
            }
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
        }
        return result.toString();
    }

    /**
     Download delle consegne e chiamata al web server per aggiornamento su gestionale
    **/
    public static void  InsertConsegna(final Consegna consegna, final Context context) {
        try {
            Log.d(TAG, "InsertConsegna:" + consegna);
            final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

            Terminale terminale = new Terminale(context);
            String url = terminale.getWebServerUrlErgon() + "InsertConsegnaEsito";

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Consegna.class, new ConsegnaSerializer());
            Gson gson = gsonBuilder.create();

            consegna.setIdDevice(terminale.getId()); //recupero l'id del device

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(mediaType, gson.toJson(consegna)))
                    .build();
            Response responses = null;
            Log.d(TAG, "InsertConsegna Request:" + request);
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d(TAG, "onFailure " + e.getLocalizedMessage());
                    consegna.setFlInviato("N");
                    consegna.setFlUploaded("N");
                    Consegna.Update(consegna,context, consegna.TsValidita);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) {
                    try {
                        String jsonData = response.body().string();

                        Gson gson = new Gson();
                        final Result result = gson.fromJson(jsonData, Result.class);
                        Log.d(TAG, "onResponse:" + result);
                        if (result.Error != null) {
                            Log.e(TAG, result.getError().toString());
                            consegna.setFlInviato("N");
                            consegna.setFlUploaded("N");
                            Consegna.Update(consegna,context, consegna.TsValidita);

                            throw new Exception("Aggiornamento fallito");
                        }else BoolUpdateStato(consegna, context);

                    } catch (Exception e) {
                        Log.e(TAG, "one error occurred: " + e.getLocalizedMessage());
                        consegna.setFlInviato("N");
                        consegna.setFlUploaded("N");
                        Consegna.Update(consegna,context, consegna.TsValidita);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "one error occurred 1: " + e.getLocalizedMessage());
            consegna.setFlInviato("N");
            consegna.setFlUploaded("N");
            Consegna.Update(consegna,context, consegna.TsValidita);
        }
    }

    public static void UpdateConsegna(final Consegna consegna, final Context context) {
        try {
            final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

            Log.d(TAG, "UpdateConsegna");

            Terminale terminale = new Terminale(context);
            String url = terminale.getWebServerUrlErgon() + "UpdateConsegnaEsitoPost";

            Gson gson = new Gson();

            consegna.setIdDevice(terminale.getId()); //recupero l'id del device

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(mediaType, gson.toJson(consegna)))
                    .build();
            Response responses = null;

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d(TAG, "onFailure " + e.getLocalizedMessage());
                    consegna.setFlInviato("N");
                    consegna.setFlUploaded("N");
                    Consegna.Update(consegna,context, consegna.TsValidita);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) {
                    try {
                        String jsonData = response.body().string();

                        Gson gson = new Gson();
                        final Result result = gson.fromJson(jsonData, Result.class);

                        if (result.Error != null) {
                            Log.e(TAG, result.getError().toString());
                            consegna.setFlInviato("N");
                            consegna.setFlUploaded("N");
                            Consegna.Update(consegna,context, consegna.TsValidita);

                            throw new Exception("Aggiornamento fallito");
                        }else BoolUpdateStato(consegna, context);

                    } catch (Exception e) {
                        consegna.setFlInviato("N");
                        consegna.setFlUploaded("N");
                        Consegna.Update(consegna,context, consegna.TsValidita);

                        Log.e(TAG, "one error occurred: " + e.getLocalizedMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "one error occurred 1: " + e.getLocalizedMessage());
            consegna.setFlInviato("N");
            consegna.setFlUploaded("N");
            Consegna.Update(consegna,context, consegna.TsValidita);
        }
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", Consegna.class.getSimpleName() + "[", "]")
                .add("AnnoReg=" + AnnoReg)
                .add("NrReg=" + NrReg)
                .add("CdCli=" + CdCli)
                .add("RagioneSociale='" + RagioneSociale + "'")
                .add("Indirizzo='" + Indirizzo + "'")
                .add("Localita='" + Localita + "'")
                .add("CdAge='" + CdAge + "'")
                .add("CdCapoArea='" + CdCapoArea + "'")
                .add("MailAge='" + MailAge + "'")
                .add("MailCapoArea='" + MailCapoArea + "'")
                .add("Sequenza='" + Sequenza + "'")
                .add("IdEsitoConsegna=" + IdEsitoConsegna)
                .add("TsValidita='" + TsValidita + "'")
                .add("flInviato='" + flInviato + "'")
                .add("testo='" + testo + "'")
                .add("TipoDocumento='" + TipoDocumento + "'")
                .add("NumeroDocumento=" + NumeroDocumento)
                .add("PagamentoContanti=" + PagamentoContanti)
                .add("MailVettore='" + MailVettore + "'")
                .add("FlUploaded='" + FlUploaded + "'")
                .add("Commento='" + Commento + "'")
                .add("IdDevice=" + IdDevice)
                .add("FileName='" + FileName + "'")
                .add("FileType='" + FileType + "'")
                .add("FileBase64='" + FileBase64 + "'")
                .toString();
    }

    public Boolean isPagamentoContanti() {
        return getPagamentoContanti();
    }
}
