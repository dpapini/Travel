package com.saporiditoscana.travel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.ContentLoadingProgressBar;

import com.saporiditoscana.travel.DbHelper.DbManager;
import com.saporiditoscana.travel.Orm.Consegna;
import com.saporiditoscana.travel.Orm.Giro;
import com.saporiditoscana.travel.Orm.Gps;
import com.saporiditoscana.travel.Orm.Terminale;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    Button btnInizio;

    ExecutorService executorService;
    Future<Integer> uploadFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        mHandler = new Handler();
        terminale = new Terminale(this);
        giro = new Giro(this);
        pbGps = findViewById(R.id.pb_gps);
        txGps = findViewById(R.id.tx_gps);
        pbConsegne = findViewById(R.id.pb_consegna);
        txConsegne = findViewById(R.id.tx_consegna);
        btnInizio = findViewById(R.id.btn_inizio);

        btnInizio.setOnClickListener(v -> {
            startUpload();
            btnInizio.setEnabled(false);
        });

        pbGps.hide();
        pbConsegne.hide();

        InitToolBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    private void InitToolBar() {
        Toolbar tb = findViewById(R.id.topAppBar);
        tb.setTitle("Travel - " + "Upload terminale");
        setSupportActionBar(tb);
    }

    private boolean hasConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return networkCapabilities != null && (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }

        return false;
    }

    private void startUpload() {
        if (hasConnection()) {
            executorService = Executors.newSingleThreadExecutor();

            uploadFuture = executorService.submit(() -> {
                uploadGps();
                uploadConsegna();
                return Activity.RESULT_OK;
            });

            executorService.shutdown();

            mHandler.postDelayed(() -> {
                if (!executorService.isTerminated()) {
                    executorService.shutdownNow();
                }
            }, 10, TimeUnit.MINUTES.ordinal());
        }
    }

    private void uploadGps() {
        mHandler.post(() -> {
            pbGps.show();
            txGps.setText("Itinerario - inizio download");
        });

        List<Gps> gpsList = null;
        if (hasConnection()) {
            gpsList = Gps.GetLista(getBaseContext());
        }

        resultGps = Activity.RESULT_CANCELED;

        if (gpsList != null) {
            for (int i = 0; i < gpsList.size(); i++) {
                final int progress = (int) ((i * 100) / gpsList.size());
                final Gps gps = gpsList.get(i);

                mHandler.post(() -> txGps.setText("Itinerario - Downloading " + progress + "%"));

                try {
                    Gps.InsertGps(gps, getBaseContext());
                } catch (Exception e) {
                    resultGps = Activity.RESULT_CANCELED;
                    return;
                }
            }
        }

        mHandler.post(() -> {
            pbGps.hide();
            txGps.setText("Itinerario - download terminato");
            resultGps = Activity.RESULT_OK;
        });
    }

    private void uploadConsegna() {
        mHandler.post(() -> {
            pbConsegne.show();
            txConsegne.setText("Consegne - inizio download");
        });

        List<Consegna> consegnaList = null;
        if (hasConnection()) {
            consegnaList = Consegna.GetListaToUpload(getBaseContext());
        }

        resultConsegna = Activity.RESULT_CANCELED;

        if (consegnaList != null) {
            for (int i = 0; i < consegnaList.size(); i++) {
                final int progress = (int) ((i * 100) / consegnaList.size());
                final Consegna consegna = consegnaList.get(i);

                mHandler.post(() -> txConsegne.setText("Consegne - Downloading " + progress + "%"));

                try {
                    Consegna.InsertConsegna(consegna, getBaseContext());
                } catch (Exception e) {
                    resultConsegna = Activity.RESULT_CANCELED;
                    return;
                }
            }
            Giro.UpdateEndGiro(giro, getBaseContext());
            resultConsegna = Activity.RESULT_OK;
        }

        mHandler.post(() -> {
            pbConsegne.hide();
            txConsegne.setText("Consegne - download terminato");
            handleUploadResults();
        });
    }

    private void handleUploadResults() {
        if (resultGps == Activity.RESULT_OK && resultConsegna == Activity.RESULT_OK) {
            // Entrambi i caricamenti sono riusciti
            // Effettua le azioni necessarie per segnalare il successo
            DbManager dbManager = new DbManager(getApplicationContext());
            dbManager.ResetDataBase(terminale);

            finish();
            startActivity(new Intent(getBaseContext(), MainActivity.class));
        }
    }
}
