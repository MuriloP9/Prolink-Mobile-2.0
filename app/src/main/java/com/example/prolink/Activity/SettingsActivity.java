package com.example.prolink.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.prolink.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_settings);

        // Inicializa os botões
        Button btnEditProfile = findViewById(R.id.btn_edit_profile);
        Button btnChangePassword = findViewById(R.id.btn_change_password);
        Button btnAbout = findViewById(R.id.btn_about);
        Button btnPrivacy = findViewById(R.id.btn_privacy);
        Button btnLogout = findViewById(R.id.btn_logout);

        // Configura os listeners
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                startActivity(new Intent(this, EditProfileActivity.class));
            });
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> {
                startActivity(new Intent(this, ChangePasswordActivity.class));
            });
        }

        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                startActivity(new Intent(this, AboutActivity.class));
            });
        }

        if (btnPrivacy != null) {
            btnPrivacy.setOnClickListener(v -> {
                startActivity(new Intent(this, PrivacyPolicyActivity.class));
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                logoutUser();
            });
        }
    }

    private void logoutUser() {
        try {
            // Limpa a sessão do usuário usando o método da LoginActivity
            LoginActivity.limparSessao(this);

            // Limpa quaisquer outras preferências específicas desta activity
            SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();


            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            Toast.makeText(this, "Logout realizado com sucesso", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao fazer logout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}