package com.example.prolink.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.prolink.R;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class RecuperarSenha extends AppCompatActivity {

    private EditText editEmail, editNovaSenha, editConfirmarSenha;
    private Button btnProximo;
    private ClasseConexao conexao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_senha);

        editEmail = findViewById(R.id.edit_email);
        editNovaSenha = findViewById(R.id.edit_nova_senha);
        editConfirmarSenha = findViewById(R.id.edit_confirmar_senha);
        btnProximo = findViewById(R.id.btn_proximo);
        conexao = new ClasseConexao(this);

        btnProximo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recuperarSenha();
            }
        });
    }

    private void recuperarSenha() {
        String email = editEmail.getText().toString().trim();
        String novaSenha = editNovaSenha.getText().toString().trim();
        String confirmarSenha = editConfirmarSenha.getText().toString().trim();

        if (email.isEmpty() || novaSenha.isEmpty() || confirmarSenha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }

        if (novaSenha.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection conn = conexao.getConnection();
                    if (conn != null) {
                        // Primeiro verifica se o email existe
                        String sqlVerifica = "SELECT id_usuario FROM Usuario WHERE email = ?";
                        PreparedStatement psVerifica = conn.prepareStatement(sqlVerifica);
                        psVerifica.setString(1, email);

                        if (psVerifica.executeQuery().next()) {
                            // Email existe, atualiza a senha
                            String sqlAtualiza = "UPDATE Usuario SET senha = ? WHERE email = ?";
                            PreparedStatement psAtualiza = conn.prepareStatement(sqlAtualiza);
                            psAtualiza.setString(1, novaSenha);
                            psAtualiza.setString(2, email);

                            int rowsAffected = psAtualiza.executeUpdate();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (rowsAffected > 0) {
                                        Toast.makeText(RecuperarSenha.this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RecuperarSenha.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(RecuperarSenha.this, "Erro ao alterar senha", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RecuperarSenha.this, "Email não cadastrado", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RecuperarSenha.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }
}