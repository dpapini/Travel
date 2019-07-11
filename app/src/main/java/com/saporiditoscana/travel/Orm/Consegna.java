package com.saporiditoscana.travel.Orm;

import android.content.Context;
import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.saporiditoscana.travel.DbHelper.DbManager;
import com.saporiditoscana.travel.DbHelper.DbQuery;
import com.saporiditoscana.travel.Logger;
import com.saporiditoscana.travel.Result;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Consegna  implements Serializable {
    private static final String TAG = "Consegna";
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
    @SerializedName("idEsitoConsegna")
    private int idEsitoConsegna;
    @SerializedName("tsValidita")
    private String tsValidita;
    @SerializedName("flInviato")
    private String flInviato;
    @SerializedName("testo")
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


    public Consegna(){}

    public Consegna(Context context){
        this.context = context;
    }

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
                    this.idEsitoConsegna = c.getInt(c.getColumnIndex("id_esito_consegna"));
                    this.tsValidita = c.getString(c.getColumnIndex("ts_validita"));
                    this.flInviato = c.getString(c.getColumnIndex("flInviato"));
                    this.testo = c.getString(c.getColumnIndex("testo"));
                    this.Sequenza = c.getString(c.getColumnIndex("sequenza"));
                    this.NumeroDocumento = c.getInt(c.getColumnIndex("numero_documento"));
                    this.TipoDocumento = c.getString(c.getColumnIndex("tipo_documento"));
                    this.PagamentoContanti =    c.getInt(c.getColumnIndex("pagamento_contanti"))==1; //TRUE
                    this.MailVettore = c.getString(c.getColumnIndex("mail_vettore"));
                    this.FlUploaded = c.getString(c.getColumnIndex("fl_uploaded"));
                    this.Commento = c.getString(c.getColumnIndex("commento"));
                }
            }
        }catch (Exception e)
        {
//            Log.e(TAG, e.getMessage());

        }
    }

    public static Cursor GetDati(Context context){
        Cursor c;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM t_consegna WHERE cod_cli > 0 ORDER BY sequenza ");

            DbManager dbManager = new DbManager(context);
            c = dbManager.GetCursor(sb.toString(), null);
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
            c= null;
        }
        return  c;
    }


    private Cursor getConsegna(int annoReg, int nrReg)
    {
        Cursor c;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM t_consegna ");
            sb.append(" WHERE anno_reg = ? " );
            sb.append("   AND nr_reg = ? " );

            String[] parameters = new String[]{
                    String.valueOf(this.AnnoReg),
                    String.valueOf(this.NrReg)};

            DbManager dbManager = new DbManager(this.context);
            c = dbManager.GetCursor(sb.toString(), parameters);
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
        return String.valueOf(CdCli) + " - " + RagioneSociale.trim();
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
        return idEsitoConsegna;
    }

    public void setIdEsitoConsegna(int idEsitoConsegna) {
        this.idEsitoConsegna = idEsitoConsegna;
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

    public String getTsValidita() {
        return tsValidita;
    }

    public void setTsValidita(String tsValidita) {
        this.tsValidita = tsValidita;
    }

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

    public static DbQuery Insert(int AnnoReg, int NrReg){
        DbQuery dbQuery = new DbQuery();
        try{
            StringBuilder sb;

            sb = new StringBuilder();
            sb.append("INSERT INTO t_consegna (anno_reg, nr_reg, numero_documento, tipo_documento, cod_cli, rag_soc, indirizzo, localita, cod_age, cod_capo_area, mail_agente, mail_capo_area, pagamento_contanti, mail_vettore, commento)");
            sb.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
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
            };
            dbQuery.setParameters(parameters);

        }
        catch (Exception e)
        {
//            Log.e(TAG, e.getMessage());
        }
        return dbQuery;
    }

    public static boolean Check(Context context){
        Boolean result = false;
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
            result = false;
        }
        return result;
    }


    public static Boolean Update(Consegna consegna, Context context){
        Boolean result = false;
        try{
            StringBuilder sb;

            String tsCurrent = Gps.GetCurrentTimeStamp();
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
                    , String.valueOf(consegna.getPagamentoContanti()?1:0)
                    , String.valueOf(consegna.getMailVettore())
                    , String.valueOf(consegna.getFlUploaded())
                    , String.valueOf(consegna.getCommento())
                    , String.valueOf(consegna.getAnnoReg())
                    , String.valueOf(consegna.getNrReg())
            };

            DbManager dbManager;
            dbManager = new DbManager(context);
            int rows = dbManager.ExecuteSql(sb.toString(),parameters);

            if (rows==0) result = false;
            else {
                result = true;
                consegna.setTsValidita(tsCurrent);
            }
        }
        catch (Exception e)
        {
            result = false;
        }
        return result;
    }

    public static boolean BoolUpdateStato(Consegna consegna, Context context){
        Boolean result = false;
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
            if (rows==0) result = false;
            else result = true;

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
        Boolean result = false;
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
        Boolean result;
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

    public static List<Consegna> GetLista (Context context)
    {
        List<Consegna> consegnas = new ArrayList<Consegna>();
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

                    consegnas.add(consegna);
                }
            }
        }catch (Exception e)
        {
            Logger.e(TAG, "one error occurred:" + e.getLocalizedMessage());

        }
        return consegnas;
    }

    /**
     *
     * Restituisce una stringa contenente tutti gli indirizzi mail dei capi area conivolti
     */
    public static String GetMailCapoArea(Context context)
    {
        StringBuilder result = new StringBuilder();
        Cursor c;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT DISTINCT  mail_capo_area FROM t_consegna ");

            DbManager dbManager = new DbManager(context);
            c = dbManager.GetCursor(sb.toString(), null);

            if (c!= null){
                while (c.moveToNext()){
                    result.append(c.getString(c.getColumnIndex("mail_capo_area"))+';');
                }
            }
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
            c= null;
        }
        return result.toString();
    }

    /**
     *
     * Restituisce una stringa contenente tutti gli indirizzi mail dei capi area conivolti
     */
    public static String GetMailVettore(Context context)
    {
        StringBuilder result = new StringBuilder();
        Cursor c;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT DISTINCT  mail_vettore FROM t_consegna ");

            DbManager dbManager = new DbManager(context);
            c = dbManager.GetCursor(sb.toString(), null);

            if (c!= null){
                while (c.moveToNext()){
                    result.append(c.getString(c.getColumnIndex("mail_vettore"))+';');
                }
            }
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
            c= null;
        }
        return result.toString();
    }

    /**
     *
     * {Restituisce una stringa contenente tutti gli indirizzi mail degli agenti conivolti}
     */
    public static String GetMailAgente(Context context)
    {
        StringBuilder result = new StringBuilder();
        Cursor c;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT DISTINCT  mail_agente FROM t_consegna ");

            DbManager dbManager = new DbManager(context);
            c = dbManager.GetCursor(sb.toString(), null);

            if (c!= null){
                while (c.moveToNext()){
                    result.append(c.getString(c.getColumnIndex("mail_agente"))+';');
                }
            }
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
            c= null;
        }
        return result.toString();
    }

    /**
     Download delle consegne e chiamata al web server per aggiornamento su gestionale
    **/
    public static void InsertConsegna(final Consegna consegna, final Context context) {
        try {
            final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

            Terminale terminale = new Terminale(context);
            String url = terminale.getWebServerUrlErgon() + "InsertConsegna";

            JsonObject json = new JsonObject();
            json.addProperty("AnnoReg", consegna.getAnnoReg());
            json.addProperty("NrReg", consegna.getNrReg());
            json.addProperty("IdEsitoConsegna", consegna.getIdEsitoConsegna());
            json.addProperty("Testo", consegna.getTesto());
            json.addProperty("TsValidita", consegna.getTsValidita());
            json.addProperty("IdDevice", terminale.getId());
            json.addProperty("Commento", consegna.getCommento());

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

                        if (result.Error != null) {
                            Logger.e(TAG, result.getError().toString());
                            throw new Exception("Aggiornamento fallito");
                        }

                        BoolUpdateStato(consegna, context);

                    } catch (Exception e) {
                        Logger.e(TAG, "one error occurred: " + e.getLocalizedMessage());
                    }
                }
            });
        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
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
                .add("idEsitoConsegna=" + idEsitoConsegna)
                .add("tsValidita='" + tsValidita + "'")
                .add("flInviato='" + flInviato + "'")
                .add("testo='" + testo + "'")
                .add("TipoDocumento='" + TipoDocumento + "'")
                .add("NumeroDocumento=" + NumeroDocumento)
                .add("PagamentoContanti=" + PagamentoContanti)
                .add("MailVettore='" + MailVettore + "'")
                .add("FlUploaded='" + FlUploaded + "'")
                .toString();
    }
}
