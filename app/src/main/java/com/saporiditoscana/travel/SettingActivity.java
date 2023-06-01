package com.saporiditoscana.travel;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saporiditoscana.travel.DbHelper.DbManager;
import com.saporiditoscana.travel.Orm.Conducente;
import com.saporiditoscana.travel.Orm.Terminale;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.widget.ContentLoadingProgressBar;
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
//    private Spinner ddConducente;
    private Conducente conducenteSelected;
    private Handler mHandler;
    private ImageButton updateVersion;
    private static final int FTP_DOWNLOAD =1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case FTP_DOWNLOAD:
                if (resultCode == Activity.RESULT_OK){
                    final String fullPath = data.getExtras().get("fullPath").toString();
                    Uri destination = uriFromFile(getBaseContext(), new File(fullPath));

                    Intent promptInstall = new Intent(Intent.ACTION_VIEW);
                    promptInstall.setDataAndType(destination,"application/vnd.android.package-archive");
                    promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    promptInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    this.getBaseContext().startActivity(promptInstall);
//                    'Process.killProcess(Process.myPid());
                }
            break;
        }
    }

    private static Uri uriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Logger.i(TAG, "Create");
        setContentView(R.layout.activity_setting);
        mHandler = new Handler(Looper.getMainLooper());
        lpb = findViewById(R.id.lpb);
        updateVersion = findViewById(R.id.update_version);
        updateVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent   = new Intent(getBaseContext(), FtpActivity.class);
                startActivityForResult(intent, FTP_DOWNLOAD);


                Toast.makeText(getApplicationContext(), "Aggiornamento versione", Toast.LENGTH_LONG).show();
            }
        });

//        Logger.i(TAG, "InitToolBar");
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
//        ddConducente = findViewById(R.id.ddConducente);
//        ddAutomezzo = findViewById(R.id.ddAutomezzo);

        if (terminale.getId() > 0 ) //terminale inizializzato valorizzo tutti i campi
        {
//            Logger.i(TAG, "terminale inizializzato");
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
//            Logger.i(TAG, "terminale nuovo");
            mode = MODE.NEW;

            enableFields(false);
        }

//        getJsonAutomezzoAsync("http://85.39.149.205/ErgonService/ServiceErgon.svc/GetDdAutomezzo");
//        Logger.i(TAG, "chiamo GetDdConducente ");
        getJsonConducenteAsync("http://85.39.149.205/ErgonService/ServiceErgon.svc/GetDdConducente");
    }

