package com.example.prolink.Activity;

import java.sql.Timestamp;

public class Mensagem {
    private int id;
    private int idRemetente;
    private int idDestinatario;
    private String texto;
    private Timestamp dataHora;

    public Mensagem(int id, int idRemetente, int idDestinatario, String texto, Timestamp dataHora) {
        this.id = id;
        this.idRemetente = idRemetente;
        this.idDestinatario = idDestinatario;
        this.texto = texto;
        this.dataHora = dataHora;
    }

    // Getters e Setters
    public int getId() { return id; }
    public int getIdRemetente() { return idRemetente; }
    public int getIdDestinatario() { return idDestinatario; }
    public String getTexto() { return texto; }
    public Timestamp getDataHora() { return dataHora; }

    public String getDataHoraFormatada() {
        return new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(dataHora.getTime()));
    }
}