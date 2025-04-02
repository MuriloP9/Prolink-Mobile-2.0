package com.example.prolink.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.prolink.R;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ContatosActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ContatoAdapter adapter;
    private List<Usuario> contatos = new ArrayList<>();
    private ClasseConexao conexao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contatos);

        // Inicializa as views
        recyclerView = findViewById(R.id.recycler_contatos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configura o adaptador com o listener de clique
        adapter = new ContatoAdapter(contatos, usuario -> abrirChat(usuario));
        recyclerView.setAdapter(adapter);

        // Inicializa a conexão com o banco
        conexao = new ClasseConexao(this);

        // Carrega os contatos
        carregarContatos();
    }

    private void carregarContatos() {
        new Thread(() -> {
            try {
                // Obtém o ID do usuário logado
                SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
                int idUsuarioLogado = prefs.getInt("ID_USUARIO", 0);

                // Query para buscar todos os usuários exceto o logado
                String query = "SELECT u.id_usuario, u.nome, u.email FROM Usuario u " +
                        "WHERE u.id_usuario != " + idUsuarioLogado;

                // Executa a consulta
                Connection conn = conexao.getConnection();
                if (conn != null) {
                    ResultSet rs = conexao.executarConsulta(query);
                    List<Usuario> novosContatos = new ArrayList<>();

                    // Processa os resultados
                    while (rs.next()) {
                        Usuario usuario = new Usuario(
                                rs.getInt("id_usuario"),
                                rs.getString("nome"),
                                rs.getString("email")
                        );
                        novosContatos.add(usuario);
                    }

                    // Atualiza a UI na thread principal
                    runOnUiThread(() -> {
                        contatos.clear();
                        contatos.addAll(novosContatos);
                        adapter.notifyDataSetChanged();
                    });

                    // Fecha os recursos
                    rs.close();
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