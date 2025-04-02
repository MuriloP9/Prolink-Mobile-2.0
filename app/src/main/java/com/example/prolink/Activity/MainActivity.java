package com.example.prolink.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prolink.R;

public class MainActivity extends AppCompatActivity {

    private TextView txtWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtWelcome = findViewById(R.id.textView); // ID da sua TextView

        // Receber os dados da Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String nomeUsuario = extras.getString("NOME_USUARIO");
            int idUsuario = extras.getInt("ID_USUARIO");

            // Atualizar a TextView com o nome do usuário
            txtWelcome.setText("Olá, " + nomeUsuario);

            // Você pode salvar o ID do usuário para usar em outras partes do app
            // Exemplo: SharedPreferences ou variável global
        }
    }
}