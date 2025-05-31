package com.example.prolink.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.prolink.R;

public class RecuperarSenha extends AppCompatActivity {

    private Button btnLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);

            btnLink = findViewById(R.id.btnLinkS);

            btnLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Substitua pela URL do seu site de cadastro
                    String url = "http://10.0.2.2/Projeto-Networking/src/php/esqueci-minha-senha.php";
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            });
    }
}