package com.example.prolink.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prolink.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddContactLinkActivity extends AppCompatActivity {

    private EditText etCodigoContato;
    private Button btnAdicionar;
    private TextView tvResultado;
    private ClasseConexao conexao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_link);

        // Inicializar componentes
        etCodigoContato = findViewById(R.id.et_codigo_contato);
        btnAdicionar = findViewById(R.id.btn_adicionar);
        tvResultado = findViewById(R.id.tv_resultado);

        conexao = new ClasseConexao(this);

        // Botão para adicionar contato pelo código
        btnAdicionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String codigoQR = etCodigoContato.getText().toString().trim();
                if (!codigoQR.isEmpty()) {
                    adicionarContato(codigoQR);
                } else {
                    Toast.makeText(AddContactLinkActivity.this,
                            "Digite o código do contato", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void adicionarContato(String codigoQR) {
        new Thread(() -> {
            Connection conn = null;
            try {
                // Obter ID do usuário logado
                SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
                int idUsuarioLogado = prefs.getInt("ID_USUARIO", 0);

                // Obter conexão
                conn = conexao.getConnection();
                if (conn == null) {
                    showMessage("Erro de conexão com o banco de dados");
                    return;
                }

                // Buscar usuário pelo código QR
                String queryUsuario = "SELECT id_usuario FROM Usuario WHERE qr_code = ?";
                PreparedStatement pstmtUsuario = conn.prepareStatement(queryUsuario);
                pstmtUsuario.setString(1, codigoQR);
                ResultSet rs = pstmtUsuario.executeQuery();

                if (rs.next()) {
                    int idContatoSolicitado = rs.getInt("id_usuario");

                    // Verificar se não é o próprio usuário
                    if (idContatoSolicitado == idUsuarioLogado) {
                        showMessage("Você não pode adicionar a si mesmo como contato");
                        return;
                    }

                    // Verificar se o contato já existe
                    String queryVerificar = "SELECT * FROM Contatos WHERE id_usuario = ? AND id_contato = ?";
                    PreparedStatement pstmtVerificar = conn.prepareStatement(queryVerificar);
                    pstmtVerificar.setInt(1, idUsuarioLogado);
                    pstmtVerificar.setInt(2, idContatoSolicitado);
                    ResultSet rsVerificar = pstmtVerificar.executeQuery();

                    if (rsVerificar.next()) {
                        showMessage("Este usuário já está na sua lista de contatos");
                        return;
                    }

                    // Inserir na tabela de Contatos
                    String queryInserir = "INSERT INTO Contatos (id_usuario, id_contato, data_adicao) VALUES (?, ?, GETDATE())";
                    PreparedStatement pstmtInserir = conn.prepareStatement(queryInserir);
                    pstmtInserir.setInt(1, idUsuarioLogado);
                    pstmtInserir.setInt(2, idContatoSolicitado);
                    int rowsAffected = pstmtInserir.executeUpdate();

                    if (rowsAffected > 0) {
                        showSuccess();
                    } else {
                        showMessage("Falha ao adicionar contato");
                    }
                } else {
                    showMessage("Código QR inválido ou não encontrado");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showMessage("Erro ao processar solicitação: " + e.getMessage());
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void showMessage(final String message) {
        runOnUiThread(() -> {
            tvResultado.setText(message);
            tvResultado.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        });
    }

    private void showSuccess() {
        runOnUiThread(() -> {
            tvResultado.setText("Contato adicionado com sucesso!");
            tvResultado.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

            // Redirecionar para a tela de contatos após 2 segundos
            tvResultado.postDelayed(() -> {
                Intent intent = new Intent(AddContactLinkActivity.this, ContatosActivity.class);
                startActivity(intent);
                finish();
            }, 2000);
        });
    }
}