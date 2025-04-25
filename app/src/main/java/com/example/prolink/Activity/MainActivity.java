package com.example.prolink.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
    private ImageView profileButton;
    private ImageView settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa as Views
        txtWelcome = findViewById(R.id.textView);
        imgQRCode = findViewById(R.id.imageView11);
        imgChat = findViewById(R.id.imageView14);
        imgContatos = findViewById(R.id.imageView12);
        imgNotificacoes = findViewById(R.id.imageView10);
        profileButton = findViewById(R.id.profileButton);
        settingsButton = findViewById(R.id.settingsButton);

        // Configura os listeners de clique
        imgQRCode.setOnClickListener(this);
        imgChat.setOnClickListener(this);
        imgContatos.setOnClickListener(this);
        imgNotificacoes.setOnClickListener(this);
        profileButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);

        handleUserData();

        // Receber os dados da Intent
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
        int id = v.getId();

        if (id == R.id.profileButton) {
            openProfileActivity();
        }
        else if (id == R.id.settingsButton) {
            openSettingsActivity();
        }
        else if (id == R.id.imageView11) { // imgQRCode
            startActivity(new Intent(this, QRCodeActivity.class));
        }
        else if (id == R.id.imageView14) { // imgChat
            abrirUrlChat();
        }
        else if (id == R.id.imageView12) { // imgContatos
            startActivity(new Intent(this, ContatosActivity.class));
        }
        else if (id == R.id.imageView10) { // imgNotificacoes
            startActivity(new Intent(this, NotificacoesActivity.class));
        }
    }

    private void handleUserData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String nomeUsuario = extras.getString("NOME_USUARIO");
            int idUsuario = extras.getInt("ID_USUARIO");

            if (nomeUsuario != null && !nomeUsuario.isEmpty()) {
                String primeiroNome = nomeUsuario.split(" ")[0];
                txtWelcome.setText("Olá, " + primeiroNome);
            }

            saveUserId(idUsuario);
        }
    }

    private void saveUserId(int idUsuario) {
        SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("ID_USUARIO", idUsuario);
        editor.apply();
    }
    private void openProfileActivity() {
        SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("ID_USUARIO", 0);

        if (userId > 0) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Usuário não identificado", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSettingsActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
    }



    private void abrirUrlChat() {
        String url = "http://10.0.2.2/Projeto-Networking/src/php/index.php";

        try {
            // Tenta abrir especificamente no Chrome
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.android.chrome");
            startActivity(intent);
        } catch (Exception e) {
            // Se falhar, tenta abrir em qualquer navegador
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception ex) {
                // Se ainda falhar, mostra mensagem mais informativa
                Toast.makeText(this,
                        "Configure o Chrome como navegador padrão ou tente novamente",
                        Toast.LENGTH_LONG).show();

                // Log para debug
                Log.e("URL_Error", "Erro ao abrir URL: " + ex.getMessage());
            }
        }
    }

    public void exibirMensagemBoasVindas(String nomeUsuario) {
        txtWelcome.setText("Olá, " + nomeUsuario);
        Toast.makeText(this, "Bem-vindo, " + nomeUsuario + "!", Toast.LENGTH_SHORT).show();
    }

}