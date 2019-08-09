package com.saporiditoscana.travel;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.saporiditoscana.travel.DbHelper.DbManager;
import com.saporiditoscana.travel.DbHelper.DbQuery;
import com.saporiditoscana.travel.Orm.Attach;
import com.saporiditoscana.travel.Orm.Consegna;

import com.saporiditoscana.travel.Orm.EsitoConsegna;
import com.saporiditoscana.travel.Orm.Giro;
import com.saporiditoscana.travel.Orm.Gps;
import com.saporiditoscana.travel.Orm.Mail;
import com.saporiditoscana.travel.Orm.Step;
import com.saporiditoscana.travel.Orm.Terminale;
import com.saporiditoscana.travel.Services.LocationMonitoringService;
import com.saporiditoscana.travel.Services.LocationService;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity  extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public interface OnInitToolbar{
        String InitToolbar(String subtitle);
    }

    public static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    static final int CAMERA_REQUEST = 1;  // The request code
    static final int SCAN_REQUEST = 2;  // The request code
    static final int UPLOAD_REQUEST = 3;  // The request code
    static final int RESULT_OK = 0;
    private boolean mTracking = false;
    static int esitoConsegna = 0;

    private DbManager db;
    private ConsegnaAdapter consegnaAdapter;
    private RecyclerView rv;
    public static String qrCodeText;
    public static EditText dtConsegna ;
    public static TextView txGiro;
    public Dialog clientDialog;
    public Giro giro;

    private Handler mHandler;
    private ContentLoadingProgressBar lpb;

    final Calendar myCalendar=Calendar.getInstance();
    FloatingActionButton fab;
    BottomAppBar bab;

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

        private String testo;
        private int id;

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

        checkGPS();

        startStep1();

        GetStatApp();
        //devo leggere la tabella step per sapere in che stato è l'applicazione
        UpdateStastoView();

        initializeViewGiro();
        switch (this.stato) {
            case CONFIGURED:
                dtConsegna.setEnabled(true);
                break;
            case UPLOAD_TRAVEL:
//                initializeViewGiro();
                break;
            case UPLOAD_DETAIL_TRAVEL:
//                initializeViewGiro();
                if (Consegna.Check(MainActivity.this)) {
                    Step.Update(STATO.TRAVEL_COMPLETED.id, MainActivity.this);
                    this.stato = STATO.TRAVEL_COMPLETED;
                }
                break;
            case TRAVEL_COMPLETED:
                //ho caricato tutte le bolle
//                initializeViewGiro();
                break;
            case UPLOAD_START_TRAVEL:
//                initializeViewGiro();
                dtConsegna.setEnabled(false);
                chekTracking();
                if (consegnaAdapter != null && consegnaAdapter.isCompleted()) {
                    Step.Update(STATO.UPLOAD_END_TRAVEL.id, this);
                    UpdateStastoView();
                }
                break;
            case UPLOAD_END_TRAVEL:
//                initializeViewGiro();
                dtConsegna.setEnabled(false);
                chekTracking();
                break;
            case UPLOAD_DOWNLOAD_TRAVEL:
//                initializeViewGiro();
                dtConsegna.setEnabled(false);
                chekTracking();
                break;
            case GET_PICTURE_CAMERA:
//                initializeViewGiro();
                dtConsegna.setEnabled(false);
                Step.Update(STATO.UPLOAD_START_TRAVEL.id,MainActivity.this);
            default:
                dtConsegna.setEnabled(true);
                break;
        }
    }

    private void chekTracking() {
        if (!mTracking) {
//            startService(new Intent(getBaseContext(), LocationMonitoringService.class));
            startService(new Intent(getBaseContext(), LocationService.class));
            mTracking = true;
        }
    }

    private void UpdateStastoView() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                TextView tvStato = findViewById(R.id.stato);
                tvStato.setText(GetStatApp().testo);
            }
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
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(MainActivity.this, SettingActivity.class));
                }
            });
            builder.show();
        }
    }

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
        if (!checkPermissions())
            requestPermissions();

        return true;
    }

    private boolean hasConnection(){
        ConnectivityManager connectivityManager  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected())
            return false;
        else return true;
    }

    private void requestPermissions() {

        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        boolean shouldProvideRationale2 = ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);

        boolean shouldProvideRationale3 = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        boolean shouldProvideRationale4 = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        boolean shouldProvideRationale5 = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA);

        boolean shouldProvideRationale6 = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.INTERNET);


        // Provide an additional rationale to the img_user. This would happen if the img_user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale || shouldProvideRationale2 || shouldProvideRationale3 ||
                shouldProvideRationale4 || shouldProvideRationale5 || shouldProvideRationale6 )  {
//            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                            Manifest.permission.READ_EXTERNAL_STORAGE,
                                            Manifest.permission.CAMERA, Manifest.permission.INTERNET
                                    },
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
//            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the img_user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA, Manifest.permission.INTERNET
                    },
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }

    }

    private void promptInternetConnect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.title_alert_no_intenet);
        builder.setMessage(R.string.msg_alert_no_internet);

        String positiveText = getString(R.string.btn_label_refresh);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Block the Application Execution until user grants the permissions
                        if (startStep2(dialog)) {
                            //Now make sure about location permission.
                            if (!checkPermissions()) {
                                requestPermissions();
                            }

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

        int permissionState3 = -1;
        if (Build.VERSION.SDK_INT < 23)  permissionState3 = 0;
        else permissionState3 =  ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET);

        int permissionState4 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionState5 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionState6 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionState7 = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);

        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED &&
                permissionState3 == PackageManager.PERMISSION_GRANTED && permissionState4 == PackageManager.PERMISSION_GRANTED &&
                permissionState5 == PackageManager.PERMISSION_GRANTED && permissionState6 == PackageManager.PERMISSION_GRANTED &&
                permissionState7 == PackageManager.PERMISSION_GRANTED;
    }

    private void populateRecyclerView() {
        List<Consegna> consegnas = Consegna.GetLista(MainActivity.this);
        if (consegnas.size() > 0) {
            rv = findViewById(R.id.rv);

            if (rv != null) {
                rv.setNestedScrollingEnabled(true);
                consegnaAdapter = new ConsegnaAdapter(consegnas, new ConsegnaAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(final Consegna item) {
                        if (GetStatApp().id != STATO.UPLOAD_START_TRAVEL.id) return;

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
                        documento.setText(item.getTipoDocumento() + " " + String.valueOf(item.getNumeroDocumento()));

                        final Button btnOk = (Button) clientDialog.findViewById(R.id.btnOk);
                        final Button btnMerceMancante = (Button) clientDialog.findViewById(R.id.btnMerceMancante);
                        final Button btnMerceDanneggiata = (Button) clientDialog.findViewById(R.id.btnMerceDanneggiata);
                        final Button btnAltro = (Button) clientDialog.findViewById(R.id.btnAltro);

                        btnOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, false);

                            //Chiedo se è presente o meno il cliente
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(R.string.alterTitle);
                            builder.setMessage(R.string.messaggio_conferma_cliente_lbl);
                            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                esitoConsegna = 5;
                                Step.Update(STATO.GET_PICTURE_CAMERA.id,MainActivity.this);
                                Gson gson = new Gson();

                                Intent intent = new Intent(getBaseContext(),  CameraCaptured.class);
                                intent.putExtra("consegna", gson.toJson(item));
                                intent.putExtra("esito", esitoConsegna);
                                intent.putExtra("commento", "");
                                startActivityForResult(intent,CAMERA_REQUEST);
                                }
                            });

                            builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                esitoConsegna = 1;

                                Mail mail = new Mail(getBaseContext(), new Mail.Completed() {
                                    @Override
                                    public void callback(final String message, Integer result) {
                                        final String _message = message;
                                        final Integer _result = result;
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                lpb.hide();
                                                if (_result == 0) {
                                                    item.setFlInviato("S");
                                                    item.setIdEsitoConsegna(esitoConsegna);
                                                    item.setCommento("");
                                                    Consegna.Update(item, getBaseContext());
                                                    Consegna.InsertConsegna(item, getBaseContext());//scrivo sul db del server

                                                    clientDialog.onBackPressed();
                                                    consegnaAdapter.Update(Consegna.GetLista(MainActivity.this));
                                                    if (consegnaAdapter.isCompleted()) {
                                                        Step.Update(STATO.UPLOAD_END_TRAVEL.id, MainActivity.this);
                                                        UpdateStastoView();
                                                    }
                                                }
                                                AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, true);
                                                Toast.makeText(getApplicationContext(), _message, Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                });
                                mail.setAddressTo(item.getMailAge() + item.getMailVettore());
                                mail.setAddressCc("logistica@saporiditoscana.com");
//                                mail.setSubject("Giro " + ((TextView)findViewById(R.id.txGiro)).getText() + " - Cliente " + item.getCliente()  + "[" + Gps.GetCurrentTimeStamp()+"]# ");
                                mail.setSubject("Giro " + giro.getDsGiro().trim() + " - Cliente " + item.getCliente()  + "[" + Gps.GetCurrentTimeStamp()+"]# ");
                                mail.setMessage("Consegna effettuata con esito: " + EsitoConsegna.GetTesto(getBaseContext(), 1));
                                mail.SendMail();
                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                            }
                        });

                        //merce mancante
                        btnMerceMancante.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, false);

                                //Chiedo se è presente o meno il cliente
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle(R.string.alterTitle);
                                builder.setMessage(R.string.messaggio_conferma_cliente_lbl);
                                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    esitoConsegna = 6;

                                    Step.Update(STATO.GET_PICTURE_CAMERA.id,MainActivity.this);
                                    Gson gson = new Gson();

                                    Intent intent = new Intent(getBaseContext(),  CameraCaptured.class);
                                    intent.putExtra("consegna", gson.toJson(item));
                                    intent.putExtra("esito", esitoConsegna);
                                    intent.putExtra("commento", "");
                                    startActivityForResult(intent,CAMERA_REQUEST);
                                    }
                                });

                                builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    esitoConsegna = 2;

                                    Step.Update(STATO.GET_PICTURE_CAMERA.id,MainActivity.this);
                                    Gson gson = new Gson();

                                    Intent intent = new Intent(getBaseContext(),  CameraCaptured.class);
                                    intent.putExtra("consegna", gson.toJson(item));
                                    intent.putExtra("esito", esitoConsegna);
                                    intent.putExtra("commento", "");
                                    startActivityForResult(intent,CAMERA_REQUEST);
                                    }
                                });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });

                        //merce danneggiata
                        btnMerceDanneggiata.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, false);

                                //Chiedo se è presente o meno il cliente
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle(R.string.alterTitle);
                                builder.setMessage(R.string.messaggio_conferma_cliente_lbl);
                                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    esitoConsegna = 7;

                                    Step.Update(STATO.GET_PICTURE_CAMERA.id,MainActivity.this);
                                    Gson gson = new Gson();

                                    Intent intent = new Intent(getBaseContext(),  CameraCaptured.class);
                                    intent.putExtra("consegna", gson.toJson(item));
                                    intent.putExtra("esito", esitoConsegna);
                                    intent.putExtra("commento", "");
                                    startActivityForResult(intent,CAMERA_REQUEST);
                                    }
                                });

                                builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    esitoConsegna = 3;

                                    Step.Update(STATO.GET_PICTURE_CAMERA.id,MainActivity.this);
                                    Gson gson = new Gson();

                                    Intent intent = new Intent(getBaseContext(),  CameraCaptured.class);
                                    intent.putExtra("consegna", gson.toJson(item));
                                    intent.putExtra("esito", esitoConsegna);
                                    intent.putExtra("commento", "");
                                    startActivityForResult(intent,CAMERA_REQUEST);
                                    }
                                });

                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });

                        //altro
                        btnAltro.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, false);

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Comment");

                                final EditText input = new EditText(MainActivity.this);

                                input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                                input.setSingleLine(false);
                                input.setLines(5);
                                input.setMaxLines(5);
                                input.setGravity(Gravity.LEFT | Gravity.TOP);
                                builder.setView(input);

                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    final String commento = input.getText().toString();
                                    if (commento.trim().isEmpty()) return;

                                    Step.Update(STATO.GET_PICTURE_CAMERA.id,MainActivity.this);
                                    Gson gson = new Gson();

                                    Intent intent = new Intent(getBaseContext(),  CameraCaptured.class);
                                    intent.putExtra("consegna", gson.toJson(item));
                                    intent.putExtra("esito", 4);
                                    intent.putExtra("commento", commento);
                                    startActivityForResult(intent,CAMERA_REQUEST);
                                    }
                                });

                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, true);
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });

                        clientDialog.show();
                    }
                });
                rv.setAdapter(consegnaAdapter);
                final SwipeController swipeController = new SwipeController(new SwipeControllerActions() {
                    @Override
                    public void onLeftClicked(int position) {
                        //TODO
                    }

                    @Override
                    public void onRightClicked(int position) {
                        //TODO
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
            }
        }
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

        //get fields on map
        dtConsegna.setText("");
        txGiro.setText("");

        populateRecyclerView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db=new DbManager(this);
        mHandler = new Handler(Looper.getMainLooper());

        dtConsegna = findViewById(R.id.dtConsegna);
        txGiro = findViewById(R.id.txGiro);

        lpb = findViewById(R.id.lpb);

        bab = findViewById(R.id.bottomAppBar);
        //setting menu in bar bottom
        bab.replaceMenu(R.menu.menu_navigation);

        bab.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final MenuItem item) {
                lpb.show();
                switch (item.getItemId()){
                    case R.id.settings:
                        Toast.makeText(MainActivity.this,"Setting", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                        lpb.hide();
                        break;
                    case R.id.download:
                        //deve scaricare i dati dal piano di carico
                        if (stato.id <= STATO.UPLOAD_TRAVEL.id) {
                            Terminale terminale = new Terminale(MainActivity.this);
                            if (!(terminale.getIdConducente() != null && !terminale.getIdConducente().isEmpty() &&
                                    terminale.getWebServer() != null && !terminale.getWebServer().isEmpty())) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle(R.string.alterTitle);
                                builder.setMessage(R.string.erroreAcquisizionePianodicarico);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(MainActivity.this, SettingActivity.class));
                                    }
                                });
                                lpb.hide();
                                builder.show();
                                break;
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
                        }else lpb.hide();
                        break;
                    case R.id.upload:
                        //start activity to upload data
                        if (stato.id == STATO.UPLOAD_END_TRAVEL.id) {
                            Intent intent = new Intent(getBaseContext(), UploadActivity.class);
                            startActivityForResult(intent, UPLOAD_REQUEST);
                        }
                        break;
                    case R.id.travel:
                        item.setEnabled(false);
                        if (!mTracking && (Consegna.Check(MainActivity.this))) {
                            //invio mail di inizio giro
                            Mail mail = new Mail(getBaseContext(), new Mail.Completed() {
                                @Override
                                public void callback(String message, Integer result) {
                                    final String _message = message;
                                    final Integer _result = result;

                                    try{lpb.hide();}catch (Exception e){}

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            lpb.hide();
                                            item.setEnabled(true);

                                            if (_result ==0 ) {
                                                //Start location sharing service
                                                startService(new Intent(getBaseContext(), LocationService.class));
                                                mTracking = true;
                                                Step.Update(STATO.UPLOAD_START_TRAVEL.id, MainActivity.this);
                                                UpdateStastoView();
                                                UpdateStartGiro();
                                            }

                                            Toast.makeText(MainActivity.this, _message, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                            mail.setAddressTo(Consegna.GetMailCapoArea(getBaseContext()));
                            mail.setAddressCc(Consegna.GetMailAgente(getBaseContext()) + Consegna.GetMailVettore(getBaseContext()) + "logistica@saporiditoscana.com;");
                            mail.setAddressCcn("agiannetti@saporiditoscana.com");
//                            mail.setSubject("Giro " + ((TextView)findViewById(R.id.txGiro)).getText() + " - partenza [" + Gps.GetCurrentTimeStamp()+"]");
                            mail.setSubject("Giro " + giro.getDsGiro() + " - partenza [" + Gps.GetCurrentTimeStamp()+"]");
                            mail.setMessage("");
                            mail.SendMail();
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(R.string.alterTitle);
                            builder.setMessage(R.string.errorePartenzaGiro);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            lpb.hide();
                            builder.show();
                            item.setEnabled(true);
                        }
                        break;
                }
                return true;
            }
        });


        fab = findViewById(R.id.fab);
