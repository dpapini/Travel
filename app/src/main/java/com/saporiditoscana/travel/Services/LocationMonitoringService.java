package com.saporiditoscana.travel.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.saporiditoscana.travel.Logger;
import com.saporiditoscana.travel.Orm.Gps;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

public class LocationMonitoringService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = LocationMonitoringService.class.getSimpleName();
    GoogleApiClient mLocationClient;

    private static final LocationRequest mLocationRequest = LocationRequest.create()
            .setInterval(1000)         // 10 minuti
            .setFastestInterval(500)  //  5 minuti
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mLocationClient == null) {
            mLocationClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mLocationClient.connect();
        }

        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@androidx.annotation.Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);
        Logger.d(TAG, "Connected to Google API");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Logger.d(TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Logger.d(TAG, "Failed to connect to Google API");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            saveGpsCoordinate(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), location.getTime());
        }
    }

    public Location getLastBestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        return location;
    }

    private void saveGpsCoordinate(String lat, String lng, long time) {
        Gps gps =new Gps();
        gps.setLatitudine(String.valueOf(lat));
        gps.setLongitudine(String.valueOf(lng));
        gps.setTsValidita(time);
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
