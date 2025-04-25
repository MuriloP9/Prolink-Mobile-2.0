package com.example.prolink.Activity;

import android.Manifest;
import android.content.Intent;
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

public class CadastroActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText editUsername, editEmail, editPassword;
    private Button btnEnter, btnSelectedPhoto;
    private ImageView imgProfile;
    private ClasseConexao conexao;
    private byte[] fotoPerfilBytes;
    //private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSAO_GALERIA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        // Inicialização dos componentes
        editUsername = findViewById(R.id.edit_username);
        editEmail = findViewById(R.id.edit_email);
        editPassword = findViewById(R.id.edit_password);
        btnEnter = findViewById(R.id.btn_enter);
        btnSelectedPhoto = findViewById(R.id.btn_selected_photo);
        imgProfile = findViewById(R.id.imageView4);
        conexao = new ClasseConexao(this);

        btnSelectedPhoto.setOnClickListener(v -> abrirGaleria());
        btnEnter.setOnClickListener(v -> cadastrarUsuario());
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
                // 1. Carrega a imagem original
                Bitmap originalBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);

                // 2. Cria um bitmap circular
                Bitmap circularBitmap = getCircularBitmap(originalBitmap);

                // 3. Define a imagem circular no ImageView
                imgProfile.setImageBitmap(circularBitmap);
                imgProfile.setVisibility(View.VISIBLE);
                btnSelectedPhoto.setText("");

                // 4. Converte para byte array (usando apenas uma compressão)
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                circularBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                fotoPerfilBytes = stream.toByteArray();

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

    private void cadastrarUsuario() {
        String nome = editUsername.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editPassword.getText().toString().trim();

        // Validações (mantenha as que você já tem)

        new CadastroTask().execute(nome, email, senha);
    }

    private class CadastroTask extends AsyncTask<String, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(String... params) {
            Connection conn = null;
            PreparedStatement ps = null;

            try {
                conn = conexao.getConnection();
                if (conn == null) {
                    errorMessage = "Sem conexão com o banco de dados";
                    return false;
                }

                String sql = "INSERT INTO Usuario (nome, email, senha, foto_perfil) VALUES (?, ?, ?, ?)";
                ps = conn.prepareStatement(sql);

                ps.setString(1, params[0]); // nome
                ps.setString(2, params[1]); // email
                ps.setString(3, params[2]); // senha

                // Se tiver foto, envia, senão envia NULL
                if (fotoPerfilBytes != null) {
                    ps.setBytes(4, fotoPerfilBytes);
                } else {
                    ps.setNull(4, java.sql.Types.VARBINARY);
                }

                return ps.executeUpdate() > 0;

            } catch (Exception e) {
                errorMessage = e.getMessage();
                return false;
            } finally {
                conexao.closeStatement(ps);
                conexao.closeConnection(conn);
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();

                // Passa os dados para a LoginActivity
                Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
                intent.putExtra("REGISTERED_EMAIL", editEmail.getText().toString());
                startActivity(intent);
                finish();
            } else {
                String message;
                if (errorMessage.contains("UNIQUE")) {
                    message = "Este email já está cadastrado";
                } else if (errorMessage.contains("connection")) {
                    message = "Erro de conexão com o servidor";
                } else {
                    message = "Erro ao cadastrar: " + errorMessage;
                }
                Toast.makeText(CadastroActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}