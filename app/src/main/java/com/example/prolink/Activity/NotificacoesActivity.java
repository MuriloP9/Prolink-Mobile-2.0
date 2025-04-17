package com.example.prolink.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private BroadcastReceiver mensagemReceiver;

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

        // Inicializa o BroadcastReceiver
        mensagemReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("NOVA_MENSAGEM_RECEBIDA".equals(intent.getAction())) {
                    Log.d("Notificacoes", "Recebido broadcast - atualizando notificações");
                    carregarNotificacoes();
                }
            }
        };

        carregarNotificacoes();
    }

    private void carregarNotificacoes() {
        new Thread(() -> {
            try {
                Connection conn = conexao.getConnection();
                if (conn != null) {
                    // Consulta simplificada e otimizada
                    String query = "SELECT m.id_mensagem, m.id_usuario_remetente, u.nome, " +
                            "m.texto, m.data_hora, m.lida " +
                            "FROM Mensagem m " +
                            "JOIN Usuario u ON m.id_usuario_remetente = u.id_usuario " +
                            "WHERE m.id_usuario_destinatario = ? " +
                            "AND m.lida = 0 " +  // Apenas mensagens não lidas
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
                                rs.getTimestamp("data_hora"),
                                rs.getBoolean("lida"),
                                1  // Como filtramos apenas não lidas, sempre 1
                        );
                        novasNotificacoes.add(notificacao);
                    }

                    runOnUiThread(() -> {
                        notificacoes.clear();
                        notificacoes.addAll(novasNotificacoes);
                        adapter.notifyDataSetChanged();

                        if (notificacoes.isEmpty()) {
                            Toast.makeText(NotificacoesActivity.this,
                                    "Nenhuma nova notificação", Toast.LENGTH_SHORT).show();
                        }
                    });

                    rs.close();
                    ps.close();
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(NotificacoesActivity.this,
                            "Erro ao carregar notificações", Toast.LENGTH_SHORT).show();
                    Log.e("Notificacoes", "Erro: " + e.getMessage());
                });
            }
        }).start();
    }


    @Override
    public void onNotificacaoClick(Notificacao notificacao) {
        // Atualiza no banco que a mensagem foi lida
        marcarComoLida(notificacao.getIdMensagem());

        // Abre o chat normalmente
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ID_DESTINATARIO", notificacao.getIdRemetente());
        intent.putExtra("NOME_DESTINATARIO", notificacao.getNomeRemetente());
        startActivity(intent);
    }

    private void marcarComoLida(int idMensagem) {
        new Thread(() -> {
            try {
                Connection conn = conexao.getConnection();
                if (conn != null) {
                    String query = "UPDATE Mensagem SET lida = true WHERE id_mensagem = ?";
                    PreparedStatement ps = conn.prepareStatement(query);
                    ps.setInt(1, idMensagem);
                    int rowsAffected = ps.executeUpdate();
                    Log.d("Notificacoes", "Mensagens marcadas como lidas: " + rowsAffected);

                    ps.close();
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Registrar receiver
        registerReceiver(mensagemReceiver, new IntentFilter("NOVA_MENSAGEM_RECEBIDA"));
        carregarNotificacoes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Não esquecer de desregistrar
        unregisterReceiver(mensagemReceiver);
    }
}