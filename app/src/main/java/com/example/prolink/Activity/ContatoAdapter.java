package com.example.prolink.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.prolink.R;

import java.util.List;

public class ContatoAdapter extends RecyclerView.Adapter<ContatoAdapter.ContatoViewHolder> {

    private List<Usuario> contatos;
    private ContatoClickListener clickListener;
    private ContatoRemoveListener removeListener;
    private ContatoBloqueioListener bloqueioListener;

    public interface ContatoClickListener {
        void onContatoClick(Usuario usuario);
    }

    public interface ContatoRemoveListener {
        void onContatoRemove(Usuario usuario, int position);
    }

    public interface ContatoBloqueioListener {
        void onContatoBloqueio(Usuario usuario, int position, boolean bloquear);
    }

    public ContatoAdapter(List<Usuario> contatos, ContatoClickListener clickListener) {
        this.contatos = contatos;
        this.clickListener = clickListener;
    }

    public void setRemoveListener(ContatoRemoveListener removeListener) {
        this.removeListener = removeListener;
    }

    public void setBloqueioListener(ContatoBloqueioListener bloqueioListener) {
        this.bloqueioListener = bloqueioListener;
    }

    @NonNull
    @Override
    public ContatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contato, parent, false);
        return new ContatoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContatoViewHolder holder, int position) {
        Usuario usuario = contatos.get(position);
        holder.tvNome.setText(usuario.getNome());
        holder.tvEmail.setText(usuario.getEmail());

        // Definir foto de perfil
        if (usuario.getFotoPerfil() != null) {
            try {
                byte[] imageBytes = android.util.Base64.decode(usuario.getFotoPerfil(), android.util.Base64.DEFAULT);
                if (imageBytes != null && imageBytes.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    if (bitmap != null) {
                        Bitmap circularBitmap = getCircularBitmap(bitmap);
                        holder.imgFotoPerfil.setImageBitmap(circularBitmap);
                    } else {
                        holder.imgFotoPerfil.setImageResource(R.drawable.perfil);
                    }
                } else {
                    holder.imgFotoPerfil.setImageResource(R.drawable.perfil);
                }
            } catch (Exception e) {
                Log.e("IMAGE_ERROR", "Erro ao processar imagem: " + e.getMessage());
                holder.imgFotoPerfil.setImageResource(R.drawable.perfil);
            }
        } else {
            holder.imgFotoPerfil.setImageResource(R.drawable.perfil);
        }

        // Configurar ícone do botão de bloqueio baseado no status atual
        if (usuario.isBloqueado()) {
            holder.btnBloquear.setImageResource(R.drawable.ic_unblock);
            holder.btnBloquear.setContentDescription("Desbloquear contato");
        } else {
            holder.btnBloquear.setImageResource(R.drawable.ic_block);
            holder.btnBloquear.setContentDescription("Bloquear contato");
        }

        // Configurar clique no item
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onContatoClick(usuario);
            }
        });

        // Configurar clique no botão de remover
        holder.btnRemover.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onContatoRemove(usuario, position);
            }
        });

        // Configurar clique no botão de bloquear/desbloquear
        holder.btnBloquear.setOnClickListener(v -> {
            if (bloqueioListener != null) {
                bloqueioListener.onContatoBloqueio(usuario, position, !usuario.isBloqueado());
            }
        });
    }

    public void removerContato(int position) {
        if (position >= 0 && position < contatos.size()) {
            contatos.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, contatos.size());
        }
    }

    public void atualizarStatusBloqueio(int position, boolean bloqueado) {
        if (position >= 0 && position < contatos.size()) {
            contatos.get(position).setBloqueado(bloqueado);
            notifyItemChanged(position);
        }
    }

    @Override
    public int getItemCount() {
        return contatos.size();
    }

    // Método para criar bitmap circular
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        android.graphics.Canvas canvas = new android.graphics.Canvas(output);
        final int color = 0xff424242;
        final android.graphics.Paint paint = new android.graphics.Paint();
        final android.graphics.Rect rect = new android.graphics.Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);

        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    static class ContatoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFotoPerfil;
        TextView tvNome;
        TextView tvEmail;
        ImageButton btnRemover;
        ImageButton btnBloquear;

        ContatoViewHolder(View itemView) {
            super(itemView);
            imgFotoPerfil = itemView.findViewById(R.id.img_foto_perfil);
            tvNome = itemView.findViewById(R.id.tv_nome_contato);
            tvEmail = itemView.findViewById(R.id.tv_email_contato);
            btnRemover = itemView.findViewById(R.id.btn_remover_contato);
            btnBloquear = itemView.findViewById(R.id.btn_bloquear_contato);
        }
    }
}