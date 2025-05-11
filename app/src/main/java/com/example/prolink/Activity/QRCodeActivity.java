package com.example.prolink.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.prolink.R;

public class QRCodeActivity extends AppCompatActivity {
    private Button btnEscanear;
    private Button btnCodigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode2);

        // Inicializar botões
        btnEscanear = findViewById(R.id.btn_escanear);
        btnCodigo = findViewById(R.id.btn_codigo);

        // Configurar botão para ir para tela de inserção de código
        btnCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QRCodeActivity.this, AddContactLinkActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}