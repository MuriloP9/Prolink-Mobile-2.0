package com.example.prolink.Activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.prolink.R;

public class CadastroWeb extends AppCompatActivity {

    private Button btnLink;
    private TextView tvJaTenhoCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_web);

        // Inicializar os componentes
        btnLink = findViewById(R.id.btnLink);
        tvJaTenhoCadastro = findViewById(R.id.tvJaTenhoCadastro);

        // Configurar o clique do botão de cadastro
        btnLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Substitua pela URL do seu site de cadastro
                String url = "http://10.0.2.2/Projeto-Networking/src/php/index.php";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        // Configurar o clique do TextView "Já tenho cadastro"
        tvJaTenhoCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirecionar para a LoginActivity
                Intent intent = new Intent(CadastroWeb.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}