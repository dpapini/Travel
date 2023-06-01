package com.saporiditoscana.travel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.Tasks;
import com.saporiditoscana.travel.DbHelper.DbManager;
import com.saporiditoscana.travel.Orm.Consegna;
import com.saporiditoscana.travel.Orm.Giro;
import com.saporiditoscana.travel.Orm.Gps;
import com.saporiditoscana.travel.Orm.Terminale;

import java.net.ConnectException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadPoolExecutor;

public class UploadActivity extends AppCompatActivity {

    private Handler mHandler;
    private int resultGps;
    private int resultConsegna;
    private Terminale terminale;
    private Giro giro;
    ContentLoadingProgressBar pbGps;
    TextView txGps;
    ContentLoadingProgressBar pbConsegne;
    TextView txConsegne;
    Button  btnInizio;
    private static final String TAG = "UploadActivity";

    Upload upload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Logger.e(TAG, "oncreate");

        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        setContentView(R.layout.activity_upload);

        terminale = new Terminale(this);
        giro = new Giro(this);
        pbGps = findViewById(R.id.pb_gps);
        txGps = findViewById(R.id.tx_gps);
        pbConsegne = findViewById(R.id.pb_consegna);
        txConsegne = findViewById(R.id.tx_consegna);
        btnInizio = findViewById(R.id.btn_inizio);
        btnInizio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload = new Upload();
                upload.execute();
                btnInizio.setEnabled(false);
            }
        });
        pbGps.hide();
        pbConsegne.hide();

        InitToolBar("Upload terminale");

        btnInizio.setEnabled(false);
        findViewById(R.id.txServer).setVisibility(View.VISIBLE);

        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                if (hasConnection()) {
                    timer.cancel();
                    timer.purge();
                    mHandler.post(() -> {
                        btnInizio.setEnabled(true);
                        findViewById(R.id.txServer).setVisibility(View.INVISIBLE);
                    });
                }
            }
        },0,60000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (upload != null) upload.cancel(true);
    }

    private void InitToolBar(String subtitle) {
        Toolbar tb = findViewById(R.id.topAppBar);
        tb.setTitle("Travel - " + subtitle);
        TextView tv = findViewById(R.id.stato);
        tv.setVisibility(View.GONE);
        if(subtitle == "Home" ){
            tb.setNavigationIcon(null);
            tv.setVisibility(View.VISIBLE);
        }
        setSupportActionBar(tb);
    }

    private boolean hasConnection(){
//        Logger.e(TAG, "hasConnection");

        final LocalTime  timeStart = LocalTime.parse("12:55");
//        Logger.e(TAG, "hasConnection timeStart: " + timeStart);
        final LocalTime timeEnd = LocalTime.parse("13:30");
//        Logger.e(TAG, "hasConnection timeEnd: " + timeEnd);

        final LocalTime timeNow = LocalTime.now();
//        Logger.e(TAG, "hasConnection timeNow: " + timeNow);

        if (timeNow.isAfter(timeStart) && timeNow.isBefore(timeEnd)) return false;

        ConnectivityManager connectivityManager  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected())
            return false;
        else return true;
    }

    class Upload extends AsyncTask<Void, Void, Integer>{

        UploadGps uploadGps = new UploadGps();
        UploadConsegna uploadConsegna = new UploadConsegna();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbGps.hide();
            pbConsegne.hide();
        }

        @Override
        protected void onCancelled() {
            uploadGps.cancel(true);
            uploadConsegna.cancel(true);
        }

        @Override
        protected void onPostExecute(Integer s) {
            Logger.e(TAG, "onPostExecute total: " + s.toString());
            if (s == Activity.RESULT_OK) {
                DbManager dbManager;
                dbManager = new DbManager(getApplicationContext());
                dbManager.ResetDataBase(terminale);

                finish();
                startActivity(new Intent(getBaseContext(), MainActivity.class));
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {

            Logger.e(TAG, "doInBackground total");

            long startTime = System.currentTimeMillis();

            uploadGps.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            uploadConsegna.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            while((resultGps == Activity.RESULT_CANCELED || resultConsegna == Activity.RESULT_CANCELED) && ((System.currentTimeMillis() - startTime) <= 60000)) {
                try {
                    synchronized (this) {
                        wait(6000);
                    }
                } catch (InterruptedException e) {
                    return   Activity.RESULT_CANCELED;
                }
            }


            Logger.e(TAG, "resultGps total: " + resultGps);
            Logger.e(TAG, "resultConsegna total: " + resultConsegna);
            if (resultGps == Activity.RESULT_CANCELED || resultConsegna == Activity.RESULT_CANCELED) return Activity.RESULT_CANCELED;

            return Activity.RESULT_OK;
        }
    }

    class UploadGps extends AsyncTask<Void, Void, Integer>{

        List<Gps> gpsList;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbGps.show();

            if (hasConnection()) {
                gpsList = Gps.GetLista(getBaseContext());
            }
            txGps.setText("Itinerario - inizio download["+ gpsList.size() +"]");

            resultGps = Activity.RESULT_CANCELED;
        }

        @Override
        protected void onPostExecute(Integer s) {
            super.onPostExecute(s);
            pbGps.hide();
            txGps.setText("Itinerario - download terminato");
            resultGps =  s;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            //leggo tutti i dati gps non trasmessi
            if (hasConnection()){
                for (Integer i=0; i< gpsList.size();i++) {
                    txGps.setText("Itinerario - Downloading " + (int)((i*100)/gpsList.size()) +"%");
                    try{
                        Gps.InsertGps (gpsList.get(i), getBaseContext());
                    }
                    catch (Exception e){
                        return Activity.RESULT_CANCELED;
                    }
                }
                return Activity.RESULT_OK;
            }
            else return Activity.RESULT_CANCELED;
        }

    }


    class UploadConsegna extends AsyncTask<Void, Void, Integer> {


        List<Consegna> consegnaList;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pbConsegne.show();
            if (hasConnection()) {
                consegnaList = Consegna.GetListaToUpload(getBaseContext());
            }
            txConsegne.setText("Consegne - inizio download ");

            resultConsegna = Activity.RESULT_CANCELED;
        }

        @Override
        protected void onPostExecute(Integer s) {
            Logger.e(TAG, "UploadConsegna :" + s);
            super.onPostExecute(s);
            pbConsegne.hide();
            txConsegne.setText("Consegne - download terminato");
            resultConsegna = s;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            Logger.e(TAG, "UploadConsegna ");
            //leggo tutt i dati delle consegne
            if (hasConnection()){
                for (Integer i=0; i< consegnaList.size();i++) {
                    txConsegne.setText("Consegne - Downloading " + (int)((i*100)/consegnaList.size()) +"%");
                    try{
                        Consegna.InsertConsegna(consegnaList.get(i),getBaseContext(),(message, result) ->
                                {
                                    if (result == -1) {
                                        Logger.e(TAG, "UploadConsegna in errore");
                                    }
                                }
                        );
                    }
                    catch (Exception e){
                        Logger.e(TAG, "InsertConsegna catch");
                        return Activity.RESULT_CANCELED;
                    }
                }
                Giro.UpdateEndGiro(giro, getBaseContext());
                return Activity.RESULT_OK;
            }else return Activity.RESULT_CANCELED;
        }
    }
}
