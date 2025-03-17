package com.example.prolink.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.prolink.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void meuMetodo(View view) {
        Toast.makeText(this, "Botão clicado!", Toast.LENGTH_SHORT).show();
    }

}