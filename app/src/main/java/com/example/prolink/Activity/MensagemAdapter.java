package com.example.prolink.Activity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.prolink.R;

import java.util.List;

public class MensagemAdapter extends RecyclerView.Adapter<MensagemAdapter.MensagemViewHolder> {
    private List<Mensagem> mensagens;
    private int idUsuarioLogado;

    public MensagemAdapter(List<Mensagem> mensagens, int idUsuarioLogado) {
        this.mensagens = mensagens;
        this.idUsuarioLogado = idUsuarioLogado;
    }

    @Override
    public int getItemViewType(int position) {
        return mensagens.get(position).getIdRemetente() == idUsuarioLogado ? 0 : 1;
    }

    @Override
    public MensagemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                viewType == 0 ? R.layout.item_mensagem_enviada : R.layout.item_mensagem_recebida,
                parent, false);
        return new MensagemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MensagemViewHolder holder, int position) {
        Mensagem mensagem = mensagens.get(position);
        holder.textoMensagem.setText(mensagem.getTexto());
        holder.horaMensagem.setText(mensagem.getDataHoraFormatada());
    }

    @Override
    public int getItemCount() {
        return mensagens.size();
    }

    static class MensagemViewHolder extends RecyclerView.ViewHolder {
        TextView textoMensagem, horaMensagem;

        MensagemViewHolder(View itemView) {
            super(itemView);
            textoMensagem = itemView.findViewById(R.id.texto_mensagem);
            horaMensagem = itemView.findViewById(R.id.hora_mensagem);
        }
    }
}