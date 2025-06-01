package com.example.prolink.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prolink.R;
import com.example.prolink.Activity.CriptoUtils;

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
    private LinearLayout headerChat;
    private TextView btnAdicionarContato;
    private boolean jaEContato = false;

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
        headerChat = findViewById(R.id.header_chat);
        btnAdicionarContato = findViewById(R.id.btn_adicionar_contato);

        btnVoltar.setOnClickListener(v -> finish());

        // Obtém IDs dos usuários
        idUsuarioLogado = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE).getInt("ID_USUARIO", 0);
        conexao = new ClasseConexao(this);

        // Verifica os extras da intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            idDestinatario = extras.getInt("ID_DESTINATARIO");
            String nomeDestinatario = extras.getString("NOME_DESTINATARIO");

            if (nomeContatoChat != null) {
                nomeContatoChat.setText(nomeDestinatario);

                // Adiciona o listener de clique para abrir o perfil do destinatário
                nomeContatoChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        abrirPerfilDestinatario();
                    }
                });

                // Também podemos adicionar um listener para a foto de perfil
                fotoPerfilChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        abrirPerfilDestinatario();
                    }
                });
            }

            // Carrega a foto de perfil do destinatário
            carregarFotoPerfil(idDestinatario);

            // Verifica se já é contato e configura o botão de adicionar
            verificarSeJaEContato();

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
                    if (adapter.getItemCount() > 0) {
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

        // Listener do botão adicionar contato
        btnAdicionarContato.setOnClickListener(v -> confirmarAdicaoContato());
    }

    /**
     * Verifica se o destinatário já é um contato do usuário logado
     */
    private void verificarSeJaEContato() {
        new Thread(() -> {
            try {
                Connection conn = conexao.getConnection();
                if (conn != null) {
                    String query = "SELECT COUNT(*) as total FROM Contatos WHERE id_usuario = ? AND id_contato = ?";
                    PreparedStatement ps = conn.prepareStatement(query);
                    ps.setInt(1, idUsuarioLogado);
                    ps.setInt(2, idDestinatario);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        int total = rs.getInt("total");
                        jaEContato = total > 0;
                    }

                    runOnUiThread(() -> {
                        if (jaEContato) {
                            btnAdicionarContato.setVisibility(View.GONE);
                        } else {
                            btnAdicionarContato.setVisibility(View.VISIBLE);
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

    /**
     * Confirma se o usuário deseja adicionar o contato
     */
    private void confirmarAdicaoContato() {
        String nomeDestinatario = nomeContatoChat.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar contato")
                .setMessage("Deseja adicionar " + nomeDestinatario + " à sua lista de contatos?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    adicionarContato();
                })
                .setNegativeButton("Não", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    /**
     * Adiciona o destinatário como contato
     */
    private void adicionarContato() {
        new Thread(() -> {
            try {
                Connection conn = conexao.getConnection();
                if (conn != null) {
                    // Verifica se já não é contato (dupla verificação)
                    String queryVerifica = "SELECT COUNT(*) as total FROM Contatos WHERE id_usuario = ? AND id_contato = ?";
                    PreparedStatement psVerifica = conn.prepareStatement(queryVerifica);
                    psVerifica.setInt(1, idUsuarioLogado);
                    psVerifica.setInt(2, idDestinatario);
                    ResultSet rsVerifica = psVerifica.executeQuery();

                    boolean jaExiste = false;
                    if (rsVerifica.next()) {
                        jaExiste = rsVerifica.getInt("total") > 0;
                    }

                    rsVerifica.close();
                    psVerifica.close();

                    if (!jaExiste) {
                        // Adiciona o contato
                        String queryInsert = "INSERT INTO Contatos (id_usuario, id_contato, bloqueado) VALUES (?, ?, ?)";
                        PreparedStatement psInsert = conn.prepareStatement(queryInsert);
                        psInsert.setInt(1, idUsuarioLogado);
                        psInsert.setInt(2, idDestinatario);
                        psInsert.setBoolean(3, false); // Não bloqueado por padrão

                        int rowsAffected = psInsert.executeUpdate();
                        psInsert.close();

                        if (rowsAffected > 0) {
                            runOnUiThread(() -> {
                                jaEContato = true;
                                btnAdicionarContato.setVisibility(View.GONE);
                                Toast.makeText(ChatActivity.this, "Contato adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Toast.makeText(ChatActivity.this, "Erro ao adicionar contato", Toast.LENGTH_SHORT).show();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            jaEContato = true;
                            btnAdicionarContato.setVisibility(View.GONE);
                            Toast.makeText(ChatActivity.this, "Este usuário já está em seus contatos", Toast.LENGTH_SHORT).show();
                        });
                    }

                    conn.close();
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this, "Erro de conexão com o banco de dados", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Erro ao adicionar contato: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    private void abrirPerfilDestinatario() {
        Intent intent = new Intent(ChatActivity.this, ProfileActivity.class);
        intent.putExtra("USER_ID", idDestinatario);
        startActivity(intent);
    }


    private void carregarFotoPerfil(int idUsuario) {
        new Thread(() -> {
            try {
                Connection conn = conexao.getConnection();
                if (conn != null) {
                    String query = "SELECT foto_perfil FROM Usuario WHERE id_usuario = ?";
                    PreparedStatement ps = conn.prepareStatement(query);
                    ps.setInt(1, idUsuario);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        byte[] fotoPerfil = rs.getBytes("foto_perfil");
                        if (fotoPerfil != null && fotoPerfil.length > 0) {
                            // Converte o array de bytes em um bitmap
                            final Bitmap bitmap = BitmapFactory.decodeByteArray(fotoPerfil, 0, fotoPerfil.length);

                            if (bitmap != null) {
                                runOnUiThread(() -> {
                                    // Cria um drawable circular a partir do bitmap
                                    RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                                    roundedDrawable.setCircular(true);
                                    roundedDrawable.setAntiAlias(true);
                                    fotoPerfilChat.setImageDrawable(roundedDrawable);
                                });
                            } else {
                                // Se falhar ao converter, use a imagem padrão
                                carregarImagemPadrao();
                            }
                        } else {
                            // Se não houver foto no banco, use a imagem padrão
                            carregarImagemPadrao();
                        }
                    } else {
                        // Se não encontrar o usuário, use a imagem padrão
                        carregarImagemPadrao();
                    }

                    rs.close();
                    ps.close();
                    conn.close();
                } else {
                    // Se não conseguir conexão com o banco, use a imagem padrão
                    carregarImagemPadrao();
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Em caso de erro, use a imagem padrão
                carregarImagemPadrao();
            }
        }).start();
    }


    private void carregarImagemPadrao() {
        runOnUiThread(() -> {
            try {

                Drawable drawable = getResources().getDrawable(R.drawable.ic_perfil_padrao);
                Bitmap bitmap;

                if (drawable instanceof BitmapDrawable) {
                    bitmap = ((BitmapDrawable) drawable).getBitmap();
                } else {

                    bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            Bitmap.Config.ARGB_8888);
                    android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);
                }

                // Cria um drawable circular a partir do bitmap
                RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                roundedDrawable.setCircular(true);
                roundedDrawable.setAntiAlias(true);
                fotoPerfilChat.setImageDrawable(roundedDrawable);
            } catch (Exception e) {
                e.printStackTrace();
                // Se tudo falhar, pelo menos tenta mostrar o ícone original
                fotoPerfilChat.setImageResource(R.drawable.ic_perfil_padrao);
            }
        });
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
                        String textoOriginal = rs.getString("texto");
                        String textoDescriptografado;

                        // Descriptografa a mensagem antes de exibir
                        if (CriptoUtils.estaCriptografado(textoOriginal)) {
                            textoDescriptografado = CriptoUtils.descriptografar(textoOriginal);
                            Log.d("Chat", "Mensagem descriptografada: " + textoOriginal + " -> " + textoDescriptografado);
                        } else {
                            // Se não estiver criptografada (mensagens antigas), mantém original
                            textoDescriptografado = textoOriginal;
                            Log.d("Chat", "Mensagem não criptografada: " + textoOriginal);
                        }

                        Mensagem msg = new Mensagem(
                                rs.getInt("id_mensagem"),
                                rs.getInt("id_usuario_remetente"),
                                rs.getInt("id_usuario_destinatario"),
                                textoDescriptografado, // Usa o texto descriptografado
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


        final Mensagem novaMensagem = new Mensagem(
                0, // ID temporário
                idUsuarioLogado,
                idDestinatario,
                texto, // Texto original para exibição
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

                // Criptografa a mensagem antes de salvar no banco
                String textoCriptografado = CriptoUtils.criptografar(texto);
                Log.d("Chat", "Mensagem criptografada: " + texto + " -> " + textoCriptografado);

                String query = "INSERT INTO Mensagem (id_usuario_remetente, id_usuario_destinatario, texto, lida) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setInt(1, idUsuarioLogado);
                ps.setInt(2, idDestinatario);
                ps.setString(3, textoCriptografado); // Salva o texto criptografado
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
    protected void onResume() {
        super.onResume();
        // Verifica novamente se é contato ao retornar para a tela
        verificarSeJaEContato();
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