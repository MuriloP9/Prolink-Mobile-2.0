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
    private ClasseConexao conexao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        // Inicialização dos componentes
        editUsername = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        btnEnter = findViewById(R.id.btn_enter);
        conexao = new ClasseConexao(CadastroActivity.this);

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

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Digite um email válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senha.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
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

            Connection conn = null;
            PreparedStatement ps = null;

            try {
                conn = conexao.getConnection();
                if (conn == null) {
                    errorMessage = "Sem conexão com o banco de dados";
                    return false;
                }

                String sql = "INSERT INTO Usuario (nome, email, senha, dataNascimento, telefone) " +
                        "VALUES (?, ?, ?, NULL, NULL)";

                ps = conn.prepareStatement(sql);
                ps.setString(1, nome);
                ps.setString(2, email);
                ps.setString(3, senha);

                int rowsAffected = ps.executeUpdate();
                return rowsAffected > 0;

            } catch (Exception e) {
                errorMessage = e.getMessage();
                return false;
            } finally {
                conexao.closeStatement(ps);
                conexao.closeConnection(conn);
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

                // Passa os dados para a LoginActivity
                Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
                intent.putExtra("REGISTERED_EMAIL", editEmail.getText().toString());
                startActivity(intent);
                finish();
            } else {
                String message;
                if (errorMessage.contains("UNIQUE")) {
                    message = "Este email já está cadastrado";
                } else if (errorMessage.contains("connection")) {
                    message = "Erro de conexão com o servidor";
                } else {
                    message = "Erro ao cadastrar: " + errorMessage;
                }
                Toast.makeText(CadastroActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}