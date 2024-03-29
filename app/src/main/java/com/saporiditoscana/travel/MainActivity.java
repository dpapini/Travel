package com.saporiditoscana.travel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.saporiditoscana.travel.DbHelper.DbQuery;
import com.saporiditoscana.travel.Orm.Attach;
import com.saporiditoscana.travel.Orm.Consegna;
import com.saporiditoscana.travel.Orm.ConsegnaDeserializer;
import com.saporiditoscana.travel.Orm.ConsegnaSerializer;
import com.saporiditoscana.travel.Orm.EsitoConsegna;
import com.saporiditoscana.travel.Orm.Giro;
import com.saporiditoscana.travel.Orm.GiroDeserializer;
import com.saporiditoscana.travel.Orm.Gps;
import com.saporiditoscana.travel.Orm.Mail;
import com.saporiditoscana.travel.Orm.Step;
import com.saporiditoscana.travel.Orm.Terminale;
import com.saporiditoscana.travel.Services.LocationService;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity  extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    static final int RESULT_OK = -1;
    private boolean mTracking = false;
    static int esitoConsegna = 0;

    private ActivityResultLauncher<Intent> incassoLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> uploadLauncher;
    private ConsegnaAdapter consegnaAdapter;
    public static String qrCodeText;
    @SuppressLint("StaticFieldLeak")
    public static EditText dtConsegna ;
    public  ImageButton btnDate;
    @SuppressLint("StaticFieldLeak")
    public static TextView txGiro;

    public Dialog clientDialog;
    public Dialog clientOtherDialog;
    public Giro giro;

    private Handler mHandler;
    private ContentLoadingProgressBar lpb;
    private boolean isStartStep1Called = false;
    final Calendar myCalendar=Calendar.getInstance();
    BottomNavigationView bab;

    private STATO stato = STATO.CONFIGURED;

    public STATO GetStatApp(){
        return stato = STATO.fromid(Step.Last(MainActivity.this).getId());
    }

    public enum STATO {
        CONFIGURED("configured", 0),
        UPLOAD_TRAVEL("upload travel", 10),
        UPLOAD_DETAIL_TRAVEL("upload detail travel", 15),
        TRAVEL_COMPLETED("travel completed",20),
        GET_PICTURE_CAMERA("start travel", 24),
        UPLOAD_START_TRAVEL("start travel", 25),
        UPLOAD_END_TRAVEL("end travel", 30),
        UPLOAD_DOWNLOAD_TRAVEL("download travel", 35);

        private final String testo;
        private final int id;

        STATO(String toString, int value) {
            testo = toString;
            id = value;
        }

        public static STATO fromid(int value) {
            for (STATO stato: STATO.values()) {
                if (stato.id == value) {
                    return stato;
                }
            }
            return null;
        }

        @NonNull
        @Override
        public String toString() {
            return testo;
        }

        public int getId() {
            return id;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Controlla i permessi solo se non sono stati concessi
        if (checkPermissions()) {
            requestPermissions();
        }

        GetStatApp();

        UpdateStastoView();

        initializeViewGiro();
        switch (this.stato) {
            case CONFIGURED:
                dtConsegna.setEnabled(true);
                break;
            case UPLOAD_TRAVEL:
                break;
            case UPLOAD_DETAIL_TRAVEL:
                if (Consegna.Check(MainActivity.this)) {
                    Step.Update(STATO.TRAVEL_COMPLETED.id, MainActivity.this);
                    this.stato = STATO.TRAVEL_COMPLETED;
                }
                break;
            case TRAVEL_COMPLETED:
                //ho caricato tutte le bolle
                break;
            case UPLOAD_START_TRAVEL:
                dtConsegna.setEnabled(false);
                chekTracking();
                if (consegnaAdapter != null && consegnaAdapter.isCompleted()) {
                    Step.Update(STATO.UPLOAD_END_TRAVEL.id, this);
                    UpdateStastoView();
                }
                break;
            case UPLOAD_END_TRAVEL:
            case UPLOAD_DOWNLOAD_TRAVEL:
                dtConsegna.setEnabled(false);
                chekTracking();
                break;
            case GET_PICTURE_CAMERA:
                dtConsegna.setEnabled(false);
                Step.Update(STATO.UPLOAD_START_TRAVEL.id,MainActivity.this);
            default:
                dtConsegna.setEnabled(true);
                break;
        }
    }

    private void chekTracking() {
        if (!mTracking) {
            startService(new Intent(getBaseContext(), LocationService.class));
            mTracking = true;
        }
    }

    private void UpdateStastoView() {
        mHandler.post(() -> {
            TextView tvStato = findViewById(R.id.stato);
            tvStato.setText(GetStatApp().testo);
        });
    }

    //heck google plae services
    private void startStep1(){
        
        if (isGooglePlayServicesAvailable()) {
            //Passing null to indicate that it is executing for the first time.
            startStep2(null);
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.alterTitle);
            builder.setMessage(R.string.playServiceNoDisp);
            builder.setPositiveButton("OK", (dialog, which) -> startActivity(new Intent(MainActivity.this, SettingActivity.class)));
            builder.show();
        }
    }

    @SuppressLint("BatteryLife")
    private Boolean startStep2(DialogInterface dialog) {

        if (!hasConnection()){
            promptInternetConnect();
            return false;
        }

        if (dialog != null) {
            dialog.dismiss();
        }

        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName))
        {
           // if you want to enable doze mode
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivity(intent);
            return false;
        }


        //Yes there is active internet connection. Next check Location is granted by user or not.
        if (checkPermissions())
            requestPermissions();

        return true;
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

    private void requestPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.INTERNET
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = addMediaPermissions(permissions);
        }

        boolean shouldProvideRationale = false;
        for (String permission : permissions) {
            shouldProvideRationale = shouldProvideRationale || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
        }

        if (shouldProvideRationale) {
            String[] finalPermissions = permissions;
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, view -> ActivityCompat.requestPermissions(MainActivity.this, finalPermissions, REQUEST_PERMISSIONS_REQUEST_CODE));
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private String[] addMediaPermissions(String[] permissions) {
        List<String> permissionList = new ArrayList<>(Arrays.asList(permissions));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_MEDIA_IMAGES);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_MEDIA_VIDEO);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_MEDIA_AUDIO);
        }

        return permissionList.toArray(new String[0]);
    }
    private void promptInternetConnect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.title_alert_no_intenet);
        builder.setMessage(R.string.msg_alert_no_internet);

        String positiveText = getString(R.string.btn_label_refresh);
        builder.setPositiveButton(positiveText,
                (dialog, which) -> {
                    //Block the Application Execution until user grants the permissions
                    if (startStep2(dialog)) {
                        //Now make sure about location permission.
                        if (checkPermissions()) {
                            requestPermissions();
                        }

                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Return the current state of the permissions needed.
    private boolean checkPermissions() {
        int permissionState1 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionState2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionState3 =  ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        int permissionState6 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        return permissionState1 != PackageManager.PERMISSION_GRANTED ||
                permissionState2 != PackageManager.PERMISSION_GRANTED ||
                permissionState3 != PackageManager.PERMISSION_GRANTED ||
                permissionState6 != PackageManager.PERMISSION_GRANTED;
    }

    private void populateRecyclerView() {
        Log.d(TAG, "populateRecyclerView:" );

        RecyclerView rv = findViewById(R.id.rv);

        rv.setHasFixedSize(true);
        rv.setNestedScrollingEnabled(true);
        rv.getRecycledViewPool().setMaxRecycledViews(0, 0);

        final SwipeController swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onLeftClicked(int position) {
                //TODO
            }

            @Override
            public void onRightClicked(int position) {
                Log.d(TAG, "onRightClicked:" );
                clientOtherDialog = new Dialog(MainActivity.this);
                clientOtherDialog.setContentView(R.layout.dialog_altro);
                clientOtherDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                final Button btnAnnulla = clientOtherDialog.findViewById(R.id.btnAnnulla);
                final Button btnIncasso = clientOtherDialog.findViewById(R.id.btnIncasso);
                final Button btnComunicazioni = clientOtherDialog.findViewById(R.id.btnComunicazioni);
                final Button btnVariazione = clientOtherDialog.findViewById(R.id.btnVariazione);

                final Consegna item = consegnaAdapter.getItemByPosition(position);
                btnAnnulla.setOnClickListener(v -> clientOtherDialog.onBackPressed());
                btnVariazione.setOnClickListener(v -> {
                    clientOtherDialog.dismiss();
                    settingContexClientDialog(item);
                });

                btnIncasso.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, CameraCaptured.class);
                    intent.putExtra("consegna", item.getCliente());
                    intent.putExtra("isNew", true);

                    incassoLauncher.launch(intent);
                });

                btnComunicazioni.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Comunicazione");

                    final EditText input = new EditText(MainActivity.this);
                    setupMultilineEditText(input);

                    builder.setView(input);
                    builder.setPositiveButton("Ok", (dialog, whichButton) ->  {
                        final String commento = input.getText().toString().trim();
                        if (commento.isEmpty()) return;

                        Mail mail = new Mail(getBaseContext(), (message, result) -> mHandler.post(() -> {
                            lpb.hide();
                            if (result == 0) clientOtherDialog.onBackPressed();

                            //Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                            MainActivity.this.runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
                        }));
                        mail.setAddressTo("credito@saporiditoscana.com");
                        mail.setSubject("Comunicazione per il Cliente " + item.getCliente() + "[" + Gps.GetCurrentTimeStamp() + "]");
                        mail.setMessage(commento);

                        mail.SendMail();

                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                    AlertDialog alert = builder.create();
                    alert.show();
                });

                clientOtherDialog.show();
            }
        });

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(rv);

        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

        List<Consegna> consegnas = Consegna.GetLista(MainActivity.this);
        consegnaAdapter = new ConsegnaAdapter(consegnas, item -> {
            if (GetStatApp().id != STATO.UPLOAD_START_TRAVEL.id) return;

            settingContexClientDialog(item);
        });

        rv.setAdapter(consegnaAdapter);

    }

    private void setupMultilineEditText(EditText editText) {
        editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setSingleLine(false);
        editText.setLines(5);
        editText.setMaxLines(5);
        editText.setGravity(Gravity.START | Gravity.TOP);
    }

    @SuppressLint("SetTextI18n")
    private void settingContexClientDialog(Consegna item) {
        Log.d(TAG, "settingContexClientDialog:" + item);
        final boolean isNew = item.getIdEsitoConsegna()==0; //rv.getLayoutManager().findViewByPosition(position).getTag() == null;

        clientDialog = new Dialog(MainActivity.this);
        clientDialog.setContentView(R.layout.dialog_cliente);
        clientDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //recupero i campi del dialog
        TextView cdCliente = clientDialog.findViewById(R.id.fragmentCdCli);
        TextView ragioneSociale = clientDialog.findViewById(R.id.fragmentRagioneSoiale);
        TextView indirizzo = clientDialog.findViewById(R.id.fragmentIndirizzo);
        TextView documento =clientDialog.findViewById(R.id.fragmentDocumento);
        //valorizzo i campi
        cdCliente.setText(String.valueOf(item.getCdCli()));
        ragioneSociale.setText(item.getRagioneSociale());
        indirizzo.setText(item.getIndirizzo());
        documento.setText(item.getTipoDocumento() + " " + item.getNumeroDocumento());

        final Button btnOk = clientDialog.findViewById(R.id.btnOk);
        final Button btnMerceMancante = clientDialog.findViewById(R.id.btnMerceMancante);
        final Button btnMerceDanneggiata = clientDialog.findViewById(R.id.btnMerceDanneggiata);
        final Button btnAltro = clientDialog.findViewById(R.id.btnAltro);

        btnOk.setOnClickListener(v -> {
            AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, false);

            // Chiedo se è presente o meno il cliente
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.alterTitle);
            builder.setMessage(R.string.messaggio_conferma_cliente_lbl);
            builder.setNegativeButton(R.string.no, (dialog, which) -> {
                esitoConsegna = 5;
                startIntentCamera(item, esitoConsegna, "", isNew);
            });

            builder.setPositiveButton(R.string.si, (dialog, which) -> {
                esitoConsegna = 1;

                Mail mail = new Mail(getBaseContext(), (message, result) -> {
                    final String _message = message;
                    final Integer _result = result;
                    mHandler.post(() -> {
                        lpb.hide();
//                        Toast.makeText(MainActivity.this, _message, Toast.LENGTH_LONG).show();
                        MainActivity.this.runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
                        if (_result == 0) {
                            item.setFlInviato("S");
                            item.setIdEsitoConsegna(esitoConsegna);
                            item.setCommento("");
                            Consegna.Update(item, getBaseContext());

                            if (isNew)
                                Consegna.InsertConsegna(item, getBaseContext());
                            else
                                Consegna.UpdateConsegna(item, getBaseContext());

                            clientDialog.onBackPressed();
                            consegnaAdapter.Update(Consegna.GetLista(MainActivity.this));
                            if (consegnaAdapter.isCompleted()) {
                                Step.Update(STATO.UPLOAD_END_TRAVEL.id, MainActivity.this);
                                UpdateStastoView();
                            }
                        }
                        AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, true);
                    });
                });

                // mail.setAddressTo(item.getMailAge() + item.getMailVettore());
                Giro giro = new Giro(MainActivity.this);
                mail.setAddressTo("messaggioautomatico@saporiditoscana.com");
                mail.setSubject("Giro " + giro.getDsGiro().trim() + " - Cliente " + item.getCliente()  + "[" + Gps.GetCurrentTimeStamp()+"]# ");
                if (isNew)
                    mail.setMessage(getString(R.string.message_mail_consegna) + EsitoConsegna.GetTesto(getBaseContext(), 1));
                else
                    mail.setMessage(getString(R.string.message_mail_variazione) + EsitoConsegna.GetTesto(getBaseContext(), 1));

                mail.SendMail();
            });

            AlertDialog alert = builder.create();
            alert.show();
        });

        //merce mancante
        btnMerceMancante.setOnClickListener(v -> {
            AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, false);

            // Chiedo se è presente o meno il cliente
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.alterTitle);
            builder.setMessage(R.string.messaggio_conferma_cliente_lbl);
            builder.setNegativeButton(R.string.no, (dialog, which) -> {
                esitoConsegna = 6;
                startIntentCamera(item, esitoConsegna, "", isNew);
            });

            builder.setPositiveButton(R.string.si, (dialog, which) -> {
                esitoConsegna = 2;
                startIntentCamera(item, esitoConsegna, "", isNew);
            });

            AlertDialog alert = builder.create();
            alert.show();
        });


        //merce danneggiata
        btnMerceDanneggiata.setOnClickListener(v -> {
            AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, false);

            // Chiedo se è presente o meno il cliente
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.alterTitle);
            builder.setMessage(R.string.messaggio_conferma_cliente_lbl);
            builder.setNegativeButton(R.string.no, (dialog, which) -> {
                esitoConsegna = 7;
                startIntentCamera(item, esitoConsegna, "", isNew);
            });

            builder.setPositiveButton(R.string.si, (dialog, which) -> {
                esitoConsegna = 3;
                startIntentCamera(item, esitoConsegna, "", isNew);
            });

            AlertDialog alert = builder.create();
            alert.show();
        });

        //altro
        btnAltro.setOnClickListener(v -> {
            AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, false);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Comment");

            final EditText input = new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            input.setSingleLine(false);
            input.setLines(5);
            input.setMaxLines(5);
            input.setGravity(Gravity.START | Gravity.TOP);
            builder.setView(input);

            builder.setPositiveButton("Ok", (dialog, whichButton) -> {
                final String commento = input.getText().toString();
                if (commento.trim().isEmpty()) return;

                startIntentCamera(item, 4, commento, isNew);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
                AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, true);
            });

            AlertDialog alert = builder.create();
            alert.show();
        });

        clientDialog.show();
    }

    private void startIntentCamera(Consegna item, int esitoConsegna, String s, boolean isNew) {
        Step.Update(STATO.GET_PICTURE_CAMERA.id, MainActivity.this);
        Intent intent = getIntentCamera(item, esitoConsegna, s, isNew);
        cameraLauncher.launch(intent);
    }

    private Intent getIntentCamera(Consegna item, int esitoConsegna, String s, boolean isNew) {
//        Log.d(TAG, "getIntentCamera");
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Consegna.class, new ConsegnaSerializer());
        Gson gson = gsonBuilder.create();

        if (!isNew) {
            item.setFileBase64("");
            item.setFileType("");
            item.setFileName("");
        }

        Intent intent = new Intent(getBaseContext(), CameraCaptured.class);
        intent.putExtra("consegna", gson.toJson(item));
        intent.putExtra("esito", esitoConsegna);
        intent.putExtra("commento", s);
        intent.putExtra("isNew", isNew);
        return intent;
    }

    private void AbilitaEsiti(Button btnOk, Button btnMerceMancante, Button btnMerceDanneggiata, Button btnAltro, boolean b) {
        btnOk.setEnabled(b);
        btnMerceMancante.setEnabled(b);
        btnMerceDanneggiata.setEnabled(b);
        btnAltro.setEnabled(b);
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkGPS();

        //get fields on map
        dtConsegna.setText("");
        txGiro.setText("");

        populateRecyclerView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        if (!isStartStep1Called) {
            startStep1();
            isStartStep1Called = true;
        }

        InitToolBar("Home");

        mHandler = new Handler(Looper.getMainLooper());

        dtConsegna = findViewById(R.id.dtConsegna);
        btnDate = findViewById(R.id.btnDate);
        txGiro = findViewById(R.id.txGiro);

        lpb = findViewById(R.id.lpb);

        bab = findViewById(R.id.bottomAppBar);
        bab.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                handleBottomAppBarItemSelected(item);
                return true;
            }
        });

        dtConsegna.setKeyListener(null);
        dtConsegna.setOnClickListener(this::handleDateClickListener);
        btnDate.setOnClickListener(this::handleDateClickListener);

        incassoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::handleIncassoActivityResult);
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::handleCameraActivityResult);
        uploadLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::handleScanActivityResult);
    }


    private void handleBottomAppBarItemSelected(MenuItem menuItem) {
        Log.i(TAG, "handleBottomAppBarItemSelected: " + menuItem.getItemId());

        lpb.show();
        int itemId = menuItem.getItemId();

        if (itemId == R.id.settings) {
            MainActivity.this.runOnUiThread(() -> Toast.makeText(MainActivity.this, "Setting", Toast.LENGTH_SHORT).show());
//            Toast.makeText(MainActivity.this, "Setting", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
            lpb.hide();
        } else if (itemId == R.id.download) {
            if (stato.id <= STATO.UPLOAD_TRAVEL.id) {
                Terminale terminale = new Terminale(MainActivity.this);
                if (!(terminale.getIdConducente() != null && !terminale.getIdConducente().isEmpty() &&
                        terminale.getWebServer() != null && !terminale.getWebServer().isEmpty())) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.alterTitle);
                    builder.setMessage(R.string.erroreAcquisizionePianodicarico);
                    builder.setPositiveButton("OK", (dialog, which) -> startActivity(new Intent(MainActivity.this, SettingActivity.class)));
                    lpb.hide();
                    builder.show();
                }
                Step.Update(STATO.CONFIGURED.id, MainActivity.this);
                UpdateStastoView();

                if (!dtConsegna.getText().toString().isEmpty())
                    getJsonObjectAsync(terminale.getWebServerUrlErgon() + getString(R.string.GetGiroConsegna));
                else {
                    dtConsegna.setError("data consegna obbligatoria");
                    dtConsegna.setText("");
                    lpb.hide();
                }
            } else lpb.hide();
        } else if (itemId == R.id.upload) {
            lpb.hide();
            if (stato.id == STATO.UPLOAD_END_TRAVEL.id) {
                Intent intent = new Intent(getBaseContext(), UploadActivity.class);
                uploadLauncher.launch(intent);
            }
        } else if (itemId == R.id.travel) {
            menuItem.setEnabled(false);
            if (!mTracking && (Consegna.Check(MainActivity.this))) {
                startService(new Intent(getBaseContext(), LocationService.class));
                mTracking = true;
                Step.Update(STATO.UPLOAD_START_TRAVEL.id, MainActivity.this);
                UpdateStastoView();
                UpdateStartGiro();

                lpb.hide();
                MainActivity.this.runOnUiThread(() -> Toast.makeText(MainActivity.this, "Giro " + txGiro.getText() + " - partenza [" + Gps.GetCurrentTimeStamp() + "]", Toast.LENGTH_SHORT).show());

                menuItem.setEnabled(true);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.alterTitle);
                builder.setMessage(R.string.errorePartenzaGiro);
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                lpb.hide();
                builder.show();
                menuItem.setEnabled(true);
            }
        }

    }

    private void handleDateClickListener(View v) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, month, day) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, day);

            final String myFormat = "dd MMMM yyyy"; //In which you need put here

            runOnUiThread(() -> {
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ITALIAN);
                dtConsegna.setText(sdf.format(myCalendar.getTime()));
                dtConsegna.postInvalidate();
            });
        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void handleIncassoActivityResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            if (data == null) return;

            String consegna = data.getStringExtra("consegna");
            String image64 = data.getStringExtra("image64");

            Mail mail = new Mail(getBaseContext(), (message, res) -> {
                final String messagio = message;
                final Integer _result = res;
                mHandler.post(() -> {
                    lpb.hide();
                    if (_result == 0) {
                        clientOtherDialog.onBackPressed();
                    }
                    Toast.makeText(MainActivity.this, messagio, Toast.LENGTH_LONG).show();
                });
            });
            mail.setAddressTo("credito@saporiditoscana.com");
            mail.setSubject("Incasso - Cliente " + consegna + "[" + Gps.GetCurrentTimeStamp() + "]");
            mail.setMessage("Copia incasso");

            Attach attach = new Attach();
            attach.setFileName("foto.jpg");
            attach.setFileBase64(image64);
            attach.setMediaType("image/jpg");
            mail.AddAttach(attach);

            mail.SendMail();
        }
    }

    private void handleCameraActivityResult(ActivityResult result) {
        Log.d(TAG, "handleCameraActivityResult:" );
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            if (data == null) return;

            if (GetStatApp() == STATO.GET_PICTURE_CAMERA) {
                lpb.show();
                GsonBuilder  gsonBuilder= new GsonBuilder();
                gsonBuilder.registerTypeAdapter(Consegna.class, new ConsegnaDeserializer());
                Gson gsonResult = gsonBuilder.create();

                String c = data.getStringExtra("consegna");
                Log.d(TAG, "handleCameraActivityResult:" +c);

                Consegna consegna = gsonResult.fromJson(c, Consegna.class);

                String comment = data.getStringExtra("commento");
                String fileName = data.getStringExtra("filename");
                boolean isNew = data.getBooleanExtra("isNew", false);
                int esito = data.getIntExtra("esito", -1);
                String image64 = data.getStringExtra("image64");

                Mail mail = new Mail(getBaseContext(), (message, _result) -> {
                    final String _messagio = message;
                    mHandler.post(() -> {
                        lpb.hide();
                        Toast.makeText(MainActivity.this, _messagio, Toast.LENGTH_LONG).show();
                        if (_result == 0) {
                            consegna.setIdEsitoConsegna(esito);
                            consegna.setFlInviato("S");
                            consegna.setCommento(comment.trim());
                            consegna.setFileName(fileName);
                            consegna.setFileType("image/jpg");
                            consegna.setFileBase64(image64);
                            Consegna.Update(consegna, getBaseContext());

                            if (isNew) {
                                Consegna.InsertConsegna(consegna, getBaseContext());
                            } else {
                                Consegna.UpdateConsegna(consegna, getBaseContext());
                            }

                            Button btnOk = clientDialog.findViewById(R.id.btnOk);
                            Button btnMerceMancante = clientDialog.findViewById(R.id.btnMerceMancante);
                            Button btnMerceDanneggiata = clientDialog.findViewById(R.id.btnMerceDanneggiata);
                            Button btnAltro = clientDialog.findViewById(R.id.btnAltro);

                            clientDialog.onBackPressed();
                            consegnaAdapter.Update(Consegna.GetLista(MainActivity.this));
                            if (consegnaAdapter.isCompleted()) {
                                Step.Update(STATO.UPLOAD_END_TRAVEL.id, MainActivity.this);
                                UpdateStastoView();
                            }
                            AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, true);
                        }
                    });
                });

                if (esito != 5) {
                    mail.setAddressTo("problemaconsegna@saporiditoscana.com;");
                } else {
                    mail.setAddressTo("messaggioautomatico@saporiditoscana.com");
                }

                Giro giro = new Giro(MainActivity.this);
                mail.setSubject("Giro " + giro.getDsGiro().trim() + " - Cliente " + consegna.getCliente() + "[" + Gps.GetCurrentTimeStamp() + "]");

                if (isNew) {
                    mail.setMessage(getString(R.string.message_mail_consegna) + EsitoConsegna.GetTesto(getBaseContext(), esito));
                } else {
                    mail.setMessage(getString(R.string.message_mail_variazione) + EsitoConsegna.GetTesto(getBaseContext(), esito));
                }

                if (esito == 4) {
                    mail.setMessage(mail.getMessage().trim() + System.lineSeparator() + " commento: " + comment);
                }

                Attach attach = new Attach();
                attach.setFileName("foto.jpg");
                attach.setFileBase64(image64);
                attach.setMediaType("image/jpg");
                mail.AddAttach(attach);
                mail.SendMail();
            }
        } else {
            Button btnOk = clientDialog.findViewById(R.id.btnOk);
            Button btnMerceMancante = clientDialog.findViewById(R.id.btnMerceMancante);
            Button btnMerceDanneggiata = clientDialog.findViewById(R.id.btnMerceDanneggiata);
            Button btnAltro = clientDialog.findViewById(R.id.btnAltro);
            AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, true);
        }
    }

    private void handleScanActivityResult(ActivityResult result) {
        if (result.getResultCode() == RESULT_OK) {
            switch (GetStatApp()) {
                case UPLOAD_TRAVEL:
                    handleUploadTravel();
                    break;
                case UPLOAD_DETAIL_TRAVEL:
                    handleUploadDetailTravel();
                    break;
                case UPLOAD_START_TRAVEL:
                    handleUploadStartTravel();
                    break;
            }
        }
    }

    private void handleUploadTravel() {
        try {
            List<DbQuery> dbQueries = new ArrayList<>();
            if (qrCodeText != null && !qrCodeText.isEmpty()) {

                Terminale terminale = new Terminale(MainActivity.this);
                String[] strings = {};

                String[] dati = qrCodeText.split("\\§");
                if (dati.length > 1) {
                    String[] s = dati[0].split("\\#");
                    String sGiro = s[0];
                    SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd", Locale.ITALIAN);

                    if (giro == null) {
                        giro = new Giro(getBaseContext());
                    }

                    Date dtconsegna = f.parse(s[1]);
                    Date d = f.parse(giro.getDtConsegnayyyyMMdd());

                    if (!(giro.getCdGiro().equals(sGiro) && (dtconsegna != null ? dtconsegna.compareTo(d) : 0) == 0 && terminale.getIdConducente().equals(s[2]))) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(R.string.alterTitle);
                        builder.setMessage(R.string.messaggio_giro_errato);
                        builder.setPositiveButton(R.string.si, (dialog, which) -> dialog.dismiss());
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        strings = dati[1].split("\\#");
                    }
                } else {
                    strings = dati[0].split("\\#");
                }

                if (strings.length == 0) {
                    strings = qrCodeText.split("\\#");
                }

                for (String string : strings) {
                    int annoReg = 2000;
                    int nrReg;
                    annoReg += Integer.parseInt(string.substring(0, 2));
                    nrReg = Integer.parseInt(string.substring(2));

                    dbQueries.add(Consegna.Insert(annoReg, nrReg));
                }

                if (Consegna.Insert(dbQueries, MainActivity.this)) {
                    Step.Update(STATO.UPLOAD_DETAIL_TRAVEL.id, MainActivity.this);
                    UpdateStastoView();
                }

                for (String string : strings) {
                    getTravelConsegnaAsync(terminale.getWebServerUrlErgon() + getString(R.string.GetTravelConsegna), string);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "one error occurred on UPLOAD_TRAVEL: " + e.getLocalizedMessage());
        }
    }

    private void handleUploadDetailTravel() {
        Terminale terminale = new Terminale(MainActivity.this);
        if (qrCodeText != null && !qrCodeText.isEmpty()) {
            getTravelConsegnaAsync(terminale.getWebServerUrlErgon() + getString(R.string.GetTravelConsegna), qrCodeText);
        }
    }

    private void handleUploadStartTravel() {
        if (qrCodeText != null && !qrCodeText.isEmpty()) {
            RecyclerView rv = findViewById(R.id.rv);
            int position = consegnaAdapter.findItem(qrCodeText);
            Objects.requireNonNull(rv.findViewHolderForAdapterPosition(position)).itemView.performClick();
        }
    }
    private void initializeViewGiro() {
        if (this.stato.id <= STATO.CONFIGURED.id) return;

        giro = new Giro(MainActivity.this);

        if (giro != null) {
            try {
                dtConsegna.setText(giro.getDtConsegnaFormatted());
                txGiro.setText(giro.getDsGiro());
                dtConsegna.setEnabled(false);
            } catch (Exception e) {
                Log.e(TAG, "one error on initializeViewGiro occurred: " + e.getMessage());
                dtConsegna.setEnabled(true);
            }
        }
    }

    private void getJsonObjectAsync(String url) {
        try {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            Terminale terminale = new Terminale(MainActivity.this);

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy",Locale.ITALIAN);
            Date d = new Date();
            try{
                d = sdf.parse(dtConsegna.getText().toString());
            }
            catch (Exception e){
                Log.e(TAG, "one error on getJsonObjectAsync occurred: " + e.getMessage());
            }
            SimpleDateFormat jsonDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); // from java.text.SimpleDateFormat
            assert d != null;
            String senddate = jsonDateFormat.format(d.getTime());

            JsonObject json = new JsonObject();
            json.addProperty("DtConsegna", senddate);
            json.addProperty("IdConducente", terminale.getIdConducente());
            json.addProperty("IdDevice", terminale.getId());

            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            urlBuilder.addQueryParameter("StringSearch", json.toString());
            url = urlBuilder.build().toString();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    mHandler.post(() -> lpb.hide());
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    try{
                        String jsonData = response.body().string();

                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.registerTypeAdapter(Result.class, new ResultDeserializer());
                        Gson gsonResult = gsonBuilder.create();
                        final Result result = gsonResult.fromJson(jsonData, Result.class);

                        if (!result.getData().toString().isEmpty()) {
                            try {
                                gsonBuilder = new GsonBuilder();
                                gsonBuilder.registerTypeAdapter(Giro.class, new GiroDeserializer());
                                gsonResult = gsonBuilder.create();

                                final Giro giro = gsonResult.fromJson(result.getData(), Giro.class);

                                if (giro.getCdGiro() == null) {
                                    mHandler.post(() -> Toast.makeText(MainActivity.this, "Nessun giro assegnato all'autista selezionato.", Toast.LENGTH_SHORT).show());
                                    return;
                                }
                                if (Giro.Insert(giro, MainActivity.this)) {
                                    Step.Update(STATO.UPLOAD_TRAVEL.id, MainActivity.this);
                                    UpdateStastoView();
                                    initializeViewGiro();

                                    getTravelConsegnaCollectionAsync(terminale.getWebServerUrlErgon() + getString(R.string.GetTravelConsegnaCollection), giro);
                                }
                            }catch(Exception e){
                                Log.e(TAG, "On error occurred: " + e.getLocalizedMessage());
                            }
                        }

                        MainActivity.this.runOnUiThread(() -> {
                            txGiro.setText(new  Giro(MainActivity.this).getDsGiro());
                            dtConsegna.setEnabled(false);
                            if (txGiro != null &&  !txGiro.toString().isEmpty())
                                Toast.makeText(MainActivity.this, "Piano di Carico acquisito correttamente", Toast.LENGTH_SHORT).show();
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "one error1 on getJsonObjectAsync occurred: " + e.getMessage());
                        if (MainActivity.this != null) Toast.makeText(MainActivity.this, "Piano di Carico non acquisibile", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (Exception e){
            Log.e(TAG, "one error2 on getJsonObjectAsync occurred: " + e.getMessage());
            if (MainActivity.this != null) Toast.makeText(MainActivity.this, "Piano di Carico non acquisibile", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkGPS() {

        //Check whether GPS tracking is enabled//
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!(lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.alterTitle);
            builder.setMessage(R.string.gpsnodisp);
            builder.setPositiveButton("OK", (dialog, which) -> startActivity(new Intent(MainActivity.this, SettingActivity.class)));
            builder.show();
//            finish();
        }
    }

    private boolean isGooglePlayServicesAvailable(){
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode  = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS){
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else{
                finish();
            }
            return false;
        }
        return true;
    }


    public void InitToolBar(String subtitle) {
        Toolbar tb = findViewById(R.id.topAppBar);
        tb.setTitle("Travel - " + subtitle);
        TextView tv = findViewById(R.id.stato);
        tv.setVisibility(View.GONE);
        if(subtitle.equals("Home")) {
            tb.setNavigationIcon(null);
            tv.setVisibility(View.VISIBLE);
        }
        setSupportActionBar(tb);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem =  menu.findItem(R.id.action_save);
        menuItem.setVisible(false);
        menuItem =  menu.findItem(R.id.actio_ripristina);
        menuItem.setVisible(false);
        menuItem =  menu.findItem(R.id.action_info);
        menuItem.setVisible(false);
        return true;
    }

    @Override
    public  boolean onOptionsItemSelected(MenuItem item){
        //scatenato dalla toolbar
        int id = item.getItemId();
        if (id == R.id.action_save) {
            EditText idTerminale = findViewById(R.id.txTerminale);

            Toast.makeText(MainActivity.this, "Saved" + idTerminale.getText().toString().trim(), Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void getTravelConsegnaAsync(String url, String qr) {
        lpb.show();
        try {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            int annoReg = 2000;
            int nrReg;
            annoReg += Integer.parseInt(qr.substring(0, 2));
            nrReg = Integer.parseInt(qr.substring(2));

            JsonObject json = new JsonObject();
            json.addProperty("pAnnoReg", annoReg);
            json.addProperty("pNrReg", nrReg);

            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            urlBuilder.addQueryParameter("StringSearch", json.toString());
            url = urlBuilder.build().toString();

            OkHttpClient client = new OkHttpClient();Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    mHandler.post(() -> lpb.hide());
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    try{
                        String jsonData = response.body().string();

                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.registerTypeAdapter(Result.class, new ResultDeserializer());
                        Gson gsonResult = gsonBuilder.create();
                        final Result result = gsonResult.fromJson(jsonData, Result.class);

                        if (!result.getData().toString().isEmpty()) {
                            gsonBuilder = new GsonBuilder();
                            gsonBuilder.registerTypeAdapter(Consegna.class, new ConsegnaDeserializer());
                            gsonResult = gsonBuilder.create();
                            final Consegna consegna = gsonResult.fromJson(result.getData().toString(), Consegna.class);

                            if (consegna.getCdCli()>0) {

                                if (!Consegna.Update(consegna, MainActivity.this)){

                                    mHandler.post(() -> {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle(R.string.alterTitle);
                                        builder.setMessage(R.string.documentonp);
                                        builder.setPositiveButton("OK", (dialog, which) -> {

                                        });
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    });

                                }

                                //controllare se tutte le registrazione sono stato caricate ed aggiornare lo stato
                                if (Consegna.Check(MainActivity.this)) {
                                    Step.Update(STATO.TRAVEL_COMPLETED.id, MainActivity.this);
                                    UpdateStastoView();
                                }
                            }
                        }

                        //caricare la listview
                        MainActivity.this.runOnUiThread(MainActivity.this::populateRecyclerView);

                    } catch (Exception e) {
                        Log.e(TAG, "one error on getTravelConsegnaAsync occurred: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Piano di Carico non acquisibile", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (Exception e){
            Log.e(TAG, "one error1 on getTravelConsegnaAsync occurred: " + e.getMessage());
            Toast.makeText(MainActivity.this, "Piano di Carico non acquisibile", Toast.LENGTH_SHORT).show();
        }
    }
    private void getTravelConsegnaCollectionAsync(String url, Giro giro) {
        lpb.show();
        try {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            JsonObject json = new JsonObject();
            json.addProperty("pCodGiro", giro.getCdGiro());
            json.addProperty("pDtConsegna", giro.getDtConsegnaddMMyyyy());

            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            urlBuilder.addQueryParameter("StringSearch", json.toString());
            url = urlBuilder.build().toString();

            OkHttpClient client = new OkHttpClient();Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    mHandler.post(() -> lpb.hide());
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                    try{
                        String jsonData = response.body().string();

                        //Gson gson = new Gson();
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.registerTypeAdapter(Result.class, new ResultDeserializer());
                        Gson gsonResult = gsonBuilder.create();
                        final Result result   =  gsonResult.fromJson(jsonData, Result.class);

                        if (!result.getData().toString().isEmpty()) {
                            gsonBuilder = new GsonBuilder();
                            gsonBuilder.registerTypeAdapter(Consegna.class, new ConsegnaDeserializer());
                            gsonResult = gsonBuilder.create();
                            Type listType = new ParameterizedType() {
                                public Type[] getActualTypeArguments() {
                                    return new Type[] { Consegna.class };
                                }
                                public Type getRawType() {
                                    return List.class;
                                }
                                public Type getOwnerType() {
                                    return null;
                                }
                            };

                            List<Consegna> consegnas = gsonResult.fromJson(result.getData().toString(), listType);
//                            List<Consegna> consegnas = gsonResult.fromJson(result.getData().toString(), new ArrayList<Consegna>().getClass().getGenericSuperclass());

                            if (consegnas.size()>0) {
                                List<DbQuery> dbQueries = new ArrayList<>();
                                for (int i = 0; i < consegnas.size() ; i++)
                                {
                                    final Consegna c = consegnas.get(i);
                                    dbQueries.add(Consegna.Insert(c.getAnnoReg(),c.getNrReg() ));
                                }
                                if (Consegna.Insert(dbQueries, MainActivity.this)) {
                                    Step.Update(STATO.UPLOAD_DETAIL_TRAVEL.id, MainActivity.this);
                                    UpdateStastoView();
                                }
                                for (int i = 0; i < consegnas.size() ; i++)
                                {
                                    if (!Consegna.Update(consegnas.get(i), MainActivity.this)){

                                        mHandler.post(() -> {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                            builder.setTitle(R.string.alterTitle);
                                            builder.setMessage(R.string.documentonp);
                                            builder.setPositiveButton("OK", (dialog, which) -> {

                                            });
                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        });

                                    }

                                }
                                if (Consegna.Insert(dbQueries, MainActivity.this)) {
                                    Step.Update(STATO.UPLOAD_DETAIL_TRAVEL.id, MainActivity.this);
                                    UpdateStastoView();
                                }
                                //controllare se tutte le registrazione sono stato caricate ed aggiornare lo stato
                                if (Consegna.Check(MainActivity.this)) {
                                    Step.Update(STATO.TRAVEL_COMPLETED.id, MainActivity.this);
                                    UpdateStastoView();
                                }

                            }
                        }

                        //caricare la listview
                        MainActivity.this.runOnUiThread(MainActivity.this::populateRecyclerView);

                    } catch (Exception e) {
                        Log.e(TAG, "one error on getTravelConsegnaAsync occurred: " + e.getMessage());

                        MainActivity.this.runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Piano di Carico non acquisibile", Toast.LENGTH_SHORT).show();
                        });

                    }
                }
            });
        }catch (Exception e){
            Log.e(TAG, "one error1 on getTravelConsegnaAsync occurred: " + e.getMessage());
            Toast.makeText(MainActivity.this, "Piano di Carico non acquisibile", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                BaseTransientBottomBar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    public void UpdateStartGiro() {
        try {
            final MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

            Terminale terminale = new Terminale(getBaseContext());
            String url = terminale.getWebServerUrlErgon() + "StartGiroConsegna";

            Giro giro = new Giro(MainActivity.this);


            JsonObject json = new JsonObject();
            json.addProperty("CdDep", giro.getCdDep());
            json.addProperty("CdGiro", giro.getCdGiro());
            json.addProperty("DtConsegna", giro.getDtConsegnaddMMyyyy());
            json.addProperty("TsStart", Gps.GetCurrentTimeStamp());

            HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
            urlBuilder.addQueryParameter("EditJson", json.toString());
            url = urlBuilder.build().toString();


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull final Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();

//                        Gson gson = new Gson();
                        GsonBuilder gsonBuilder = new GsonBuilder();
                        gsonBuilder.registerTypeAdapter(Result.class, new ResultDeserializer());
                        Gson gsonResult = gsonBuilder.create();
                        final Result result = gsonResult.fromJson(jsonData, Result.class);

                        if (result.Error != null)
                            Log.e(TAG, result.getError().toString());

                    } catch (Exception e) {
                        Log.e(TAG, "one error occurred [UpdateStartGiro 2]: " + e.getLocalizedMessage());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "one error occurred [UpdateStartGiro 1]: " + e.getLocalizedMessage());
        }
    }

    //Callback received when a permissions request has been completed.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,                                           @NonNull int[] grantResults) {
//        Log.i(TAG, "onRequestPermissionResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permesso concesso, puoi eseguire le azioni corrispondenti
            } else {
                // Permesso negato, mostra un messaggio all'utente e offri la possibilità di aprire le impostazioni dell'app
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.permission_denied_explanation, Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.settings, view -> openAppSettings());
                snackbar.show();
            }
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

}