//    private void getJsonAutomezzoAsync(String url) {
//        try {
//            OkHttpClient client = new OkHttpClient();
//            Request request = new Request.Builder()
//                    .url(url)
//                    .build();
//            Response responses = null;
//
//            client.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    e.printStackTrace();
//                }
//
//                @Override
//                public void onResponse(Call call, final Response response) throws IOException {
//                    if (!response.isSuccessful()) {
//                        throw new IOException("Unexpected code " + response);
//                    }
//                    try{
//                        final String jsonData = response.body().string();
//
//                        Gson gson = new Gson();
//                        final Result result   =  gson.fromJson(jsonData, Result.class);
//                        Type listType = new  TypeToken<List<Automezzo>> (){}.getType();
//                        final List<Automezzo>  automezzoList = gson.fromJson(result.Data.toString(),listType);
//                        Automezzo a = new Automezzo();
//                        a.setId("-1");
//                        a.setTarga("Selezionare la targa");
//                        automezzoList.add(0,a);
//
//                        mHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                final Spinner spinner = findViewById(R.id.ddAutomezzo);
//                                AutomezzoAdapter automezzoAdapter = new AutomezzoAdapter<Automezzo>(getApplicationContext(), android.R.layout.simple_spinner_item, automezzoList);
//
//                                spinner.setAdapter(automezzoAdapter);
//                                spinner.setEnabled(true);
//
//                                if(terminale != null && terminale.getIdAutomezzo()!= null && !terminale.getIdAutomezzo().isEmpty() && !terminale.getIdAutomezzo().equals("-1")) {
//                                    spinner.setSelection(automezzoAdapter.getAutomezzoById(terminale.getIdAutomezzo()));
//                                    spinner.setEnabled(false);
//                                }
//
//                                spinner.setOnItemSelectedListener(
//                                    new AdapterView.OnItemSelectedListener() {
//
//                                        @Override
//                                        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//
//                                            int position = spinner.getSelectedItemPosition();
//                                            if (position >0 ) {
//                                                Toast.makeText(getApplicationContext(), "You have selected " + automezzoList.get(position), Toast.LENGTH_LONG).show();
//                                                txTarga = findViewById(R.id.txTarga);
//                                                txTarga.setText(automezzoList.get(position).Targa);
//                                            }
//                                        }
//
//                                        @Override
//                                        public void onNothingSelected(AdapterView<?> arg0) {
//
//                                        }
//                                    }
//                                );
//                            }
//
//                        });
//
//                    } catch (Exception e) {
//                        Log.e(TAG, e.getMessage());
//                    }
//                }
//            });
//        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
//        }
//    }

    private void getJsonConducenteAsync(String url) {
        try {
            final AutoCompleteTextView atv = findViewById(R.id.atvConducente);
            atv.setEnabled(false);

            lpb.show();
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
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            lpb.hide();
                        }
                    });
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    try{
                        final String jsonData = response.body().string();
                        Gson gson = new Gson();
                        final Result result   =  gson.fromJson(jsonData, Result.class);

                        Type listType = new  TypeToken<List<Conducente>> (){}.getType();
                        final List<Conducente>  conducenteList = gson.fromJson(result.getData().toString(),listType);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {

                                ConducenteAdapter<Conducente> conducenteAdapter = new ConducenteAdapter<Conducente>(getApplicationContext(), android.R.layout.simple_spinner_item, conducenteList);

                                atv.setAdapter(conducenteAdapter);
                                atv.setEnabled(true);

//                                spinner.setAdapter(conducenteAdapter);
//                                spinner.setEnabled(true);

                                if(terminale != null && terminale.getIdConducente()!= null  && !terminale.getIdConducente().isEmpty() && !terminale.getIdConducente().equals("-1")) {
                                    conducenteSelected = conducenteAdapter.getConducente(terminale.getIdConducente());
                                    if (conducenteSelected!=null) {
                                        atv.setText(conducenteSelected.Testo);
                                        atv.setEnabled(false);
                                    }
//                                    spinner.setSelection(conducenteAdapter.getConducenteById(terminale.getIdConducente()));
//                                    spinner.setEnabled(false);
                                }

                                atv.setOnItemClickListener((parent, view, position, id) -> {
                                    conducenteSelected = null;
                                    if (position != ListView.INVALID_POSITION) {
                                        conducenteSelected =(Conducente)parent.getItemAtPosition(position);
                                        atv.setSelection(0);
                                        Toast.makeText(getApplicationContext(), "Hai selezionato" + conducenteSelected.getTesto(), Toast.LENGTH_LONG).show();
                                        // TODO Auto-generated method stub
                                        txAutista = findViewById(R.id.txAutista);
                                        txAutista.setText(conducenteSelected.getTesto());

                                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                                    }
                                });


//                                spinner.setOnItemSelectedListener(
//                                        new AdapterView.OnItemSelectedListener() {
//
//                                            @Override
//                                            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//
//                                                int position = spinner.getSelectedItemPosition();
//                                                if (position > 0) {
//                                                    Toast.makeText(getApplicationContext(), "You have selected " + conducenteList.get(position), Toast.LENGTH_LONG).show();
//                                                    // TODO Auto-generated method stub
//                                                    txAutista = findViewById(R.id.txAutista);
//                                                    txAutista.setText(conducenteList.get(position).Testo);
//                                                }
//                                            }
//
//                                            @Override
//                                            public void onNothingSelected(AdapterView<?> arg0) {
//                                                // TODO Auto-generated method stub
//
//                                            }
//                                        }
//                                );
                            }

                        });

                    } catch (Exception e) {
                        Logger.e(TAG, "one error on getJsonConducenteAsync occurred: " + e.getMessage());
                    }
                }
            });
        }catch (Exception e){
//            Log.e(TAG, e.getMessage());
            Logger.e(TAG, "one error on getJsonConducenteAsync occurred: " + e.getMessage());
        }
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
                    Logger.e(TAG, "one error on OptionsItemSelected occurred: " + e.getMessage());
                }

                String positiveText = "chiudi";
                builder.setPositiveButton(positiveText,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onBackPressed();
                            }
                        });
                builder.show();
                return true;
            case R.id.actio_ripristina:
                builder.setTitle("Password");
                final EditText psw= new EditText(this);
                psw.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                psw.setGravity(Gravity.LEFT|Gravity.TOP);

                builder.setView(psw);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert = alt_bld.create();
                            // Title for AlertDialog
                            alert.setTitle("Title");
                            alert.show();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            case R.id.action_save:
                if (!txFtpServer.getText().toString().trim().isEmpty()  &&
                    !txFtpServer.getText().toString().trim().toUpperCase().contains("HTTP://")) {
                    //errore in mappa
                    txFtpServer.setError(getString(R.string.wrong_address));
                    txFtpServer.setText("");
                    return false;
                }

                if (!txWebServer.getText().toString().trim().isEmpty() &&
                    !txWebServer.getText().toString().trim().toUpperCase().contains("HTTP://")) {
                    //errore in mappa
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
//                terminale.setIdAutomezzo(((Automezzo) ddAutomezzo.getSelectedItem()).Id);

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
//                            if (ddConducente.getSelectedItem()!= null)
//                                terminale.setIdConducente(((Conducente)ddConducente.getSelectedItem()).Id);

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
                    Logger.e(TAG, "one error on save occurred: " + e.getMessage());
                    Toast.makeText(this, "One Error on save occurred: " +e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                return true;
            default:return super.onOptionsItemSelected(item);
        }
    }

}
