package com.saporiditoscana.travel.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.saporiditoscana.travel.Logger;
import com.saporiditoscana.travel.Orm.Gps;

import java.util.List;

/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 *
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 *
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification assocaited with that service is removed.
 */
public class    LocationService extends Service {

    private static final String TAG = LocationService.class.getSimpleName();

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 600000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private static final LocationRequest mLocationRequest = LocationRequest.create()
            .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)                   // 10 minuti
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)    //  5 minuti
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Handler mServiceHandler;
    private Location mLocation; //current Location

    @Override
    public void onCreate() {
//        Logger.d(TAG,"onCreate");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null)
                    return;
                saveGpsCoordinate(locationResult.getLastLocation());
            }
        };

        startLocationUpdates();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();

        mServiceHandler = new Handler(handlerThread.getLooper());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Logger.d(TAG,"onStartCommand");
        return START_STICKY;
    }

    private void startLocationUpdates() {
        Logger.d(TAG,"startLocationUpdates");
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null /* Looper */);
    }

    @Nullable
    public IBinder onBind() {
        return onBind();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void saveGpsCoordinate(Location location) {
//        Logger.d(TAG,"saveGpsCoordinate");
        Gps gps =new Gps();
        gps.setLatitudine(String.valueOf(location.getLatitude()));
        gps.setLongitudine(String.valueOf(location.getLongitude()));
        gps.setTsValidita(location.getTime());
        Gps.Insert(gps, getBaseContext());

        //call webservice
        if (hasConnection()){
            List<Gps> gpsList = Gps.GetLista(this);
            for (Gps item:gpsList) {
                Gps.InsertGps (item, this); //aggiorno il db in azienda
            }
        }

    }

    private boolean hasConnection(){
        ConnectivityManager connectivityManager  = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected())
            return false;
        else return true;
    }

}