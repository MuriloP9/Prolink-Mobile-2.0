package com.example.prolink.Activity;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ClasseConexao {
    private static final String TAG = "ClasseConexao";
    private static final String DRIVER = "net.sourceforge.jtds.jdbc.Driver";
    private static final String URL = "jdbc:jtds:sqlserver://192.168.0.16:1433/prolink";
    private static final String USER = "sa";
    private static final String PASSWORD = "etesp";

    private Context context;

    public ClasseConexao(Context context) {
        this.context = context;
    }

    public Connection getConnection() {
        try {
            // 1. Carregar o driver JDBC
            Class.forName(DRIVER);

            // 2. Estabelecer a conexão
            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (ClassNotFoundException e) {
            logError("Driver JDBC não encontrado", e);
            showToast("Erro de configuração do aplicativo");
        } catch (SQLException e) {
            logError("Erro ao conectar ao banco de dados", e);
            showToast("Falha na conexão com o servidor");
        }
        return null;
    }

    public ResultSet executeQuery(String query) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            if (conn != null) {
                stmt = conn.createStatement();
                return stmt.executeQuery(query);
            }
        } catch (SQLException e) {
            logError("Erro ao executar consulta", e);
        }
        return null;
    }

    public int executeUpdate(String query) {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            if (conn != null) {
                stmt = conn.createStatement();
                return stmt.executeUpdate(query);
            }
        } catch (SQLException e) {
            logError("Erro ao executar atualização", e);
        }
        return -1;
    }

    public PreparedStatement prepareStatement(String sql) {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                return conn.prepareStatement(sql);
            }
        } catch (SQLException e) {
            logError("Erro ao preparar statement", e);
        }
        return null;
    }

    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logError("Erro ao fechar conexão", e);
            }
        }
    }

    public void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logError("Erro ao fechar statement", e);
            }
        }
    }

    public void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logError("Erro ao fechar result set", e);
            }
        }
    }

    // Adicione este método para manter compatibilidade
    public ResultSet executarConsulta(String query) throws SQLException {
        Connection conn = getConnection();
        if (conn != null) {
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(query);
        }
        return null;
    }

    private void logError(String message, Exception e) {
        Log.e(TAG, message + ": " + e.getMessage());
        e.printStackTrace();
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    // Método compatível com versão anterior (opcional)
    @Deprecated
    public Connection entBanco(Context ctx) {
        return getConnection();
    }
}