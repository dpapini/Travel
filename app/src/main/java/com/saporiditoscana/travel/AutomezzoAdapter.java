package com.saporiditoscana.travel;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.List;

public class AutomezzoAdapter<Automezzo> extends ArrayAdapter<Automezzo> {
    public AutomezzoAdapter(@NonNull Context context, int resource, @NonNull List<Automezzo> objects) {
        super(context, resource, objects);
    }

    public int getAutomezzoById(String id){
        int result = -1;

        for (int i = 0; i < this.getCount(); i++)
        {
            if (id.equals(((com.saporiditoscana.travel.Orm.Automezzo)this.getItem(i)).getId()))
            {
                result = i;
                break;
            }
        }
        return result;
    }
}
