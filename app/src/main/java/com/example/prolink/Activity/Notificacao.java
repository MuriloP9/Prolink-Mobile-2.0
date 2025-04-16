package com.example.prolink.Activity;

import java.sql.Timestamp;

public class Notificacao {
    private int idMensagem;
    private int idRemetente;
    private String nomeRemetente;
    private String texto;
    private Timestamp dataHora;
    private boolean lida; // Novo campo para controlar se foi lida

    public Notificacao(int idMensagem, int idRemetente, String nomeRemetente,
                       String texto, Timestamp dataHora, boolean lida) {
        this.idMensagem = idMensagem;
        this.idRemetente = idRemetente;
        this.nomeRemetente = nomeRemetente;
        this.texto = texto;
        this.dataHora = dataHora;
        this.lida = lida;
    }

    // Getters e Setters
    public int getIdMensagem() { return idMensagem; }
    public int getIdRemetente() { return idRemetente; }
    public String getNomeRemetente() { return nomeRemetente; }
    public String getTexto() { return texto; }
    public Timestamp getDataHora() { return dataHora; }
    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
}