package com.example.prolink.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prolink.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ContatosActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ContatoAdapter adapter;
    private List<Usuario> contatos = new ArrayList<>();
    private ClasseConexao conexao;
    private TextView tvSemContatos;
    private FloatingActionButton fabAdicionarContato;
    private int idUsuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contatos);

        // Obter ID do usuário logado
        SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
        idUsuarioLogado = prefs.getInt("ID_USUARIO", 0);

        // Inicializar views
        recyclerView = findViewById(R.id.recycler_contatos);
        tvSemContatos = findViewById(R.id.tv_sem_contatos);
        fabAdicionarContato = findViewById(R.id.fab_adicionar_contato);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar adapter
        adapter = new ContatoAdapter(contatos, usuario -> abrirChat(usuario));
        adapter.setRemoveListener((usuario, position) -> confirmarRemocaoContato(usuario, position));
        adapter.setBloqueioListener((usuario, position, bloquear) -> confirmarAlteracaoBloqueio(usuario, position, bloquear));
        recyclerView.setAdapter(adapter);

        // Inicializar conexão com o banco de dados
        conexao = new ClasseConexao(this);

        // Configurar botão de adicionar contato
        fabAdicionarContato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContatosActivity.this, AddContactLinkActivity.class);
                startActivity(intent);
            }
        });

        // Carregar contatos
        carregarContatos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar contatos ao retornar para a activity
        carregarContatos();
    }

    private void carregarContatos() {
        new Thread(() -> {
            try {
                // Consulta SQL para obter contatos com foto de perfil e status de bloqueio
                String query = "SELECT u.id_usuario, u.nome, u.email, u.foto_perfil, c.bloqueado " +
                        "FROM Usuario u " +
                        "INNER JOIN Contatos c ON u.id_usuario = c.id_contato " +
                        "WHERE c.id_usuario = ?";

                // Executa a consulta
                Connection conn = conexao.getConnection();
                if (conn != null) {
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, idUsuarioLogado);
                    ResultSet rs = pstmt.executeQuery();

                    List<Usuario> novosContatos = new ArrayList<>();

                    // Processa os resultados
                    while (rs.next()) {
                        int id = rs.getInt("id_usuario");
                        String nome = rs.getString("nome");
                        String email = rs.getString("email");
                        boolean bloqueado = rs.getBoolean("bloqueado");

                        // Processar a foto de perfil
                        String fotoPerfilBase64 = null;
                        byte[] fotoPerfilBytes = rs.getBytes("foto_perfil");
                        if (fotoPerfilBytes != null && fotoPerfilBytes.length > 0) {
                            fotoPerfilBase64 = Base64.encodeToString(fotoPerfilBytes, Base64.DEFAULT);
                        }

                        Usuario usuario = new Usuario(id, nome, email, fotoPerfilBase64, bloqueado);
                        novosContatos.add(usuario);
                    }

                    // Atualiza a UI na thread principal
                    runOnUiThread(() -> {
                        contatos.clear();
                        contatos.addAll(novosContatos);
                        adapter.notifyDataSetChanged();

                        // Mostra mensagem se não houver contatos
                        if (contatos.isEmpty()) {
                            tvSemContatos.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            tvSemContatos.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    });

                    // Fecha os recursos
                    rs.close();
                    pstmt.close();
                    conn.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao carregar contatos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void confirmarRemocaoContato(Usuario usuario, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remover contato")
                .setMessage("Deseja remover " + usuario.getNome() + " da sua lista de contatos?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    removerContato(usuario.getId(), position);
                })
                .setNegativeButton("Não", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void confirmarAlteracaoBloqueio(Usuario usuario, int position, boolean bloquear) {
        String acao = bloquear ? "bloquear" : "desbloquear";
        String mensagem = "Deseja " + acao + " " + usuario.getNome() + "?";

        if (bloquear) {
            mensagem += "\n\nContatos bloqueados não poderão enviar mensagens para você.";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(bloquear ? "Bloquear contato" : "Desbloquear contato")
                .setMessage(mensagem)
                .setPositiveButton("Sim", (dialog, which) -> {
                    alterarStatusBloqueio(usuario.getId(), position, bloquear);
                })
                .setNegativeButton("Não", (dialog, which) -> {
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    private void removerContato(int idContato, int position) {
        new Thread(() -> {
            try {
                Connection conn = conexao.getConnection();
                if (conn != null) {
                    // SQL para remover o contato
                    String query = "DELETE FROM Contatos WHERE id_usuario = ? AND id_contato = ?";

                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, idUsuarioLogado);
                    pstmt.setInt(2, idContato);

                    int rowsAffected = pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    if (rowsAffected > 0) {
                        // Sucesso - atualize a UI na thread principal
                        runOnUiThread(() -> {
                            adapter.removerContato(position);
                            Toast.makeText(ContatosActivity.this, "Contato removido com sucesso", Toast.LENGTH_SHORT).show();

                            // Verifica se a lista de contatos está vazia
                            if (contatos.isEmpty()) {
                                tvSemContatos.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        // Falha - exiba uma mensagem de erro
                        runOnUiThread(() -> {
                            Toast.makeText(ContatosActivity.this, "Erro ao remover contato", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(ContatosActivity.this, "Erro de conexão com o banco de dados", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ContatosActivity.this, "Erro ao remover contato: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void alterarStatusBloqueio(int idContato, int position, boolean bloquear) {
        new Thread(() -> {
            try {
                Connection conn = conexao.getConnection();
                if (conn != null) {
                    // SQL para atualizar o status de bloqueio
                    String query = "UPDATE Contatos SET bloqueado = ? WHERE id_usuario = ? AND id_contato = ?";

                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setBoolean(1, bloquear);
                    pstmt.setInt(2, idUsuarioLogado);
                    pstmt.setInt(3, idContato);

                    int rowsAffected = pstmt.executeUpdate();
                    pstmt.close();
                    conn.close();

                    if (rowsAffected > 0) {
                        // Sucesso - atualize a UI na thread principal
                        runOnUiThread(() -> {
                            adapter.atualizarStatusBloqueio(position, bloquear);
                            String mensagem = bloquear ?
                                    "Contato bloqueado com sucesso" :
                                    "Contato desbloqueado com sucesso";
                            Toast.makeText(ContatosActivity.this, mensagem, Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        // Falha - exiba uma mensagem de erro
                        runOnUiThread(() -> {
                            Toast.makeText(ContatosActivity.this, "Erro ao atualizar status do contato", Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(ContatosActivity.this, "Erro de conexão com o banco de dados", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(ContatosActivity.this, "Erro ao atualizar status do contato: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void abrirChat(Usuario usuario) {
        // Verifica se o contato está bloqueado antes de abrir o chat
        if (usuario.isBloqueado()) {
            Toast.makeText(this, "Este contato está bloqueado. Desbloqueie-o para iniciar uma conversa.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ID_DESTINATARIO", usuario.getId());
        intent.putExtra("NOME_DESTINATARIO", usuario.getNome());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar recursos se necessário
    }
}