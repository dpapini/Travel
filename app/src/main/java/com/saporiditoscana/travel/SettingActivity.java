package com.saporiditoscana.travel;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.widget.ContentLoadingProgressBar;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.saporiditoscana.travel.DbHelper.DbManager;
import com.saporiditoscana.travel.Orm.Conducente;
import com.saporiditoscana.travel.Orm.ConducenteDeserializer;
import com.saporiditoscana.travel.Orm.Terminale;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";
    private static final String REGEXTARGA = "[A-Za-z]{2}[0-9]{3}[A-Za-z]{2}";
    private ContentLoadingProgressBar lpb;

    public enum MODE{
        NEW,
        UPDATE,
    }
    private MODE mode;
    private Terminale terminale;

    //istanzio el variabili per  i campi della view
    private EditText txTerminale;
    private EditText txTesto;
    private EditText txFtpServer;
    private EditText txWebServer;
    private EditText txTarga;
    private EditText txAutista;
    private Conducente conducenteSelected;
    private Handler mHandler;
    private ActivityResultLauncher<Intent> ftpLauncher;

    public static PackageInfo packageInfo;

    private static Uri uriFromFile(Context context, File file) {
        //return FileProvider.getUriForFile(context, packageInfo.versionName + ".provider", file);
        return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
    }

    private void InitToolBar(String subtitle) {
        Toolbar tb = findViewById(R.id.topAppBar);
        tb.setTitle("Travel - " + subtitle);
        TextView tv = findViewById(R.id.stato);
        tv.setVisibility(View.GONE);
        if(Objects.equals(subtitle, "Home")){
            tb.setNavigationIcon(null);
            tv.setVisibility(View.VISIBLE);
        }
        setSupportActionBar(tb);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mHandler = new Handler(Looper.getMainLooper());
        lpb = findViewById(R.id.lpb);
        ImageButton updateVersion = findViewById(R.id.update_version);
        updateVersion.setOnClickListener(v -> {
            Intent intent   = new Intent(getBaseContext(), FtpActivity.class);
            ftpLauncher.launch(intent);
            Toast.makeText(getApplicationContext(), "Aggiornamento versione", Toast.LENGTH_LONG).show();
        });

        InitToolBar("Impostazioni");

        //setto Terminale
        terminale = new Terminale(this);

        //recupero i campi della view
        txTerminale = findViewById(R.id.txTerminale);
        txTesto = findViewById(R.id.txTesto);
        txFtpServer = findViewById(R.id.txFtpServer);
        txWebServer = findViewById(R.id.txWebServer);
        txTarga = findViewById(R.id.txTarga);
        txAutista = findViewById(R.id.txAutista);

        if (terminale.getId() > 0 ) //terminale inizializzato valorizzo tutti i campi
        {
            mode = MODE.UPDATE;

            txTerminale = findViewById(R.id.txTerminale);
            txTerminale.setText(String.valueOf(terminale.getId()));
            txTerminale.setEnabled(false);
            txTerminale.setFocusable(false);

            txTesto = findViewById(R.id.txTesto);
            txTesto.setText(terminale.getTesto());

            txFtpServer = findViewById(R.id.txFtpServer);
            txFtpServer.setText(terminale.getFtpServer());

            txWebServer = findViewById(R.id.txWebServer);
            txWebServer.setText(terminale.getWebServer());

            txTarga = findViewById(R.id.txTarga);
            txTarga.setText(terminale.getTarga());

            txAutista = findViewById(R.id.txAutista);
            txAutista.setText(terminale.getConducente());
        }else{
            mode = MODE.NEW;

            enableFields(false);
        }

        getJsonConducenteAsync();

        ftpLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::handleFtpActivityResult);

        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleFtpActivityResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            if (data == null) return;
            final String fullPath = data.getExtras().get("fullPath").toString();
            Uri destination = uriFromFile(getBaseContext(), new File(fullPath));

            Intent promptInstall = new Intent(Intent.ACTION_VIEW);
            promptInstall.setDataAndType(destination,"application/vnd.android.package-archive");
            promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            promptInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            this.getBaseContext().startActivity(promptInstall);
        }
    }

    private void getJsonConducenteAsync() {
        final AutoCompleteTextView atv = findViewById(R.id.atvConducente);
        atv.setEnabled(true);

        lpb.show();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://85.39.149.205/ErgonService/ServiceErgon.svc/GetDdConducente")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                mHandler.post(() -> {
                    lpb.hide();
                    showErrorDialog("GetDdConducente errore " + e.getMessage());
                });
                Log.e(TAG, "Error on getJsonConducenteAsync 1: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                mHandler.post(() -> lpb.hide());
                if (!response.isSuccessful()) {
                    mHandler.post(() -> showErrorDialog("Unexpected code " + response));
                    throw new IOException("Unexpected code " + response);
                }
                try {
                    final String jsonData = response.body().string();
                    // Creare l'istanza di GsonBuilder e registrare il deserializzatore
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(Result.class, new ResultDeserializer());
                    Gson gsonResult = gsonBuilder.create();
                    Result result = gsonResult.fromJson(jsonData, Result.class);
                    Type listType = TypeToken.getParameterized(List.class, Conducente.class).getType();
                    //mHandler.post(() -> showErrorDialog("TypeToken istanziato: " ));

                    gsonBuilder = new GsonBuilder();
                    gsonBuilder.registerTypeAdapter(listType, new ConducenteListDeserializer());
                    gsonBuilder.registerTypeAdapter(Conducente.class, new ConducenteDeserializer());
                    gsonResult = gsonBuilder.create();

                    final List<Conducente> conducenteList = gsonResult.fromJson(result.getData().toString(), listType);

                    mHandler.post(() -> {
                        ConducenteAdapter conducenteAdapter = new ConducenteAdapter(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, conducenteList);

                        atv.setAdapter(conducenteAdapter);

                        if (terminale != null && terminale.getIdConducente() != null && !terminale.getIdConducente().isEmpty() && !terminale.getIdConducente().equals("-1")) {
                            conducenteSelected = conducenteAdapter.getConducente(terminale.getIdConducente());
                            if (conducenteSelected != null) {
                                atv.setText(conducenteSelected.getTesto());
                                atv.setEnabled(false);
                            }
                        }

                        atv.setOnItemClickListener((parent, view, position, id) -> {
                            conducenteSelected = null;
                            if (position != ListView.INVALID_POSITION) {
                                conducenteSelected = (Conducente) parent.getItemAtPosition(position);
                                atv.setSelection(0);
                                Toast.makeText(getApplicationContext(), "Hai selezionato " + conducenteSelected.getTesto(), Toast.LENGTH_LONG).show();
                                txAutista = findViewById(R.id.txAutista);
                                txAutista.setText(conducenteSelected.getTesto());

                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                            }
                        });
                    });
                } catch (Exception e) {
                    mHandler.post(() -> showErrorDialog("Error on getJsonConducenteAsync 2: " + e.getMessage()));
                    Log.e(TAG, "Error on getJsonConducenteAsync: " + e.getMessage());
                }
            }
        });
    }

    private void showErrorDialog(String errorMessage) {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(SettingActivity.this);
        alt_bld.setMessage(errorMessage)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog1, id1) -> dialog1.cancel());
        AlertDialog alert = alt_bld.create();
        alert.setTitle("Title");
        alert.show();
    }
    private void enableFields(boolean value) {
        txTesto.setEnabled(value);
        txTesto.setFocusable(value);

        txFtpServer.setEnabled(value);
        txFtpServer.setFocusable(value);

        txWebServer.setEnabled(value);
        txWebServer.setFocusable(value);

        txTarga.setEnabled(value);
        txTarga.setFocusable(value);

        txAutista.setEnabled(value);
        txAutista.setFocusable(value);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public  boolean onOptionsItemSelected(MenuItem item){
        //scatenato dalla toolbar
        int id = item.getItemId();
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (id)
        {
            case R.id.action_info:
                builder.setTitle("Info");
                try {
                builder.setMessage("Versione: " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "one error on OptionsItemSelected occurred: " + e.getMessage());
                }

                String positiveText = "chiudi";
                builder.setPositiveButton(positiveText,
                        (dialog, which) -> onBackPressed());
                builder.show();
                return true;
            case R.id.actio_ripristina:
                builder.setTitle("Password");
                final EditText psw= new EditText(this);
                psw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                psw.setGravity(Gravity.START|Gravity.TOP);

                builder.setView(psw);
                builder.setPositiveButton("OK", (dialog, which) -> {
                    if (psw.getText().toString().equals("1977") ){
                        DbManager dbManager;
                        dbManager = new DbManager(getApplicationContext());
                        dbManager.ResetDataBase(terminale);
                        finish();
                        startActivity(new Intent(getBaseContext(), MainActivity.class));
                    }else
                    {
                        AlertDialog.Builder alt_bld = new AlertDialog.Builder(SettingActivity.this);
                        alt_bld.setMessage("Password non valida")
                                .setCancelable(false)
                                .setPositiveButton("OK", (dialog1, id1) -> dialog1.cancel());
                        AlertDialog alert = alt_bld.create();
                        alert.setTitle("Title");
                        alert.show();
                    }
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            case R.id.action_save:
                if (!txFtpServer.getText().toString().trim().isEmpty()  &&
                    !txFtpServer.getText().toString().trim().toUpperCase().contains("HTTP://")) {
                    txFtpServer.setError(getString(R.string.wrong_address));
                    txFtpServer.setText("");
                    return false;
                }

                if (!txWebServer.getText().toString().trim().isEmpty() &&
                    !txWebServer.getText().toString().trim().toUpperCase().contains("HTTP://")) {
                    txWebServer.setError(getString(R.string.wrong_address));
                    txWebServer.setText("");
                    return false;
                }

                if(!txTarga.getText().toString().trim().isEmpty())
                {
                    // Create a Pattern object
                    Pattern r = Pattern.compile(REGEXTARGA);
                    // Now create matcher object.
                    Matcher m = r.matcher(txTarga.getText().toString().trim());

                    if (!m.find()) {
                        txTarga.setError(getString(R.string.wrong_targa));
                        txTarga.setText("");
                    }
                }

                terminale = new Terminale(this);
                terminale.setId(Integer.parseInt(txTerminale.getText().toString().trim()));

                boolean result = false;
                try {
                    //aggiorno la tabella
                    switch (mode) {
                        case NEW:
                            result = terminale.InsertTerminale(terminale.getId());
                            break;
                        case UPDATE:
                            terminale.setTesto(txTesto.getText().toString().trim());
                            terminale.setFtpServer(txFtpServer.getText().toString().trim());
                            terminale.setWebServer(txWebServer.getText().toString().trim());
                            terminale.setTarga(txTarga.getText().toString().trim());
                            terminale.setConducente(txAutista.getText().toString().trim());

                            if (conducenteSelected != null){
                                terminale.setIdConducente(conducenteSelected.getId());
                            }


                            result = terminale.UpdateTerminale(terminale);
                            break;
                    }

                    if (result) {
                        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(new Intent(this, MainActivity.class));

                    }
                    else
                        Toast.makeText(this, "Error on saving", Toast.LENGTH_SHORT).show();

                    enableFields(true);
                }catch (Exception e){
                    Log.e(TAG, "one error on save occurred: " + e.getMessage());
                    Toast.makeText(this, "One Error on save occurred: " +e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                return true;
            default:return super.onOptionsItemSelected(item);
        }
    }

}
