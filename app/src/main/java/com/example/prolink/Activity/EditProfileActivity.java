package com.example.prolink.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.prolink.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSAO_GALERIA = 2;

    // Componentes da interface
    private EditText editNome, editTelefone, editDataNascimento, editIdade,
            editEndereco, editFormacao, editExperiencia, editInteresses,
            editProjetos, editHabilidades;
    private Button btnSalvar, btnSelecionarFoto;
    private ImageView imgProfile;

    // Variáveis de controle
    private ClasseConexao conexao;
    private byte[] fotoPerfilBytes;
    private int idUsuario;
    private boolean fotoAlterada = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        inicializarComponentes();

        if (obterIdUsuario()) {
            Log.d(TAG, "ID do usuário obtido: " + idUsuario);
            carregarDadosUsuario();
        } else {
            Log.e(TAG, "Não foi possível obter ID do usuário");
            mostrarErroESair();
        }

        btnSelecionarFoto.setOnClickListener(v -> abrirGaleria());
        btnSalvar.setOnClickListener(v -> atualizarPerfil());
    }

    private void inicializarComponentes() {
        // Dados básicos
        editNome = findViewById(R.id.edit_nome);
        editTelefone = findViewById(R.id.edit_telefone);
        editDataNascimento = findViewById(R.id.edit_data_nascimento);

        // Dados do perfil
        editIdade = findViewById(R.id.edit_idade);
        editEndereco = findViewById(R.id.edit_endereco);
        editFormacao = findViewById(R.id.edit_formacao);
        editExperiencia = findViewById(R.id.edit_experiencia);
        editInteresses = findViewById(R.id.edit_interesses);
        editProjetos = findViewById(R.id.edit_projetos);
        editHabilidades = findViewById(R.id.edit_habilidades);

        // Botões e imagem
        btnSalvar = findViewById(R.id.btn_salvar);
        btnSelecionarFoto = findViewById(R.id.btn_selected_photo);
        imgProfile = findViewById(R.id.img_profile);

        conexao = new ClasseConexao(this);
    }

    private boolean obterIdUsuario() {
        // 1. Primeiro tenta obter do Intent (dados passados pela Activity anterior)
        Intent intent = getIntent();
        if (intent != null) {
            idUsuario = intent.getIntExtra("ID_USUARIO", -1);
            if (idUsuario == -1) {
                idUsuario = intent.getIntExtra("id_usuario", -1);
            }
            if (idUsuario == -1) {
                idUsuario = intent.getIntExtra("user_id", -1);
            }

            Log.d(TAG, "ID do usuário do Intent: " + idUsuario);

            if (idUsuario != -1) {
                // Salva na sessão para próximas vezes
                salvarSessao(intent);
                return true;
            }
        }

        // 2. Tenta obter das SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        idUsuario = prefs.getInt("id_usuario", -1);

        Log.d(TAG, "ID do usuário das SharedPreferences: " + idUsuario);

        if (idUsuario != -1) {
            return true;
        }

        // 3. Debug das SharedPreferences
        debugSharedPreferences();

        return false;
    }

    private void salvarSessao(Intent intent) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Salva dados do Intent nas SharedPreferences
        if (intent.hasExtra("ID_USUARIO")) {
            editor.putInt("id_usuario", intent.getIntExtra("ID_USUARIO", -1));
        }
        if (intent.hasExtra("NOME_USUARIO")) {
            editor.putString("nome", intent.getStringExtra("NOME_USUARIO"));
        }
        if (intent.hasExtra("EMAIL_USUARIO")) {
            editor.putString("email", intent.getStringExtra("EMAIL_USUARIO"));
        }

        boolean saved = editor.commit();
        Log.d(TAG, "Sessão salva do Intent: " + saved);
    }

    private void debugSharedPreferences() {
        Log.d(TAG, "=== DEBUG SHARED PREFERENCES ===");

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        java.util.Map<String, ?> allEntries = prefs.getAll();

        if (!allEntries.isEmpty()) {
            Log.d(TAG, "SharedPreferences 'user_session':");
            for (java.util.Map.Entry<String, ?> entry : allEntries.entrySet()) {
                Log.d(TAG, "  " + entry.getKey() + " = " + entry.getValue());
            }
        } else {
            Log.d(TAG, "SharedPreferences 'user_session' está vazia");
        }

        Log.d(TAG, "=== FIM DEBUG ===");
    }

    private void mostrarErroESair() {
        Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show();

        // Limpa a sessão
        LoginActivity.limparSessao(this);

        // Redireciona para a tela de login
        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void abrirGaleria() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } else {
            verificarPermissoes();
        }
    }

    private void verificarPermissoes() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSAO_GALERIA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSAO_GALERIA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirGaleria();
            } else {
                Toast.makeText(this, "Permissão negada para acessar a galeria", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            try {
                // Carrega a imagem original
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);

                // Cria um bitmap circular
                Bitmap circularBitmap = getCircularBitmap(originalBitmap);

                // Define a imagem circular no ImageView
                imgProfile.setImageBitmap(circularBitmap);
                imgProfile.setVisibility(View.VISIBLE);
                btnSelecionarFoto.setText("");

                // Converte para byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                circularBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                fotoPerfilBytes = stream.toByteArray();
                fotoAlterada = true;

            } catch (IOException e) {
                Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para criar bitmap circular
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, 150, 150);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(75, 75, 75, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // Redimensiona mantendo proporção
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, false);
        canvas.drawBitmap(scaledBitmap, rect, rect, paint);

        return output;
    }

    private void carregarDadosUsuario() {
        if (idUsuario != -1) {
            new CarregarDadosTask().execute();
        } else {
            mostrarErroESair();
        }
    }

    private void atualizarPerfil() {
        if (idUsuario == -1) {
            Toast.makeText(this, "Erro: usuário não identificado", Toast.LENGTH_SHORT).show();
            return;
        }

        String nome = editNome.getText().toString().trim();
        String telefone = editTelefone.getText().toString().trim();
        String dataNascimento = editDataNascimento.getText().toString().trim();
        String idadeStr = editIdade.getText().toString().trim();
        String endereco = editEndereco.getText().toString().trim();
        String formacao = editFormacao.getText().toString().trim();
        String experiencia = editExperiencia.getText().toString().trim();
        String interesses = editInteresses.getText().toString().trim();
        String projetos = editProjetos.getText().toString().trim();
        String habilidades = editHabilidades.getText().toString().trim();

        // Validação básica
        if (nome.isEmpty()) {
            editNome.setError("Nome é obrigatório");
            return;
        }

        Integer idade = null;
        if (!idadeStr.isEmpty()) {
            try {
                idade = Integer.parseInt(idadeStr);
                if (idade < 0 || idade > 120) {
                    editIdade.setError("Idade inválida");
                    return;
                }
            } catch (NumberFormatException e) {
                editIdade.setError("Idade deve ser um número válido");
                return;
            }
        }

        new AtualizarPerfilTask().execute(nome, telefone, dataNascimento,
                String.valueOf(idade), endereco, formacao,
                experiencia, interesses, projetos, habilidades);
    }

    // Task para carregar dados do usuário
    private class CarregarDadosTask extends AsyncTask<Void, Void, Boolean> {
        private String errorMessage = "";
        private String nome, telefone, dataNascimento, email;
        private Integer idade;
        private String endereco, formacao, experiencia, interesses, projetos, habilidades;
        private byte[] fotoBytes;

        @Override
        protected Boolean doInBackground(Void... params) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                conn = conexao.getConnection();
                if (conn == null) {
                    errorMessage = "Sem conexão com o banco de dados";
                    Log.e(TAG, errorMessage);
                    return false;
                }

                Log.d(TAG, "Buscando dados para usuário ID: " + idUsuario);

                // Primeiro, verifica se o usuário existe
                String sqlCheck = "SELECT COUNT(*) FROM Usuario WHERE id_usuario = ?";
                ps = conn.prepareStatement(sqlCheck);
                ps.setInt(1, idUsuario);
                rs = ps.executeQuery();

                if (rs.next() && rs.getInt(1) == 0) {
                    errorMessage = "Usuário não encontrado no banco de dados (ID: " + idUsuario + ")";
                    Log.e(TAG, errorMessage);
                    return false;
                }

                rs.close();
                ps.close();

                // Busca dados básicos do usuário incluindo foto
                String sql = "SELECT nome, foto_perfil, email, telefone, dataNascimento FROM Usuario WHERE id_usuario = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, idUsuario);
                rs = ps.executeQuery();

                if (rs.next()) {
                    nome = rs.getString("nome");
                    email = rs.getString("email");
                    telefone = rs.getString("telefone");
                    dataNascimento = rs.getString("dataNascimento");
                    fotoBytes = rs.getBytes("foto_perfil");

                    Log.d(TAG, "Dados básicos carregados - Nome: " + nome + ", Email: " + email);
                } else {
                    errorMessage = "Usuário não encontrado no banco de dados (ID: " + idUsuario + ")";
                    Log.e(TAG, errorMessage);
                    return false;
                }

                rs.close();
                ps.close();

                // Busca dados do perfil
                sql = "SELECT idade, endereco, formacao, experiencia_profissional, " +
                        "interesses, projetos_especializacoes, habilidades " +
                        "FROM Perfil WHERE id_usuario = ?";

                ps = conn.prepareStatement(sql);
                ps.setInt(1, idUsuario);
                rs = ps.executeQuery();

                if (rs.next()) {
                    int idadeValue = rs.getInt("idade");
                    if (rs.wasNull()) {
                        idade = null;
                    } else {
                        idade = idadeValue;
                    }
                    endereco = rs.getString("endereco");
                    formacao = rs.getString("formacao");
                    experiencia = rs.getString("experiencia_profissional");
                    interesses = rs.getString("interesses");
                    projetos = rs.getString("projetos_especializacoes");
                    habilidades = rs.getString("habilidades");

                    Log.d(TAG, "Dados do perfil carregados");
                } else {
                    Log.d(TAG, "Perfil não existe ainda para este usuário");
                }

                return true;

            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Erro ao carregar dados do usuário", e);
                return false;
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (ps != null) ps.close();
                    if (conn != null) conn.close();
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao fechar recursos", e);
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Log.d(TAG, "Dados carregados com sucesso");
                // Preenche os campos com os dados carregados
                editNome.setText(nome != null ? nome : "");
                editTelefone.setText(telefone != null ? telefone : "");
                editDataNascimento.setText(dataNascimento != null ? dataNascimento : "");
                editIdade.setText(idade != null ? String.valueOf(idade) : "");
                editEndereco.setText(endereco != null ? endereco : "");
                editFormacao.setText(formacao != null ? formacao : "");
                editExperiencia.setText(experiencia != null ? experiencia : "");
                editInteresses.setText(interesses != null ? interesses : "");
                editProjetos.setText(projetos != null ? projetos : "");
                editHabilidades.setText(habilidades != null ? habilidades : "");

                // Carrega foto de perfil se existir
                if (fotoBytes != null && fotoBytes.length > 0) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
                    if (bitmap != null) {
                        Bitmap circularBitmap = getCircularBitmap(bitmap);
                        imgProfile.setImageBitmap(circularBitmap);
                        imgProfile.setVisibility(View.VISIBLE);
                        btnSelecionarFoto.setText("");
                    }
                }
            } else {
                Log.e(TAG, "Falha ao carregar dados: " + errorMessage);
                Toast.makeText(EditProfileActivity.this,
                        "Erro ao carregar dados: " + errorMessage,
                        Toast.LENGTH_LONG).show();

                if (errorMessage.contains("não encontrado")) {
                    mostrarErroESair();
                }
            }
        }
    }

    // Task para atualizar perfil
    private class AtualizarPerfilTask extends AsyncTask<String, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(String... params) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                conn = conexao.getConnection();
                if (conn == null) {
                    errorMessage = "Sem conexão com o banco de dados";
                    return false;
                }

                conn.setAutoCommit(false); // Inicia transação

                // Atualiza dados básicos na tabela Usuario
                String sql = "UPDATE Usuario SET nome = ?, telefone = ?, dataNascimento = ?";
                if (fotoAlterada) {
                    sql += ", foto_perfil = ?";
                }
                sql += " WHERE id_usuario = ?";

                ps = conn.prepareStatement(sql);
                ps.setString(1, params[0]); // nome
                ps.setString(2, params[1]); // telefone
                ps.setString(3, params[2]); // dataNascimento

                int paramIndex = 4;
                if (fotoAlterada) {
                    if (fotoPerfilBytes != null) {
                        ps.setBytes(4, fotoPerfilBytes);
                    } else {
                        ps.setNull(4, java.sql.Types.VARBINARY);
                    }
                    paramIndex = 5;
                }
                ps.setInt(paramIndex, idUsuario);

                int rowsAffected = ps.executeUpdate();
                if (rowsAffected == 0) {
                    errorMessage = "Usuário não encontrado para atualização";
                    return false;
                }

                ps.close();

                // Verifica se já existe um perfil para este usuário
                String sqlCheck = "SELECT COUNT(*) FROM Perfil WHERE id_usuario = ?";
                ps = conn.prepareStatement(sqlCheck);
                ps.setInt(1, idUsuario);
                rs = ps.executeQuery();

                boolean perfilExiste = false;
                if (rs.next()) {
                    perfilExiste = rs.getInt(1) > 0;
                }
                rs.close();
                ps.close();

                // Atualiza ou insere dados do perfil
                if (perfilExiste) {
                    // Atualiza perfil existente
                    sql = "UPDATE Perfil SET idade = ?, endereco = ?, formacao = ?, " +
                            "experiencia_profissional = ?, interesses = ?, projetos_especializacoes = ?, " +
                            "habilidades = ? WHERE id_usuario = ?";
                } else {
                    // Insere novo perfil
                    sql = "INSERT INTO Perfil (idade, endereco, formacao, experiencia_profissional, " +
                            "interesses, projetos_especializacoes, habilidades, id_usuario) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                }

                ps = conn.prepareStatement(sql);

                // Converte idade de String para Integer
                if (params[3] != null && !params[3].equals("null") && !params[3].isEmpty()) {
                    try {
                        ps.setInt(1, Integer.parseInt(params[3]));
                    } catch (NumberFormatException e) {
                        ps.setNull(1, java.sql.Types.INTEGER);
                    }
                } else {
                    ps.setNull(1, java.sql.Types.INTEGER);
                }

                ps.setString(2, params[4]); // endereco
                ps.setString(3, params[5]); // formacao
                ps.setString(4, params[6]); // experiencia
                ps.setString(5, params[7]); // interesses
                ps.setString(6, params[8]); // projetos
                ps.setString(7, params[9]); // habilidades
                ps.setInt(8, idUsuario);

                ps.executeUpdate();

                conn.commit(); // Confirma a transação
                return true;

            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Erro ao atualizar perfil", e);

                try {
                    if (conn != null) {
                        conn.rollback(); // Desfaz a transação em caso de erro
                    }
                } catch (Exception rollbackEx) {
                    Log.e(TAG, "Erro ao fazer rollback", rollbackEx);
                }

                return false;
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (ps != null) ps.close();
                    if (conn != null) {
                        conn.setAutoCommit(true); // Restaura auto commit
                        conn.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao fechar recursos", e);
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(EditProfileActivity.this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();

                // Atualiza SharedPreferences com o novo nome se foi alterado
                String novoNome = editNome.getText().toString().trim();
                SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("nome", novoNome);
                editor.commit();

                Log.d(TAG, "Perfil atualizado com sucesso");

                // Volta para a tela anterior
                finish();
            } else {
                Toast.makeText(EditProfileActivity.this,
                        "Erro ao atualizar perfil: " + errorMessage,
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "Erro ao atualizar perfil: " + errorMessage);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpa referências para evitar memory leaks
        if (fotoPerfilBytes != null) {
            fotoPerfilBytes = null;
        }
    }
}