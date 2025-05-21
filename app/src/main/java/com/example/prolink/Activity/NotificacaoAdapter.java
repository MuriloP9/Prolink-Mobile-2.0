package com.example.prolink.Activity;

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

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.ViewHolder> {

    private List<Notificacao> notificacoes;
    private OnNotificacaoClickListener listener;
    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat fullDateFormat;
    private long oneDayInMillis = 24 * 60 * 60 * 1000;
    private long currentTime;

    public NotificacaoAdapter(List<Notificacao> notificacoes, OnNotificacaoClickListener listener) {
        this.notificacoes = notificacoes;
        this.listener = listener;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
        this.fullDateFormat = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
        this.currentTime = System.currentTimeMillis();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificacao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacao notificacao = notificacoes.get(position);

        // Configurar nome do remetente
        holder.txtRemetente.setText(notificacao.getNomeRemetente());

        // Formatar e exibir data/hora de acordo com a idade da mensagem
        String formattedTime;
        long messageTime = notificacao.getDataHora().getTime();
        long timeDiff = currentTime - messageTime;

        if (timeDiff < oneDayInMillis) {
            // Hoje: mostrar apenas a hora
            formattedTime = timeFormat.format(notificacao.getDataHora());
        } else if (timeDiff < 7 * oneDayInMillis) {
            // Menos de uma semana: mostrar data curta
            formattedTime = dateFormat.format(notificacao.getDataHora());
        } else {
            // Mais antiga: mostrar data completa
            formattedTime = fullDateFormat.format(notificacao.getDataHora());
        }

        holder.txtData.setText(formattedTime);

        // Configurar texto da mensagem
        holder.txtMensagem.setText(notificacao.getTexto());

        // Configurar indicadores de não lido
        if (!notificacao.isLida()) {
            holder.indicatorUnread.setVisibility(View.VISIBLE);
            // Opcional: destaque visual adicional
            holder.txtMensagem.setTextColor(holder.txtMensagem.getContext().getResources().getColor(R.color.colorPrimaryDark));
        } else {
            holder.indicatorUnread.setVisibility(View.GONE);
            // Texto normal para mensagens lidas
            holder.txtMensagem.setTextColor(holder.txtMensagem.getContext().getResources().getColor(android.R.color.secondary_text_light));
        }

        // Configurar clique no item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificacaoClick(notificacao, true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificacoes != null ? notificacoes.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View indicatorUnread;
        TextView txtRemetente;
        TextView txtData;
        TextView txtMensagem;

        public ViewHolder(View itemView) {
            super(itemView);
            indicatorUnread = itemView.findViewById(R.id.indicator_unread);
            txtRemetente = itemView.findViewById(R.id.txt_remetente);
            txtData = itemView.findViewById(R.id.txt_data);
            txtMensagem = itemView.findViewById(R.id.txt_mensagem);
        }
    }

    public interface OnNotificacaoClickListener {
        void onNotificacaoClick(Notificacao notificacao, boolean verificarBloqueio);
    }
}