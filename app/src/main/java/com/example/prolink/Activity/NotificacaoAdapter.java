package com.example.prolink.Activity;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.prolink.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.NotificacaoViewHolder> {

    private List<Notificacao> notificacoes;
    private OnNotificacaoClickListener listener;

    public interface OnNotificacaoClickListener {
        void onNotificacaoClick(Notificacao notificacao);
    }

    public NotificacaoAdapter(List<Notificacao> notificacoes, OnNotificacaoClickListener listener) {
        this.notificacoes = notificacoes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificacaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacao, parent, false);
        return new NotificacaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacaoViewHolder holder, int position) {
        Notificacao notificacao = notificacoes.get(position);
        holder.bind(notificacao);
    }

    @Override
    public int getItemCount() {
        return notificacoes.size();
    }

    class NotificacaoViewHolder extends RecyclerView.ViewHolder {
        TextView txtRemetente, txtMensagem, txtData;
        View itemView;

        NotificacaoViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            txtRemetente = itemView.findViewById(R.id.txt_remetente);
            txtMensagem = itemView.findViewById(R.id.txt_mensagem);
            txtData = itemView.findViewById(R.id.txt_data);
        }

        void bind(Notificacao notificacao) {
            // Destaca notificações não lidas
            if (!notificacao.isLida()) {
                itemView.setBackgroundColor(Color.parseColor("#E3F2FD")); // Azul claro
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            txtRemetente.setText(notificacao.getNomeRemetente());
            txtMensagem.setText(notificacao.getTexto());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            txtData.setText(sdf.format(notificacao.getDataHora()));

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Marca como lida ao clicar
                    notificacao.setLida(true);
                    listener.onNotificacaoClick(notificacao);
                }
            });
        }
    }
}