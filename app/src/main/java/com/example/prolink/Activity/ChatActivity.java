package com.example.prolink.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.prolink.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText editMensagem;
    private ImageButton btnEnviar;
    private MensagemAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();
    private int idUsuarioLogado, idDestinatario;
    private ClasseConexao conexao;
    private Handler handler = new Handler();
    private Runnable atualizarMensagens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Obter IDs dos usuários
        idUsuarioLogado = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE).getInt("ID_USUARIO", 0);
        idDestinatario = getIntent().getIntExtra("ID_DESTINATARIO", 0);
        String nomeDestinatario = getIntent().getStringExtra("NOME_DESTINATARIO");

        // Configurar toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(nomeDestinatario);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicializar views
        recyclerView = findViewById(R.id.recycler_mensagens);
        editMensagem = findViewById(R.id.edit_mensagem);
        btnEnviar = findViewById(R.id.btn_enviar);
        conexao = new ClasseConexao(this);

        // Configurar RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MensagemAdapter(mensagens, idUsuarioLogado);
        recyclerView.setAdapter(adapter);

        // Carregar mensagens
        carregarMensagens();

        // Configurar atualização periódica
        atualizarMensagens = new Runnable() {
            @Override
            public void run() {
                carregarMensagens();
                handler.postDelayed(this, 5000); // Atualiza a cada 5 segundos
            }
        };
        handler.postDelayed(atualizarMensagens, 5000);

        // Listener do botão enviar
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensagem();
            }
        });
    }

    private void carregarMensagens() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection conn = conexao.getConnection();
                    if (conn != null) {
                        String query = "SELECT * FROM Mensagem WHERE " +
                                "(id_usuario_remetente = ? AND id_usuario_destinatario = ?) OR " +
                                "(id_usuario_remetente = ? AND id_usuario_destinatario = ?) " +
                                "ORDER BY data_hora ASC";

                        PreparedStatement ps = conn.prepareStatement(query);
                        ps.setInt(1, idUsuarioLogado);
                        ps.setInt(2, idDestinatario);
                        ps.setInt(3, idDestinatario);
                        ps.setInt(4, idUsuarioLogado);

                        ResultSet rs = ps.executeQuery();
                        final List<Mensagem> novasMensagens = new ArrayList<>();

                        while (rs.next()) {
                            Mensagem msg = new Mensagem(
                                    rs.getInt("id_mensagem"),
                                    rs.getInt("id_usuario_remetente"),
                                    rs.getInt("id_usuario_destinatario"),
                                    rs.getString("texto"),
                                    rs.getTimestamp("data_hora")
                            );
                            novasMensagens.add(msg);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mensagens.clear();
                                mensagens.addAll(novasMensagens);
                                adapter.notifyDataSetChanged();
                                if (!mensagens.isEmpty()) {
                                    recyclerView.scrollToPosition(mensagens.size() - 1);
                                }
                            }
                        });

                        rs.close();
                        ps.close();
                        conn.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void enviarMensagem() {
        final String texto = editMensagem.getText().toString().trim();
        if (texto.isEmpty()) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection conn = conexao.getConnection();
                    if (conn != null) {
                        String query = "INSERT INTO Mensagem (id_usuario_remetente, id_usuario_destinatario, texto) VALUES (?, ?, ?)";
                        PreparedStatement ps = conn.prepareStatement(query);
                        ps.setInt(1, idUsuarioLogado);
                        ps.setInt(2, idDestinatario);
                        ps.setString(3, texto);

                        ps.executeUpdate();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editMensagem.setText("");
                                carregarMensagens();
                            }
                        });

                        ps.close();
                        conn.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(atualizarMensagens);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}