package com.example.prolink.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prolink.R;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView txtWelcome;
    private ImageView imgQRCode, imgChat, imgContatos, imgNotificacoes;
    private ImageView profileButton, settingsButton, profileImage;
    private ClasseConexao conexao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        conexao = new ClasseConexao(this);

        initializeViews();
        setupClickListeners();
        handleUserData();
    }

    private void initializeViews() {
        txtWelcome = findViewById(R.id.textView);
        imgQRCode = findViewById(R.id.imageView11);
        imgChat = findViewById(R.id.imageView14);
        imgContatos = findViewById(R.id.imageView12);
        imgNotificacoes = findViewById(R.id.imageView10);
        profileButton = findViewById(R.id.profileButton);
        settingsButton = findViewById(R.id.settingsButton);
        profileImage = findViewById(R.id.profileImage);
    }

    private void setupClickListeners() {
        imgQRCode.setOnClickListener(this);
        imgChat.setOnClickListener(this);
        imgContatos.setOnClickListener(this);
        imgNotificacoes.setOnClickListener(this);
        profileButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
    }

    private void handleUserData() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String nomeUsuario = extras.getString("NOME_USUARIO");
            int idUsuario = extras.getInt("ID_USUARIO");

            if (nomeUsuario != null && !nomeUsuario.isEmpty()) {
                setWelcomeMessage(nomeUsuario);
            }

            saveUserId(idUsuario);
            loadProfileImage(idUsuario);
        }
    }

    private void setWelcomeMessage(String fullName) {
        String primeiroNome = fullName.split(" ")[0];
        txtWelcome.setText("Olá, " + primeiroNome);
    }

    private void saveUserId(int idUsuario) {
        SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("ID_USUARIO", idUsuario);
        editor.apply();
    }

    private void loadProfileImage(int userId) {
        new LoadProfileImageTask().execute(userId);
    }

    private class LoadProfileImageTask extends AsyncTask<Integer, Void, Bitmap> {
        private String errorMessage = "";

        @Override
        protected Bitmap doInBackground(Integer... params) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            Bitmap bitmap = null;

            try {
                conn = conexao.getConnection();
                if (conn == null) {
                    errorMessage = "Sem conexão com o banco de dados";
                    return null;
                }

                String sql = "SELECT foto_perfil FROM Usuario WHERE id_usuario = ?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, params[0]);
                rs = ps.executeQuery();

                if (rs.next()) {
                    byte[] imageBytes = rs.getBytes("foto_perfil");
                    if (imageBytes != null && imageBytes.length > 0) {
                        bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    } else {
                        Log.d("IMAGE_LOAD", "Imagem vazia ou nula no banco de dados");
                    }
                } else {
                    Log.d("IMAGE_LOAD", "Nenhum resultado encontrado para o usuário ID: " + params[0]);
                }

            } catch (SQLException e) {
                errorMessage = e.getMessage();
                Log.e("SQL_ERROR", "Erro ao carregar imagem: " + e.getMessage());
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (ps != null) ps.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    Log.e("SQL_ERROR", "Erro ao fechar conexão: " + e.getMessage());
                }
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                try {
                    Bitmap circularBitmap = getCircularBitmap(bitmap);
                    profileImage.setImageBitmap(circularBitmap);
                    Log.d("IMAGE_LOAD", "Imagem carregada com sucesso");
                } catch (Exception e) {
                    Log.e("IMAGE_ERROR", "Erro ao processar imagem: " + e.getMessage());
                    setDefaultProfileImage();
                }
            } else {
                Log.d("IMAGE_LOAD", "Usando imagem padrão");
                setDefaultProfileImage();

                if (!errorMessage.isEmpty()) {
                    showToast("Erro ao carregar imagem: " + errorMessage);
                }
            }
        }
    }

    private Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        android.graphics.Canvas canvas = new android.graphics.Canvas(output);
        final int color = 0xff424242;
        final android.graphics.Paint paint = new android.graphics.Paint();
        final android.graphics.Rect rect = new android.graphics.Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);

        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private void setDefaultProfileImage() {
        profileImage.setImageResource(R.drawable.perfil);
    }

    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.profileButton) {
            openProfileActivity();
        } else if (id == R.id.settingsButton) {
            openSettingsActivity();
        } else if (id == R.id.imageView11) {
            startActivity(new Intent(this, QRCodeActivity.class));
        } else if (id == R.id.imageView14) {
            openChatUrl();
        } else if (id == R.id.imageView12) {
            startActivity(new Intent(this, ContatosActivity.class));
        } else if (id == R.id.imageView10) {
            startActivity(new Intent(this, NotificacoesActivity.class));
        }
    }

    private void openProfileActivity() {
        SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("ID_USUARIO", 0);

        if (userId > 0) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USER_ID", userId);
            startActivity(intent);
        } else {
            showToast("Usuário não identificado");
        }
    }

    private void openSettingsActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void openChatUrl() {
        String url = "http://10.0.2.2/Projeto-Networking/src/php/index.php";

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setPackage("com.android.chrome");
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception ex) {
                showToast("Configure o Chrome como navegador padrão ou tente novamente");
                Log.e("URL_Error", "Erro ao abrir URL: " + ex.getMessage());
            }
        }
    }

    public void exibirMensagemBoasVindas(String nomeUsuario) {
        txtWelcome.setText("Olá, " + nomeUsuario);
        showToast("Bem-vindo, " + nomeUsuario + "!");
    }
}