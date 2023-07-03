package com.saporiditoscana.travel;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.saporiditoscana.travel.Orm.Conducente;

import java.util.List;

public class ConducenteAdapter extends ArrayAdapter<Conducente> {
    public ConducenteAdapter(Context context, int resource, List<Conducente> objects) {
        super(context, resource, objects);
    }

    public Conducente getConducente(String id){
        Conducente result = null;

        for (int i = 0; i < getCount(); i++) {
            if (id.equals(getItem(i).getId())) {
                result = getItem(i);
                break;
            }
        }

        return result;
    }
}
