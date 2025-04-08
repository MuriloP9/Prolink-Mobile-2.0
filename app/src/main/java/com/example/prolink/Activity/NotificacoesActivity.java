package com.example.prolink.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.prolink.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class NotificacoesActivity extends AppCompatActivity implements NotificacaoAdapter.OnNotificacaoClickListener {

    private RecyclerView recyclerView;
    private NotificacaoAdapter adapter;
    private List<Notificacao> notificacoes = new ArrayList<>();
    private ClasseConexao conexao;
    private int idUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacoes2);

        // Configuração inicial
        recyclerView = findViewById(R.id.recycler_notificacoes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificacaoAdapter(notificacoes, this);
        recyclerView.setAdapter(adapter);

        conexao = new ClasseConexao(this);
        idUsuarioLogado = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE).getInt("ID_USUARIO", 0);

        carregarNotificacoes();
    }

    private void carregarNotificacoes() {
        new Thread(() -> {
            try {
                Connection conn = conexao.getConnection();
                if (conn != null) {
                    String query = "SELECT m.id_mensagem, m.id_usuario_remetente, u.nome, m.texto, m.data_hora " +
                            "FROM Mensagem m " +
                            "JOIN Usuario u ON m.id_usuario_remetente = u.id_usuario " +
                            "WHERE m.id_usuario_destinatario = ? " +
                            "ORDER BY m.data_hora DESC";

                    PreparedStatement ps = conn.prepareStatement(query);
                    ps.setInt(1, idUsuarioLogado);

                    ResultSet rs = ps.executeQuery();
                    List<Notificacao> novasNotificacoes = new ArrayList<>();

                    while (rs.next()) {
                        Notificacao notificacao = new Notificacao(
                                rs.getInt("id_mensagem"),
                                rs.getInt("id_usuario_remetente"),
                                rs.getString("nome"),
                                rs.getString("texto"),
                                rs.getTimestamp("data_hora")
                        );
                        novasNotificacoes.add(notificacao);
                    }

                    runOnUiThread(() -> {
                        notificacoes.clear();
                        notificacoes.addAll(novasNotificacoes);
                        adapter.notifyDataSetChanged();
                    });

                    rs.close();
                    ps.close();
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(NotificacoesActivity.this,
                        "Erro ao carregar notificações", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public void onNotificacaoClick(Notificacao notificacao) {
        // Logs para debug
        Log.d("NotificacaoClick", "ID Remetente: " + notificacao.getIdRemetente());
        Log.d("NotificacaoClick", "Nome Remetente: " + notificacao.getNomeRemetente());

        // Abre o chat com o remetente
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ID_DESTINATARIO", notificacao.getIdRemetente());
        intent.putExtra("NOME_DESTINATARIO", notificacao.getNomeRemetente());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarNotificacoes(); // Atualiza as notificações ao retornar para a tela
    }
}