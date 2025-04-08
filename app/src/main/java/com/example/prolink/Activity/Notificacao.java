package com.example.prolink.Activity;

import java.sql.Timestamp;

public class Notificacao {
    private int idMensagem;
    private int idRemetente;
    private String nomeRemetente;
    private String texto;
    private Timestamp dataHora;

    public Notificacao(int idMensagem, int idRemetente, String nomeRemetente, String texto, Timestamp dataHora) {
        this.idMensagem = idMensagem;
        this.idRemetente = idRemetente;
        this.nomeRemetente = nomeRemetente;
        this.texto = texto;
        this.dataHora = dataHora;
    }

    // Getters
    public int getIdMensagem() { return idMensagem; }
    public int getIdRemetente() { return idRemetente; }
    public String getNomeRemetente() { return nomeRemetente; }
    public String getTexto() { return texto; }
    public Timestamp getDataHora() { return dataHora; }
}