package com.saporiditoscana.travel;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import androidx.annotation.NonNull;

public class ConducenteAdapter<Conducente> extends ArrayAdapter<Conducente> {
    public ConducenteAdapter(@NonNull Context context, int resource, @NonNull List<Conducente> objects) {
        super(context, resource, objects);
    }

    public int getConducenteById(String id){
        int result = -1;

        for (int i = 0; i < this.getCount(); i++)
        {
            if (id.equals(((com.saporiditoscana.travel.Orm.Conducente)this.getItem(i)).getId()))
            {
                result = i;
                break;
            }
        }
        return result;
    }

    public Conducente getConducente(String id){
        Conducente result = null;

        for (int i = 0; i < this.getCount(); i++)
        {
            if (id.equals(((com.saporiditoscana.travel.Orm.Conducente)this.getItem(i)).getId()))
            {
                result = (Conducente) ((com.saporiditoscana.travel.Orm.Conducente)this.getItem(i));
                break;
            }
        }
        return result;
    }
}
