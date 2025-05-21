package com.example.prolink.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.prolink.R;

public class EscolhaAdd extends AppCompatActivity {

    private Button btnQrcode, btnLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escolha_add);

        // Inicializar os botões
        btnQrcode = findViewById(R.id.btn_qrcode);
        btnLink = findViewById(R.id.btn_link);

        // Configurar o clique do botão QRcode
        btnQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EscolhaAdd.this, QRCodeActivity.class);
                startActivity(intent);
            }
        });

        // Configurar o clique do botão Link
        btnLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EscolhaAdd.this, AddContactLinkActivity.class);
                startActivity(intent);
            }
        });
    }
}