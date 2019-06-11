package com.saporiditoscana.travel.Orm;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;

public class Automezzo {
    private static final String TAG = "Automezzo";
    private Context context;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getTesto() {
        return Testo;
    }

    public void setTesto(String testo) {
        Testo = testo;
    }

    public String getTarga() {
        return Targa;
    }

    public void setTarga(String targa) {
        Targa = targa;
    }

    @NonNull
    @Override
    public String toString() {
        return getTarga();
    }

    @SerializedName("Id")
    public String Id;
    @SerializedName("Testo")
    public String Testo;
    @SerializedName("Targa")
    public String Targa;
}
