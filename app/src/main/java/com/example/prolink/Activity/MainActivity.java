package com.example.prolink.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prolink.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView txtWelcome;
    private ImageView imgQRCode;
    private ImageView imgChat;
    private ImageView imgContatos;
    private ImageView imgNotificacoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa as Views
        txtWelcome = findViewById(R.id.textView);
        imgQRCode = findViewById(R.id.imageView11); // ID da ImageView do QR Code
        imgChat = findViewById(R.id.imageView14); // ID da ImageView do Chat (ajuste conforme seu layout)
        imgContatos = findViewById(R.id.imageView12); // ID da ImageView de Contatos
        imgNotificacoes = findViewById(R.id.imageView10); // ID da ImageView de Notificações

        // Configura os listeners de clique
        imgQRCode.setOnClickListener(this);
        imgChat.setOnClickListener(this);
        imgContatos.setOnClickListener(this);
        imgNotificacoes.setOnClickListener(this);

        // Receber os dados da Intent (mantido da versão original)
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String nomeUsuario = extras.getString("NOME_USUARIO");
            int idUsuario = extras.getInt("ID_USUARIO");

            // Atualizar a TextView com o nome do usuário
            if (nomeUsuario != null && !nomeUsuario.isEmpty()) {
                String primeiroNome = nomeUsuario.split(" ")[0];
                txtWelcome.setText("Olá, " + primeiroNome);
            }

            // Salva o ID do usuário no SharedPreferences
            SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("ID_USUARIO", idUsuario);
            editor.apply();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;

        // Verifica qual ImageView foi clicada
        if (v.getId() == imgQRCode.getId()) {
            intent = new Intent(this, QRCodeActivity.class);
        }
        else if (v.getId() == imgChat.getId()) {
            intent = new Intent(this, ChatActivity.class);
        }
        else if (v.getId() == imgContatos.getId()) {
            intent = new Intent(this, ContatosActivity.class);
        }
        else if (v.getId() == imgNotificacoes.getId()) {
            intent = new Intent(this, NotificacoesActivity.class);
        }

        // Inicia a Activity se foi criada
        if (intent != null) {
            startActivity(intent);

        }
    }

    // Mantém a função de boas-vindas original
    public void exibirMensagemBoasVindas(String nomeUsuario) {
        txtWelcome.setText("Olá, " + nomeUsuario);
        Toast.makeText(this, "Bem-vindo, " + nomeUsuario + "!", Toast.LENGTH_SHORT).show();
    }
}