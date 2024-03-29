package com.saporiditoscana.travel;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.saporiditoscana.travel.Orm.Consegna;

import java.util.List;

public class ConsegnaAdapter extends RecyclerView.Adapter<ConsegnaAdapter.ConsegnaViewHolder> {
    public interface OnItemClickListener{
        void onItemClick(Consegna item);
    }

    List<Consegna> consegnas;
    OnItemClickListener listener;

    public static class ConsegnaViewHolder extends  RecyclerView.ViewHolder {
        CardView cv;
        TextView cliente;
        TextView localita;
        TextView indirizzo;
        TextView sequenza;
        TextView annoReg;
        TextView nrReg;
        TextView tipoDocumento;
        TextView numeroDocumento;
        TextView checkCliente;
        TextView pagamentoContanti;

        public ConsegnaViewHolder (View itemView){
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            checkCliente = itemView.findViewById(R.id.checkCliente);
            cliente =  itemView.findViewById(R.id.cliente);
            localita = itemView.findViewById(R.id.localita);
            indirizzo = itemView.findViewById(R.id.indirizzo);
            sequenza = itemView.findViewById(R.id.sequenza);
            annoReg = itemView.findViewById(R.id.annoreg);
            nrReg = itemView.findViewById(R.id.nrreg);
            tipoDocumento = itemView.findViewById(R.id.tipoDocumento);
            numeroDocumento = itemView.findViewById(R.id.numeroDocumento);
            pagamentoContanti = itemView.findViewById(R.id.pagamentoContanti);
        }

        public void bind(final Consegna item  , final OnItemClickListener listener) {
            cliente.setText(item.getCliente());
            indirizzo.setText(item.getIndirizzo());
            localita.setText(item.getLocalita());
            sequenza.setText(item.getSequenza());
            annoReg.setText(String.valueOf(item.getAnnoReg()));
            nrReg.setText(String.valueOf(item.getNrReg()));
            tipoDocumento.setText(item.getTipoDocumento());
            numeroDocumento.setText(String.valueOf(item.getNumeroDocumento()));

            checkCliente.setVisibility(View.GONE);
            if (item.getIdEsitoConsegna() != 0) {
                checkCliente.getBackground().setTint(Color.GREEN);
                checkCliente.setVisibility(View.VISIBLE);
                itemView.setTag("swipe_enable");//se già valorizzato esito abilito lo swipe per altre opzioni
            }

            pagamentoContanti.setVisibility(View.GONE);
            if(item.getPagamentoContanti()){
                pagamentoContanti.getBackground().setTint(Color.GREEN);
                pagamentoContanti.setVisibility(View.VISIBLE);
            }

            itemView.setOnClickListener(v -> {
                if (item.getIdEsitoConsegna() == 0)  listener.onItemClick(item);
            });
        }
    }

    ConsegnaAdapter (List<Consegna> consegnas, OnItemClickListener listener){
        this.consegnas = consegnas;
        this.listener = listener;
    }

    public boolean isCompleted(){
        boolean result = true   ;
        for (int i=0;i<consegnas.size();i++){
            if (consegnas.get(i).getIdEsitoConsegna() == 0)
                return false;
        }
        return result;
    }

    public void Update(List<Consegna> consegnas){
        this.consegnas.clear();
        this.consegnas.addAll(consegnas);
//        this.consegnas = consegnas;
        this.notifyDataSetChanged();
    }

    public Consegna getItemByPosition(int position){
        return this.consegnas.get(position);
    }

    /**
     * cerca l'item per anno e numero di registrazione
    */

    public int findItem(String key){
        int annoReg = 2000;
        int nrReg;
        annoReg += Integer.parseInt(key.substring(0, 2));
        nrReg = Integer.parseInt(key.substring(2));

        for (int i=0;i<consegnas.size();i++){
            if (consegnas.get(i).getAnnoReg() == annoReg && consegnas.get(i).getNrReg() == nrReg)
                return i;
        }
        return -1;
    }

    @NonNull
    @Override
    public ConsegnaViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardviewconsegna, viewGroup, false);
        return new ConsegnaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsegnaViewHolder consegnaViewHolder, int i) {
        consegnaViewHolder.bind(consegnas.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return  consegnas.size();
    }

}
