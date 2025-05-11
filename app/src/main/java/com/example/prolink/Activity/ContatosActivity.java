package com.example.prolink.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contatos);

        // Inicializa as views
        recyclerView = findViewById(R.id.recycler_contatos);
        tvSemContatos = findViewById(R.id.tv_sem_contatos);
        fabAdicionarContato = findViewById(R.id.fab_adicionar_contato);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configura o adaptador com o listener de clique
        adapter = new ContatoAdapter(contatos, usuario -> abrirChat(usuario));
        recyclerView.setAdapter(adapter);

        // Inicializa a conexão com o banco
        conexao = new ClasseConexao(this);

        // Configura o FloatingActionButton para adicionar contatos
        fabAdicionarContato.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContatosActivity.this, AddContactLinkActivity.class);
                startActivity(intent);
            }
        });

        // Carrega os contatos
        carregarContatos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega a lista de contatos ao voltar para esta tela
        carregarContatos();
    }

    private void carregarContatos() {
        new Thread(() -> {
            try {
                // Obtém o ID do usuário logado
                SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
                int idUsuarioLogado = prefs.getInt("ID_USUARIO", 0);

                // Query para buscar apenas os contatos adicionados pelo usuário
                String query = "SELECT u.id_usuario, u.nome, u.email, u.foto_perfil " +
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
                        Usuario usuario = new Usuario(
                                rs.getInt("id_usuario"),
                                rs.getString("nome"),
                                rs.getString("email"),
                                rs.getString("foto_perfil")
                        );
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

    private void abrirChat(Usuario usuario) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("ID_DESTINATARIO", usuario.getId());
        intent.putExtra("NOME_DESTINATARIO", usuario.getNome());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpeza de recursos se necessário
    }
}