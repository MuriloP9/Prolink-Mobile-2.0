package com.example.prolink.Activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.prolink.R;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class CadastroActivity extends AppCompatActivity {

    private EditText editUsername, editEmail, editPassword;
    private Button btnEnter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        editUsername = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        btnEnter = findViewById(R.id.btn_enter);

        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cadastrarUsuario();
            }
        });
    }

    private void cadastrarUsuario() {
        String nome = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editPassword.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        new CadastroTask().execute(nome, email, senha);
    }

    private class CadastroTask extends AsyncTask<String, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(String... params) {
            String nome = params[0];
            String email = params[1];
            String senha = params[2];

            try (Connection conn = new ClasseConexao().entBanco(CadastroActivity.this)) {
                if (conn == null) {
                    errorMessage = "Sem conexão com o banco de dados";
                    return false;
                }

                String sql = "INSERT INTO Usuario (nome, email, senha, dataNascimento, telefone) " +
                        "VALUES (?, ?, ?, NULL, NULL)";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, nome);
                    ps.setString(2, email);
                    ps.setString(3, senha);

                    int rowsAffected = ps.executeUpdate();
                    return rowsAffected > 0;
                }
            } catch (Exception e) {
                errorMessage = e.getMessage();
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CadastroActivity.this, LoginActivity.class));
                finish();
            } else {
                String message = errorMessage.contains("UNIQUE") ?
                        "Este email já está cadastrado" :
                        "Erro ao cadastrar: " + errorMessage;

                Toast.makeText(CadastroActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}