package com.example.prolink.Activity;

        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.LinearLayoutManager;
        import android.support.v7.widget.RecyclerView;
        import android.util.Log;
        import android.view.View;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.example.prolink.R;

        import java.sql.Connection;
        import java.sql.PreparedStatement;
        import java.sql.ResultSet;
        import java.sql.SQLException;
        import java.sql.Timestamp;
        import java.util.ArrayList;
        import java.util.List;

public class NotificacoesActivity extends AppCompatActivity implements NotificacaoAdapter.OnNotificacaoClickListener {

    private static final String TAG = "NotificacoesActivity";
    private RecyclerView recyclerNotificacoes;
    private NotificacaoAdapter adapter;
    private List<Notificacao> notificacoes = new ArrayList<>();
    private TextView tvSemNotificacoes;
    private ClasseConexao conexao;
    private int idUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificacoes2);

        // Obter ID do usuário logado
        SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
        idUsuarioLogado = prefs.getInt("ID_USUARIO", 0);

        if (idUsuarioLogado == 0) {
            Toast.makeText(this, "ID do usuário não encontrado. Faça login novamente.", Toast.LENGTH_LONG).show();
            // Redirecionar para a tela de login se necessário
            finish();
            return;
        }

        Log.d(TAG, "ID usuário logado: " + idUsuarioLogado);

        // Inicializar views
        recyclerNotificacoes = findViewById(R.id.recycler_notificacoes);
        tvSemNotificacoes = findViewById(R.id.tv_sem_notificacoes);

        // Configurar RecyclerView
        recyclerNotificacoes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificacaoAdapter(notificacoes, this);
        recyclerNotificacoes.setAdapter(adapter);

        // Inicializar conexão com banco de dados
        try {
            conexao = new ClasseConexao(this);
            // Testar conexão imediatamente
            Connection testConn = conexao.getConnection();
            if (testConn != null) {
                Log.d(TAG, "Conexão com banco de dados estabelecida com sucesso");
                // Verificar se o banco de dados tem a estrutura esperada
                verificarEstruturaBancoDados(testConn);
                testConn.close();
            } else {
                Log.e(TAG, "Falha ao obter conexão com banco de dados");
                Toast.makeText(this, "Não foi possível conectar ao banco de dados", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar conexão: " + e.getMessage());
            Toast.makeText(this, "Erro ao inicializar conexão: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Carregar notificações
        carregarNotificacoes();
    }

    private void verificarEstruturaBancoDados(Connection conn) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            // Verificar se a tabela Mensagem tem as colunas esperadas
            String query = "SELECT TOP 1 * FROM Mensagem";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            // Obter metadados das colunas
            java.sql.ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            Log.d(TAG, "Estrutura da tabela Mensagem:");
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                String columnType = metaData.getColumnTypeName(i);
                Log.d(TAG, "Coluna " + i + ": " + columnName + " (" + columnType + ")");
            }

        } catch (SQLException e) {
            Log.e(TAG, "Erro ao verificar estrutura do banco: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                Log.e(TAG, "Erro ao fechar recursos: " + e.getMessage());
            }
        }
    }

    private void carregarNotificacoes() {
        new Thread(() -> {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            try {
                // Query para buscar notificações não lidas ou mais recentes
                // Corrigido para usar os nomes de campo corretos: id_usuario_remetente e id_usuario_destinatario
                String query = "SELECT m.id_mensagem, m.id_usuario_remetente AS id_remetente, u.nome, m.texto, m.data_hora, m.lida, " +
                        "(SELECT COUNT(*) FROM Mensagem WHERE id_usuario_remetente = m.id_usuario_remetente AND id_usuario_destinatario = ? AND lida = 0) AS unread_count " +
                        "FROM Mensagem m " +
                        "INNER JOIN Usuario u ON m.id_usuario_remetente = u.id_usuario " +
                        "WHERE m.id_usuario_destinatario = ? " +
                        "ORDER BY m.data_hora DESC ";

                Log.d(TAG, "Tentando executar query para carregar notificações");

                conn = conexao.getConnection();
                if (conn != null) {
                    pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, idUsuarioLogado);
                    pstmt.setInt(2, idUsuarioLogado);
                    rs = pstmt.executeQuery();

                    List<Notificacao> novasNotificacoes = new ArrayList<>();

                    while (rs.next()) {
                        int idMensagem = rs.getInt("id_mensagem");
                        int idRemetente = rs.getInt("id_remetente");
                        String nomeRemetente = rs.getString("nome");
                        String texto = rs.getString("texto");
                        Timestamp dataHora = rs.getTimestamp("data_hora");
                        boolean lida = rs.getBoolean("lida");
                        int unreadCount = rs.getInt("unread_count");

                        Notificacao notificacao = new Notificacao(
                                idMensagem, idRemetente, nomeRemetente, texto, dataHora, lida, unreadCount);
                        novasNotificacoes.add(notificacao);
                    }

                    Log.d(TAG, "Notificações carregadas com sucesso: " + novasNotificacoes.size());

                    final List<Notificacao> finalNovasNotificacoes = novasNotificacoes;
                    runOnUiThread(() -> {
                        notificacoes.clear();
                        notificacoes.addAll(finalNovasNotificacoes);
                        adapter.notifyDataSetChanged();

                        // Atualizar visibilidade
                        if (notificacoes.isEmpty()) {
                            tvSemNotificacoes.setVisibility(View.VISIBLE);
                            recyclerNotificacoes.setVisibility(View.GONE);
                        } else {
                            tvSemNotificacoes.setVisibility(View.GONE);
                            recyclerNotificacoes.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    Log.e(TAG, "Conexão com banco de dados retornou null");
                    runOnUiThread(() -> Toast.makeText(NotificacoesActivity.this,
                            "Erro: Não foi possível conectar ao banco de dados", Toast.LENGTH_LONG).show());
                }
            } catch (SQLException e) {
                Log.e(TAG, "Erro SQL: " + e.getMessage(), e);
                final String errorMsg = "Erro SQL: " + e.getMessage();
                runOnUiThread(() -> Toast.makeText(NotificacoesActivity.this,
                        errorMsg, Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                Log.e(TAG, "Erro ao carregar notificações: " + e.getMessage(), e);
                final String errorMsg = "Erro: " + e.getMessage();
                runOnUiThread(() -> Toast.makeText(NotificacoesActivity.this,
                        errorMsg, Toast.LENGTH_LONG).show());
            } finally {
                // Fechar recursos para evitar memory leaks
                try {
                    if (rs != null) rs.close();
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    Log.e(TAG, "Erro ao fechar recursos: " + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void onNotificacaoClick(Notificacao notificacao, boolean verificarBloqueio) {
        // Marcar mensagem como lida
        marcarMensagemComoLida(notificacao.getIdMensagem());

        if (verificarBloqueio) {
            // Verificar se o remetente está bloqueado antes de abrir o chat
            verificarBloqueioEAbrirChat(notificacao);
        } else {
            // Abrir chat diretamente (caso necessário em outros contextos)
            abrirChat(notificacao.getIdRemetente(), notificacao.getNomeRemetente());
        }
    }

    private void verificarBloqueioEAbrirChat(Notificacao notificacao) {
        new Thread(() -> {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            try {
                conn = conexao.getConnection();
                if (conn != null) {
                    // Verificar se o contato está bloqueado
                    String query = "SELECT bloqueado FROM Contatos " +
                            "WHERE id_usuario = ? AND id_contato = ?";

                    pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, idUsuarioLogado);
                    pstmt.setInt(2, notificacao.getIdRemetente());
                    rs = pstmt.executeQuery();

                    boolean bloqueado = false;
                    if (rs.next()) {
                        bloqueado = rs.getBoolean("bloqueado");
                    }

                    final boolean usuarioBloqueado = bloqueado;

                    runOnUiThread(() -> {
                        if (usuarioBloqueado) {
                            // Usuário bloqueado, exibir mensagem
                            Toast.makeText(NotificacoesActivity.this,
                                    "Este contato está bloqueado. Desbloqueie-o para iniciar uma conversa.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            // Usuário não está bloqueado, pode abrir o chat
                            abrirChat(notificacao.getIdRemetente(), notificacao.getNomeRemetente());
                        }
                    });
                } else {
                    Log.e(TAG, "Conexão retornou null ao verificar bloqueio");
                    runOnUiThread(() -> Toast.makeText(NotificacoesActivity.this,
                            "Erro de conexão ao verificar status do contato", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao verificar bloqueio: " + e.getMessage(), e);
                final String errorMsg = "Erro: " + e.getMessage();
                runOnUiThread(() -> {
                    Toast.makeText(NotificacoesActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                });
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    Log.e(TAG, "Erro ao fechar recursos: " + e.getMessage());
                }
            }
        }).start();
    }

    private void marcarMensagemComoLida(int idMensagem) {
        new Thread(() -> {
            Connection conn = null;
            PreparedStatement pstmt = null;

            try {
                conn = conexao.getConnection();
                if (conn != null) {
                    String query = "UPDATE Mensagem SET lida = 1 WHERE id_mensagem = ?";
                    pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, idMensagem);
                    int rowsAffected = pstmt.executeUpdate();
                    Log.d(TAG, "Mensagem marcada como lida: " + idMensagem + ", linhas afetadas: " + rowsAffected);
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao marcar mensagem como lida: " + e.getMessage(), e);
            } finally {
                try {
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    Log.e(TAG, "Erro ao fechar recursos: " + e.getMessage());
                }
            }
        }).start();
    }

    private void abrirChat(int idRemetente, String nomeRemetente) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ID_DESTINATARIO", idRemetente);
        intent.putExtra("NOME_DESTINATARIO", nomeRemetente);
        startActivity(intent);
    }
}