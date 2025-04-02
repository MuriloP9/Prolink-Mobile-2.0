package com.example.prolink.Activity;

public class Usuario {
    private int id;
    private String nome;
    private String email;
    private String fotoPerfil; // Adicionado campo para foto

    public Usuario(int id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    // Construtor com foto
    public Usuario(int id, String nome, String email, String fotoPerfil) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.fotoPerfil = fotoPerfil;
    }

    // Getters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getFotoPerfil() { return fotoPerfil; } // Getter para foto
}