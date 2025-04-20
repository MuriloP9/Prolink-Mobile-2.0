package com.example.prolink.Activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.prolink.R;

import java.util.List;

public class ContatoAdapter extends RecyclerView.Adapter<ContatoAdapter.ContatoViewHolder> {
    private List<Usuario> contatos;
    private OnContatoClickListener listener;

    // Interface para o clique no contato
    public interface OnContatoClickListener {
        void onContatoClick(Usuario usuario);
    }

    // Construtor
    public ContatoAdapter(List<Usuario> contatos, OnContatoClickListener listener) {
        this.contatos = contatos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflar o layout do item do contato
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contato, parent, false);
        return new ContatoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContatoViewHolder holder, int position) {
        Usuario contato = contatos.get(position);

        // Configurar os dados do contato
        holder.nomeContato.setText(contato.getNome());

        // Adicionar foto do perfil (se disponível)
        if (contato.getFotoPerfil() != null) {
            // Implementar carregamento da imagem (Glide/Picasso)
        }

        // Configurar o clique no item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onContatoClick(contato);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return contatos != null ? contatos.size() : 0;
    }

    // ViewHolder para os itens do contato
    static class ContatoViewHolder extends RecyclerView.ViewHolder {
        TextView nomeContato;
        ImageView fotoPerfil;
        // Adicione outros views conforme necessário (ImageView para foto, etc.)

        ContatoViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeContato = itemView.findViewById(R.id.nome_contato);
            fotoPerfil = itemView.findViewById(R.id.foto_perfil);
            // Inicializar outros views aqui
        }
    }

    // Método para atualizar a lista de contatos
    public void atualizarContatos(List<Usuario> novosContatos) {
        this.contatos = novosContatos;
        notifyDataSetChanged();
    }
}