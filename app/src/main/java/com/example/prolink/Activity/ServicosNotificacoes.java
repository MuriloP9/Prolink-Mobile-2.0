package com.example.prolink.Activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.prolink.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Timer;
import java.util.TimerTask;

public class ServicosNotificacoes extends Service {

    private static final String CHANNEL_ID = "ProLink_Channel";
    private static final int NOTIFICATION_ID = 1;
    private Timer timer;
    private ClasseConexao conexao;
    private int idUsuarioLogado;

    @Override
    public void onCreate() {
        super.onCreate();
        conexao = new ClasseConexao(this);

        // Obter ID do usuário logado
        SharedPreferences prefs = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
        idUsuarioLogado = prefs.getInt("ID_USUARIO", 0);

        criarCanalNotificacao();
        iniciarVerificacaoMensagens();
    }

    private void criarCanalNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence nome = "ProLink Notificações";
            String descricao = "Notificações de novas mensagens";
            int importancia = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel canal = new NotificationChannel(CHANNEL_ID, nome, importancia);
            canal.setDescription(descricao);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }
    }

    private void iniciarVerificacaoMensagens() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                verificarNovasMensagens();
            }
        }, 0, 30000); // Verifica a cada 30 segundos
    }

    private void verificarNovasMensagens() {
        try {
            Connection conn = conexao.getConnection();
            if (conn != null && idUsuarioLogado > 0) {
                // Verifica se existem mensagens não lidas
                String query = "SELECT m.id_mensagem, m.id_remetente, u.nome, m.texto, " +
                        "(SELECT COUNT(*) FROM Contatos WHERE id_usuario = ? AND id_contato = m.id_remetente AND bloqueado = 1) as is_blocked " +
                        "FROM Mensagem m " +
                        "INNER JOIN Usuario u ON m.id_remetente = u.id_usuario " +
                        "WHERE m.id_destinatario = ? AND m.lida = 0 " +
                        "ORDER BY m.data_hora DESC LIMIT 1";

                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, idUsuarioLogado);
                pstmt.setInt(2, idUsuarioLogado);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    int idMensagem = rs.getInt("id_mensagem");
                    int idRemetente = rs.getInt("id_remetente");
                    String nomeRemetente = rs.getString("nome");
                    String texto = rs.getString("texto");
                    boolean bloqueado = rs.getInt("is_blocked") > 0;

                    // Não notificar mensagens de usuários bloqueados
                    if (!bloqueado) {
                        criarNotificacao(idMensagem, idRemetente, nomeRemetente, texto);
                    }
                }

                rs.close();
                pstmt.close();
                conn.close();
            }
        } catch (Exception e) {
            Log.e("NOTIFICACAO_ERRO", "Erro ao verificar mensagens: " + e.getMessage());
        }
    }

    private void criarNotificacao(int idMensagem, int idRemetente, String nomeRemetente, String textoMensagem) {
        // Intent para abrir a Activity de notificações quando clicar
        Intent intent = new Intent(this, NotificacoesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Construir notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_lock)
                .setContentTitle("Nova mensagem de " + nomeRemetente)
                .setContentText(textoMensagem)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Exibir notificação
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Serviço será reiniciado se for encerrado pelo sistema
    }
}