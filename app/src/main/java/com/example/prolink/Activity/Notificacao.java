package com.example.prolink.Activity;

import java.sql.Timestamp;
import java.util.Locale;

public class Notificacao {
    private int idMensagem;
    private int idRemetente;
    private String nomeRemetente;
    private String texto;
    private Timestamp dataHora;
    private int unreadCount;
    private boolean lida;

    public Notificacao(int idMensagem, int idRemetente, String nomeRemetente,
                       String texto, Timestamp dataHora, boolean lida, int unreadCount) {
        this.idMensagem = idMensagem;
        this.idRemetente = idRemetente;
        this.nomeRemetente = nomeRemetente;
        this.texto = texto;
        this.dataHora = dataHora;
        this.lida = lida;
        this.unreadCount = unreadCount;
    }

    // Getters e Setters
    public int getIdMensagem() { return idMensagem; }
    public int getIdRemetente() { return idRemetente; }
    public String getNomeRemetente() { return nomeRemetente; }
    public String getTexto() { return texto; }
    public Timestamp getDataHora() { return dataHora; }
    public boolean isLida() { return lida; }
    public int getUnreadCount() { return unreadCount; }

    public void setLida(boolean lida) { this.lida = lida; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    // Método para formatar a data (usado no adapter)
    public String getDataFormatada() {
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(new java.util.Date(dataHora.getTime()));
    }
}