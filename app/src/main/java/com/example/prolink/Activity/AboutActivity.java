package com.example.prolink.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.prolink.R;

public class AboutActivity extends AppCompatActivity {

    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Inicializar componentes
        initViews();

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        // Botão voltar para MainActivity
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Criar intent para voltar à MainActivity
                Intent intent = new Intent(AboutActivity.this, MainActivity.class);

                // Limpar stack de activities para evitar acúmulo
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                startActivity(intent);

                // Adicionar animação de transição suave
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                // Finalizar esta activity
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Comportamento personalizado para botão voltar do sistema
        Intent intent = new Intent(AboutActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}