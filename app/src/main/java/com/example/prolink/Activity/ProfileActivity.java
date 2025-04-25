package com.example.prolink.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.prolink.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView imgProfile;
    private TextView tvName, tvEmail, tvBirthdate, tvEducation, tvExperience, tvInterests, tvProjects, tvSkills;
    private ClasseConexao conexao;
    private int userId; // ID do usuário logado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inicializa componentes
        btnBack = findViewById(R.id.btn_back);
        imgProfile = findViewById(R.id.img_profile);
        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        tvBirthdate = findViewById(R.id.tv_birthdate);
        tvEducation = findViewById(R.id.tv_education);
        tvExperience = findViewById(R.id.tv_experience);
        tvInterests = findViewById(R.id.tv_interests);
        tvProjects = findViewById(R.id.tv_projects);
        tvSkills = findViewById(R.id.tv_skills);
        conexao = new ClasseConexao(this);

        // Botão de voltar
        btnBack.setOnClickListener(v -> finish());

        // Obter ID do usuário (você pode passar via Intent ou pegar do SharedPreferences)
        userId = getIntent().getIntExtra("USER_ID", 0);

        if (userId > 0) {
            new LoadProfileTask().execute(userId);
        } else {
            Toast.makeText(this, "Erro ao carregar perfil", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private class LoadProfileTask extends AsyncTask<Integer, Void, ProfileData> {
        private String errorMessage = "";

        @Override
        protected ProfileData doInBackground(Integer... params) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            ProfileData profileData = new ProfileData();

            try {
                conn = conexao.getConnection();
                if (conn == null) {
                    errorMessage = "Sem conexão com o banco de dados";
                    return null;
                }

                // Consulta para obter dados do usuário e perfil
                String sql = "SELECT u.nome, u.email, u.dataNascimento, u.foto_perfil, " +
                        "p.formacao, p.experiencia_profissional, p.interesses, p.projetos_especializacoes, p.habilidades " +
                        "FROM Usuario u LEFT JOIN Perfil p ON u.id_usuario = p.id_usuario " +
                        "WHERE u.id_usuario = ?";

                ps = conn.prepareStatement(sql);
                ps.setInt(1, params[0]);
                rs = ps.executeQuery();

                if (rs.next()) {
                    profileData.name = rs.getString("nome");
                    profileData.email = rs.getString("email");

                    // Formata data de nascimento
                    if (rs.getDate("dataNascimento") != null) {
                        profileData.birthdate = new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("dataNascimento"));
                    } else {
                        profileData.birthdate = "Não informado";
                    }

                    // Foto de perfil
                    byte[] fotoBytes = rs.getBytes("foto_perfil");
                    if (fotoBytes != null) {
                        profileData.profileImage = BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
                    }

                    // Dados do perfil
                    profileData.education = rs.getString("formacao") != null ? rs.getString("formacao") : "Não informado";
                    profileData.experience = rs.getString("experiencia_profissional") != null ? rs.getString("experiencia_profissional") : "Não informado";
                    profileData.interests = rs.getString("interesses") != null ? rs.getString("interesses") : "Não informado";
                    profileData.projects = rs.getString("projetos_especializacoes") != null ? rs.getString("projetos_especializacoes") : "Não informado";
                    profileData.skills = rs.getString("habilidades") != null ? rs.getString("habilidades") : "Não informado";
                }

            } catch (SQLException e) {
                errorMessage = e.getMessage();
                return null;
            } finally {
                conexao.closeResultSet(rs);
                conexao.closeStatement(ps);
                conexao.closeConnection(conn);
            }

            return profileData;
        }

        @Override
        protected void onPostExecute(ProfileData profileData) {
            if (profileData != null) {
                // Preenche os dados na tela
                tvName.setText(profileData.name);
                tvEmail.setText(profileData.email);
                tvBirthdate.setText("Nascimento: " + profileData.birthdate);
                tvEducation.setText("Formação: " + profileData.education);
                tvExperience.setText("Experiência: " + profileData.experience);
                tvInterests.setText("Interesses: " + profileData.interests);
                tvProjects.setText("Projetos: " + profileData.projects);
                tvSkills.setText("Habilidades: " + profileData.skills);

                if (profileData.profileImage != null) {
                    imgProfile.setImageBitmap(profileData.profileImage);
                } else {
                    imgProfile.setImageResource(R.drawable.ic_default_profile);
                }
            } else {
                Toast.makeText(ProfileActivity.this, "Erro ao carregar perfil: " + errorMessage, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // Classe auxiliar para armazenar os dados do perfil
    private static class ProfileData {
        String name;
        String email;
        String birthdate;
        String education;
        String experience;
        String interests;
        String projects;
        String skills;
        Bitmap profileImage;
    }
}