package com.saporiditoscana.travel.Orm;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;

public class Conducente {
    private static final String TAG = "Conducente";
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


    @NonNull
    @Override
    public String toString() {
        return getTesto();
    }

    @SerializedName("Id")
    public String Id;
    @SerializedName("Testo")
    public String Testo;

}

