package com.saporiditoscana.travel.Services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import com.saporiditoscana.travel.Orm.Gps;

import java.util.List;

public class LocationService extends Service implements LocationListener {

    private static final String TAG = LocationService.class.getSimpleName();
    private LocationManager locationManager;

    @Override
    public void onCreate() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationUpdates();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Log.d(TAG, "Location updates started");
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(this);
        Log.d(TAG, "Location updates stopped");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            saveGpsCoordinate(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), location.getTime());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private void saveGpsCoordinate(String lat, String lng, long time) {
        Gps gps = new Gps();
        gps.setLatitudine(String.valueOf(lat));
        gps.setLongitudine(String.valueOf(lng));
        gps.setTsValidita(time);
        Gps.Insert(gps, getBaseContext());

        // Call webservice
        if (hasConnection()) {
            List<Gps> gpsList = Gps.GetLista(this);
            for (Gps item : gpsList) {
                Gps.InsertGps(item, this); // Aggiorno il database in azienda
            }
        }
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
}
