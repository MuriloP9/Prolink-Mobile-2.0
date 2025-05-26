package com.example.prolink.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.prolink.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnAccept;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnAccept = findViewById(R.id.btn_accept);
    }

    private void setupListeners() {
        // Botão voltar
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Botão aceitar política
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptPrivacyPolicy();
            }
        });
    }

    private void acceptPrivacyPolicy() {
        // Aqui você pode salvar a preferência de que o usuário aceitou a política
        // Por exemplo, usando SharedPreferences

        Toast.makeText(this, "Política de Privacidade aceita com sucesso!", Toast.LENGTH_SHORT).show();

        // Fecha a activity e retorna para a anterior
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}