package com.example.prolink.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prolink.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText mEditEmail;
    private EditText mEditPassword;
    private Button mBtnEnter;
    private TextView mTxtAccount;
    private ClasseConexao conexao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicialização dos componentes
        mEditEmail = findViewById(R.id.edit_email);
        mEditPassword = findViewById(R.id.edit_password);
        mBtnEnter = findViewById(R.id.btn_enter);
        mTxtAccount = findViewById(R.id.txt_account);
        conexao = new ClasseConexao(LoginActivity.this);

        // Verificar se já existe uma sessão válida
        verificarSessaoExistente();

        // Listener do botão de login
        mBtnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEditEmail.getText().toString().trim();
                String password = mEditPassword.getText().toString().trim();

                Log.i(TAG, "Email: " + email);
                Log.i(TAG, "Tentativa de login");

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                autenticarUsuario(email, password);
            }
        });

        // Listener do texto "Criar conta"
        mTxtAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, CadastroWeb.class);
                startActivity(intent);
            }
        });

        TextView txtRecuperarSenha = findViewById(R.id.txt_recuperar_senha);
        txtRecuperarSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RecuperarSenha.class);
                startActivity(intent);
            }
        });
    }

    private void verificarSessaoExistente() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int idUsuario = prefs.getInt("id_usuario", -1);
        String email = prefs.getString("email", null);
        String nome = prefs.getString("nome", null);

        if (idUsuario != -1 && email != null && nome != null) {
            Log.d(TAG, "Sessão existente encontrada para usuário: " + nome);
            // Se já tem uma sessão válida, pode ir direto para MainActivity
            irParaMainActivity(nome, idUsuario, email);
        }
    }

    private void autenticarUsuario(final String email, final String senha) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Connection conn = null;
                PreparedStatement ps = null;
                ResultSet rs = null;

                try {
                    conn = conexao.getConnection();
                    if (conn == null) {
                        runOnUiThread(() -> {
                            Toast.makeText(LoginActivity.this, "Erro de conexão com o banco de dados", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    String sql = "SELECT id_usuario, nome, email FROM Usuario WHERE email = ? AND senha = ?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, email);
                    ps.setString(2, senha);

                    rs = ps.executeQuery();

                    if (rs.next()) {
                        // Dados do usuário
                        final String nomeUsuario = rs.getString("nome");
                        final int idUsuario = rs.getInt("id_usuario");
                        final String emailUsuario = rs.getString("email");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "Login bem-sucedido - ID: " + idUsuario + ", Nome: " + nomeUsuario);
                                salvarSessaoUsuario(idUsuario, nomeUsuario, emailUsuario);
                                Toast.makeText(LoginActivity.this, "Bem-vindo, " + nomeUsuario + "!", Toast.LENGTH_SHORT).show();
                                irParaMainActivity(nomeUsuario, idUsuario, emailUsuario);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.w(TAG, "Credenciais inválidas para email: " + email);
                                Toast.makeText(LoginActivity.this, "Email ou senha incorretos", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro durante autenticação", e);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LoginActivity.this, "Erro ao verificar credenciais", Toast.LENGTH_SHORT).show();
                        }
                    });
                } finally {
                    try {
                        if (rs != null) rs.close();
                        if (ps != null) ps.close();
                        if (conn != null) conn.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao fechar recursos", e);
                    }
                }
            }
        }).start();
    }

    private void salvarSessaoUsuario(int idUsuario, String nome, String email) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("id_usuario", idUsuario);
        editor.putString("nome", nome);
        editor.putString("email", email);
        editor.putString("usuario", email);
        editor.putLong("timestamp_login", System.currentTimeMillis());

        boolean saved = editor.commit();

        Log.d(TAG, "Sessão salva: " + saved + " - ID: " + idUsuario + ", Nome: " + nome + ", Email: " + email);

        // Debug
        int savedId = prefs.getInt("id_usuario", -1);
        String savedName = prefs.getString("nome", null);
        Log.d(TAG, "Verificação pós-salvamento - ID: " + savedId + ", Nome: " + savedName);
    }

    private void irParaMainActivity(String nomeUsuario, int idUsuario, String email) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("NOME_USUARIO", nomeUsuario);
        intent.putExtra("ID_USUARIO", idUsuario);
        intent.putExtra("EMAIL_USUARIO", email);
        intent.putExtra("id_usuario", idUsuario);
        intent.putExtra("user_id", idUsuario);

        Log.d(TAG, "Navegando para MainActivity com ID: " + idUsuario);
        startActivity(intent);
        finish();
    }

    public static void limparSessao(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_session", android.content.Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        Log.d("LoginActivity", "Sessão limpa");
    }
}