//      add listener for float button
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean abilitaCameraScanCode = false;
                switch (GetStatApp()) {
                    case UPLOAD_TRAVEL:
                    case UPLOAD_DETAIL_TRAVEL:
                    case UPLOAD_START_TRAVEL:
                        abilitaCameraScanCode =true;
                        break;
                    default:
                        abilitaCameraScanCode=false;
                }

                if (abilitaCameraScanCode) {
                    qrCodeText = "";
                    Intent intent = new Intent(getApplicationContext(), ScanCodeActivity.class);
                    startActivityForResult(intent, SCAN_REQUEST);
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.alterTitle);
                    builder.setMessage(getString(R.string.camerenodisp) + GetStatApp().testo);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (GetStatApp().id <= STATO.CONFIGURED.id)
                                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                        }
                    });
                    builder.show();
                }
            }
        });

        InitToolBar("Home");

        dtConsegna.setKeyListener(null);
        dtConsegna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.MONTH, month);
                                myCalendar.set(Calendar.DAY_OF_MONTH, day);

                                final String myFormat = "dd MMMM yyyy"; //In which you need put here

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ITALIAN);
                                        dtConsegna = findViewById(R.id.dtConsegna);
                                        dtConsegna.setText(sdf.format(myCalendar.getTime()));
                                        dtConsegna.postInvalidate();
                                    }
                                });

                            }
                        }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));

                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });
    }

    private void initializeViewGiro() {
//        Logger.d(TAG, "initializeViewGiro STARTED");
//        Logger.d(TAG, "stato: "+ this.stato.id + " - " + this.stato.testo);
        if (this.stato.id <= STATO.CONFIGURED.id) return;

        giro = new Giro(MainActivity.this);

        if (giro != null) {
            try {
                dtConsegna.setText(giro.getDtConsegnaFormatted());
                txGiro.setText(giro.getDsGiro());
                dtConsegna.setEnabled(false);
//                Logger.d(TAG, "dtConsegna: "+ dtConsegna.getText());
//                Logger.d(TAG, "txGiro: "+ txGiro.getText());
            } catch (Exception e) {
                Logger.e(TAG, "one error on initializeViewGiro occurred: " + e.getMessage());
                dtConsegna.setEnabled(true);
            }
        }
//        Logger.d(TAG, "initializeViewGiro END");
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
                Logger.e(TAG, "one error on getJsonObjectAsync occurred: " + e.getMessage());
            }
            SimpleDateFormat jsonDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); // from java.text.SimpleDateFormat
            String senddate = jsonDateFormat.format(d.getTime());

            JsonObject json = new JsonObject();
            json.addProperty("DtConsegna", senddate);
            json.addProperty("IdConducente", terminale.getIdConducente());
            json.addProperty("IdDevice", terminale.getId());

            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            urlBuilder.addQueryParameter("StringSearch", json.toString());
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
                        String jsonData = response.body().string();

                        Gson gson = new Gson();
                        final Result result   =  gson.fromJson(jsonData, Result.class);


                        if (!result.getData().toString().isEmpty()) {
                            try {
                                Type type = new TypeToken<Giro>() {}.getType();
                                final Giro giro = gson.fromJson(result.getData().toString(), type);

                                if (giro.getCdGiro() == null) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "Nessun giro assegnato all'autista selezionato.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }
                                if (Giro.Insert(giro, MainActivity.this)) {
                                    Step.Update(STATO.UPLOAD_TRAVEL.id, MainActivity.this);
                                    UpdateStastoView();
                                    initializeViewGiro();
                                }
                            }catch(Exception e){
                                Logger.e(TAG, "On error occurred: " + e.getLocalizedMessage());
                            }
                        }

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                TextView txGiro;
//                                txGiro =findViewById(R.id.txGiro);
                                txGiro.setText(new  Giro(MainActivity.this).getDsGiro());
                                dtConsegna.setEnabled(false);
                                if (txGiro != null &&  !txGiro.toString().isEmpty())
                                    Toast.makeText(MainActivity.this, "Piano di Carico acquisito correttamente", Toast.LENGTH_SHORT).show();
                            }
                        });

                    } catch (Exception e) {
                        Logger.e(TAG, "one error1 on getJsonObjectAsync occurred: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Piano di Carico non acquisibile", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (Exception e){
            Logger.e(TAG, "one error2 on getJsonObjectAsync occurred: " + e.getMessage());
            Toast.makeText(MainActivity.this, "Piano di Carico non acquisibile", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkGPS() {
        //Check whether GPS tracking is enabled//
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.alterTitle);
            builder.setMessage(R.string.gpsnodisp);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(MainActivity.this, SettingActivity.class));
                }
            });
            builder.show();
            finish();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // Check which request we're responding to
            switch(requestCode){
                case CAMERA_REQUEST:
                    if (resultCode == Activity.RESULT_OK) {
                        switch (GetStatApp()) {
                            case GET_PICTURE_CAMERA:
                                if (data == null) return;
                                lpb.show();
                                Gson gson = new Gson();

                                final String c = data.getExtras().get("consegna").toString();
                                final Consegna consegna = gson.fromJson(c,Consegna.class);
                                final String comment = data.getExtras().get("commento").toString();
                                final String fileName = data.getStringExtra("filename");

                                final int esito =  data.getIntExtra("esito",-1);// Integer.parseInt(data.getExtras().get("esito").toString());
                                final String image64 =  data.getExtras().get("image64").toString();

                                //send mail
                                Mail mail = new Mail(getBaseContext(), new Mail.Completed() {
                                    @Override
                                    public void callback(String message, Integer result) {
                                    final String _messagio = message;
                                    final Integer _result = result;
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                        lpb.hide();
                                        if (_result == 0) {
                                            //update tabella consegna
                                            consegna.setIdEsitoConsegna(esito);
                                            consegna.setFlInviato("S");
                                            consegna.setCommento(comment.trim());
                                            consegna.setFileName(fileName);
                                            consegna.setFileType("image/jpg");
                                            consegna.setFileBase64(image64);
                                            Consegna.Update(consegna, getBaseContext());
                                            Consegna.InsertConsegna(consegna, getBaseContext());

                                            final Button btnOk = (Button) clientDialog.findViewById(R.id.btnOk);
                                            final Button btnMerceMancante = (Button) clientDialog.findViewById(R.id.btnMerceMancante);
                                            final Button btnMerceDanneggiata = (Button) clientDialog.findViewById(R.id.btnMerceDanneggiata);
                                            final Button btnAltro = (Button) clientDialog.findViewById(R.id.btnAltro);
                                            clientDialog.onBackPressed();
                                            consegnaAdapter.Update(Consegna.GetLista(MainActivity.this));
                                            if (consegnaAdapter.isCompleted()) {
                                                Step.Update(STATO.UPLOAD_END_TRAVEL.id, MainActivity.this);
                                                UpdateStastoView();
                                            }
                                            AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, true);
                                        }
                                        Toast.makeText(getApplicationContext(), _messagio, Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    }
                                });
                                mail.setAddressTo(consegna.getMailCapoArea() + consegna.getMailAge()+ consegna.getMailVettore());
                                mail.setAddressCc("problemaconsegna@saporiditoscana.com;");
//                                mail.setSubject("Giro " + ((TextView)findViewById(R.id.txGiro)).getText() + " - Cliente " + consegna.getCliente()  + "[" + Gps.GetCurrentTimeStamp()+"]");
                                mail.setSubject("Giro " + giro.getDsGiro().trim() + " - Cliente " + consegna.getCliente()  + "[" + Gps.GetCurrentTimeStamp()+"]");
                                mail.setMessage("Consegna effettuata con esito: " + EsitoConsegna.GetTesto(getBaseContext(), esito));
                                if (esito == 4) {
                                    mail.setMessage("Consegna effettuata con esito: " + EsitoConsegna.GetTesto(getBaseContext(), esito) +
                                            System.lineSeparator() +
                                            " commento: " + comment);
                                }

                                Attach attach = new Attach();
                                attach.setFileName("foto.jpg");
                                attach.setFileBase64(image64);
                                attach.setMediaType("image/jpg");
                                mail.AddAttach(attach);
                                mail.SendMail();
                            break;
                        }
                    }else {
                        final Button btnOk = (Button) clientDialog.findViewById(R.id.btnOk);
                        final Button btnMerceMancante = (Button) clientDialog.findViewById(R.id.btnMerceMancante);
                        final Button btnMerceDanneggiata = (Button) clientDialog.findViewById(R.id.btnMerceDanneggiata);
                        final Button btnAltro = (Button) clientDialog.findViewById(R.id.btnAltro);
                        AbilitaEsiti(btnOk, btnMerceMancante, btnMerceDanneggiata, btnAltro, true);
                    }
                break;
                case SCAN_REQUEST:
//                    Logger.d(TAG +"["+ SCAN_REQUEST + "]", "resultcode:" + resultCode);
                    if (resultCode == RESULT_OK) {
                        // The user picked a contact.
                        // The Intent's data Uri identifies which contact was selected.
                        // Do something with the contact here (bigger example below)
                        switch (GetStatApp()) {
                            case UPLOAD_TRAVEL: //leggo il qrcode per caricare tutta la distinta e passo allo stato successivo
                                List<DbQuery> dbQueries = new ArrayList<DbQuery>();
                                if (qrCodeText != null && !qrCodeText.toString().isEmpty()) {

                                    Logger.d(TAG, "QrCode load successful");

                                    Terminale terminale = new Terminale(MainActivity.this);
                                    String[] strings = {};

                                    //controllo se la distinta ha il paragrafo del giro
                                    String[] dati = qrCodeText.split("\\§");
                                    if (dati.length > 1){
                                        String[] s = dati[0].split("\\#");
                                        String sGiro = s[0];
                                        SimpleDateFormat f =new SimpleDateFormat("yyyyMMdd",Locale.ITALIAN);

                                        if (giro== null) giro = new Giro(getBaseContext());

                                        Date dtconsegna =  f.parse(s[1]);
                                        Date d = f.parse(giro.getDtConsegnayyyyMMdd());

                                        if (!(giro.getCdGiro().equals(sGiro) && dtconsegna.compareTo(d) == 0 && terminale.getIdConducente().equals(s[2]))){
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                            builder.setTitle(R.string.alterTitle);
                                            builder.setMessage(R.string.messaggio_giro_errato);
                                            builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    return;
                                                }
                                            });
                                            AlertDialog alert = builder.create();
                                            alert.show();
                                        }else
                                            strings = dati[1].split("\\#");

                                    }else{
                                        //qrcode vecchio non c'è il riferimento al Giro
                                        strings = dati[0].split("\\#");
                                    }

                                    if (strings.length<=0)
                                        strings = qrCodeText.split("\\#");

                                    for (int i = 0; i < strings.length; i++) {
                                        int annoReg = 2000;
                                        int nrReg = -1;
                                        annoReg += Integer.parseInt(strings[i].substring(0, 2));
                                        nrReg = Integer.parseInt(strings[i].substring(2));

                                        dbQueries.add(Consegna.Insert(annoReg, nrReg));

                                    }
                                    if (Consegna.Insert(dbQueries, MainActivity.this)) {
                                        Step.Update(STATO.UPLOAD_DETAIL_TRAVEL.id, MainActivity.this);
                                        UpdateStastoView();
                                    }
                                    //carico in automatico tutti i dati delle consegne
                                    for (int i = 0; i < strings.length; i++) {
                                        getTravelConsegnaAsync(terminale.getWebServerUrlErgon() + getString(R.string.GetTravelConsegna), strings[i]);
                                    }
                                }
                            break;
                            case UPLOAD_DETAIL_TRAVEL: //leggo il barcode delle singole bolle per confermare gli scarichi
                                Terminale terminale = new Terminale(MainActivity.this);
                                if (qrCodeText != null && !qrCodeText.toString().isEmpty()) {
                                    getTravelConsegnaAsync(terminale.getWebServerUrlErgon() + getString(R.string.GetTravelConsegna), qrCodeText);
                                }
                            break;
                            case UPLOAD_START_TRAVEL: //leggo il barcode delle bolle per dare esito della consegna
                                if (qrCodeText != null && !qrCodeText.toString().isEmpty()) {
                                    int position = consegnaAdapter.findItem(qrCodeText);
                                    rv.findViewHolderForAdapterPosition(position).itemView.performClick();
                                }
                            break;
                        }
                    }
                break;
                case UPLOAD_REQUEST:
                    if (resultCode == Activity.RESULT_OK)
                    {
                        mTracking = false;
                        stopService(new Intent(MainActivity.this, LocationService.class));

                        Toast.makeText(MainActivity.this,"Upload", Toast.LENGTH_SHORT).show();
                    }
                break;
            }
        }catch (Exception e){
            Logger.e(TAG,"one error occured on onActivityResult: " + e.getMessage(), e);
        }
    }

    public void InitToolBar(String subtitle) {
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
        switch (id)
        {
            case R.id.action_save:
                EditText idTerminale = findViewById(R.id.txTerminale);

                Toast.makeText(this,"Saved" + idTerminale.getText().toString().trim(), Toast.LENGTH_SHORT).show();
                return true;
            default:return super.onOptionsItemSelected(item);
        }
    }


    private void getTravelConsegnaAsync(String url, String qr) {
        lpb.show();
        try {
            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            int annoReg = 2000;
            int nrReg = -1;
            annoReg += Integer.parseInt(qr.substring(0, 2));
            nrReg = Integer.parseInt(qr.substring(2));

            JsonObject json = new JsonObject();
            json.addProperty("pAnnoReg", annoReg);
            json.addProperty("pNrReg", nrReg);

            HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
            urlBuilder.addQueryParameter("StringSearch", json.toString());
            url = urlBuilder.build().toString();

            OkHttpClient client = new OkHttpClient();Request request = new Request.Builder()
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
                        String jsonData = response.body().string();

                        Gson gson = new Gson();
                        final Result result   =  gson.fromJson(jsonData, Result.class);

                        if (!result.getData().toString().isEmpty()) {
//                            Logger.e(TAG, "result: " + result.getData().toString());
                            Type type = new TypeToken<Consegna>() {}.getType();
                            final Consegna consegna = gson.fromJson(result.getData().toString(), type);
                            if (consegna.getCdCli()>0) {

                                if (!Consegna.Update(consegna, MainActivity.this)){

                                    mHandler.post(() -> {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle(R.string.alterTitle);
                                        builder.setMessage(R.string.documentonp);
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
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

                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //caricare la listview
                                populateRecyclerView();
                            }
                        });

                    } catch (Exception e) {
                        Logger.e(TAG, "one error on getTravelConsegnaAsync occurred: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Piano di Carico non acquisibile", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (Exception e){
            Logger.e(TAG, "one error1 on getTravelConsegnaAsync occurred: " + e.getMessage());
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

            JsonObject json = new JsonObject();
            json.addProperty("CdDep", giro.getCdDep());
            json.addProperty("CdGiro", giro.getCdGiro());
            json.addProperty("DtConsegna", giro.getDtConsegnaddMMyyyy());
            json.addProperty("TsStart", Gps.GetCurrentTimeStamp());

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

                        if (result.Error != null)
                            Logger.e(TAG, result.getError().toString());

                    } catch (Exception e) {
                        Logger.e(TAG, "one error occurred: " + e.getLocalizedMessage());
                    }
                }
            });
        } catch (Exception e) {
            Logger.e(TAG, "one error occurred: " + e.getLocalizedMessage());
        }
    }

    //Callback received when a permissions request has been completed.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
//        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If img_user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
//                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.i(TAG, "Permission granted, updates requested, starting location updates");
            } else {
                // Permission denied.

                // Notify the img_user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the img_user for permission (device policy or "Never ask
                // again" prompts). Therefore, a img_user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    @Override
    protected void onDestroy() {
//        stopService(new Intent(this, LocationMonitoringService.class));
//        mTracking = false;
        super.onDestroy();
    }

    @Override
    protected void onStop() {
//        stopService(new Intent(this, LocationMonitoringService.class));
//        mTracking = false;
        super.onStop();
    }
}