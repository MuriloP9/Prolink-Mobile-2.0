package com.example.prolink.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    private ImageView fotoPerfilChat;
    private TextView nomeContatoChat;
    private ImageButton btnVoltar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Configura as views
        recyclerView = findViewById(R.id.recycler_mensagens);
        editMensagem = findViewById(R.id.edit_mensagem);
        btnEnviar = findViewById(R.id.btn_enviar);
        fotoPerfilChat = findViewById(R.id.foto_perfil_chat);
        nomeContatoChat = findViewById(R.id.nome_contato_chat);
        btnVoltar = findViewById(R.id.btn_voltar);

        btnVoltar.setOnClickListener(v -> finish());

        // Obtém IDs dos usuários
        idUsuarioLogado = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE).getInt("ID_USUARIO", 0);
        conexao = new ClasseConexao(this);

        // Verifica os extras da intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            idDestinatario = extras.getInt("ID_DESTINATARIO");
            String nomeDestinatario = extras.getString("NOME_DESTINATARIO");

            // Configura a toolbar
            //if (getSupportActionBar() != null) {
                //getSupportActionBar().setTitle(nomeDestinatario);
                //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
           // }

            if (nomeContatoChat != null) {
                nomeContatoChat.setText(nomeDestinatario);
            }

            // Marca mensagens como lidas ao abrir o chat
            marcarMensagensComoLidas(idDestinatario);

            // Carrega as mensagens
            carregarMensagens();
        } else {
            Toast.makeText(this, "Erro: contato não especificado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // Configura o RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MensagemAdapter(mensagens, idUsuarioLogado);
        recyclerView.setAdapter(adapter);


        // Configura o listener para ajustar o scroll quando o teclado aparece
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) { // Teclado aparecendo
                recyclerView.postDelayed(() -> {
                    if (adapter.getItemCount() > 0) { // Usando adapter.getItemCount() em vez de mensagens.size()
                        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
                    }
                }, 100);
            }
        });


        // Listener para focar no EditText
        editMensagem.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && adapter.getItemCount() > 0) {
                recyclerView.postDelayed(() -> recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1), 100);
            }
        });

        recyclerView.setItemAnimator(new DefaultItemAnimator() {
            @Override
            public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
                // Desativa animações de movimento para evitar flickering
                dispatchMoveFinished(holder);
                return false;
            }
        });

        // Configura atualização periódica das mensagens
        atualizarMensagens = new Runnable() {
            @Override
            public void run() {
                carregarMensagens();
                handler.postDelayed(this, 3000); // Atualiza a cada 3 segundos
            }
        };
        handler.postDelayed(atualizarMensagens, 3000);

        // Listener do botão enviar
        btnEnviar.setOnClickListener(v -> enviarMensagem());
    }

    private void carregarMensagens() {
        new Thread(() -> {
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
                                rs.getTimestamp("data_hora"),
                                rs.getBoolean("lida")
                        );
                        novasMensagens.add(msg);
                    }

                    runOnUiThread(() -> {
                        mensagens.clear();
                        mensagens.addAll(novasMensagens);
                        adapter.notifyDataSetChanged();
                        if (!mensagens.isEmpty()) {
                            recyclerView.scrollToPosition(mensagens.size() - 1);
                        }
                    });

                    rs.close();
                    ps.close();
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void enviarMensagem() {
        final String texto = editMensagem.getText().toString().trim();
        if (texto.isEmpty()) {
            Toast.makeText(this, "Digite uma mensagem", Toast.LENGTH_SHORT).show();
            return;
        }

        // Adiciona visualmente antes de enviar ao banco (feedback imediato)
        final Mensagem novaMensagem = new Mensagem(
                0, // ID temporário
                idUsuarioLogado,
                idDestinatario,
                texto,
                new Timestamp(System.currentTimeMillis()),
                true // Considera como lida para o remetente
        );

        runOnUiThread(() -> {
            mensagens.add(novaMensagem);
            adapter.notifyItemInserted(mensagens.size() - 1);
            recyclerView.scrollToPosition(mensagens.size() - 1);
            editMensagem.setText("");
        });

        new Thread(() -> {
            try {
                Connection conn = conexao.getConnection();
                if (conn == null) {
                    runOnUiThread(() ->
                            Toast.makeText(ChatActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show());
                    return;
                }

                String query = "INSERT INTO Mensagem (id_usuario_remetente, id_usuario_destinatario, texto, lida) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setInt(1, idUsuarioLogado);
                ps.setInt(2, idDestinatario);
                ps.setString(3, texto);
                ps.setBoolean(4, false); // Para o destinatário será não lida

                int affectedRows = ps.executeUpdate();
                Log.d("Chat", "Mensagem enviada - linhas afetadas: " + affectedRows);

                if (affectedRows == 0) {
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this, "Falha ao enviar mensagem", Toast.LENGTH_SHORT).show();
                        mensagens.remove(novaMensagem);
                        adapter.notifyDataSetChanged();
                    });
                } else {
                    // Atualiza o ID da mensagem no objeto
                    try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            final int novoId = generatedKeys.getInt(1);
                            runOnUiThread(() -> {
                                novaMensagem.setId(novoId);
                                adapter.notifyDataSetChanged();
                            });
                        }
                    }

                    // Dispara broadcast para atualizar notificações
                    Intent intent = new Intent("NOVA_MENSAGEM_RECEBIDA");
                    sendBroadcast(intent);
                    Log.d("Chat", "Broadcast de nova mensagem enviado");
                }

                ps.close();
                conn.close();

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    mensagens.remove(novaMensagem);
                    adapter.notifyDataSetChanged();
                });
            }
        }).start();
    }

    private void marcarMensagensComoLidas(int idRemetente) {
        new Thread(() -> {
            try {
                Connection conn = conexao.getConnection();
                if (conn != null) {
                    String query = "UPDATE Mensagem SET lida = true " +
                            "WHERE id_usuario_remetente = ? AND id_usuario_destinatario = ? AND lida = false";
                    PreparedStatement ps = conn.prepareStatement(query);
                    ps.setInt(1, idRemetente);
                    ps.setInt(2, idUsuarioLogado);
                    int rowsAffected = ps.executeUpdate();
                    Log.d("Chat", "Mensagens marcadas como lidas: " + rowsAffected);

                    // Atualiza a lista de mensagens após marcar como lidas
                    runOnUiThread(this::carregarMensagens);

                    ps.close();
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove callbacks para evitar vazamentos de memória
        handler.removeCallbacks(atualizarMensagens);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}