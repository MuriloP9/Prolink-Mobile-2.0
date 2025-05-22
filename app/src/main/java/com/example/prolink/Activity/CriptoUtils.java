package com.example.prolink.Activity;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class CriptoUtils {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    // Chave fixa para simplificação (em produção, use uma chave mais segura)
    private static final String SECRET_KEY = "ProLinkApp123456"; // 16 bytes para AES-128

    /**
     * Criptografa uma mensagem usando AES
     * @param mensagem Mensagem em texto plano
     * @return Mensagem criptografada em Base64
     */
    public static String criptografar(String mensagem) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] mensagemCriptografada = cipher.doFinal(mensagem.getBytes("UTF-8"));
            return Base64.encodeToString(mensagemCriptografada, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            // Em caso de erro, retorna a mensagem original
            return mensagem;
        }
    }

    /**
     * Descriptografa uma mensagem usando AES
     * @param mensagemCriptografada Mensagem criptografada em Base64
     * @return Mensagem em texto plano
     */
    public static String descriptografar(String mensagemCriptografada) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] mensagemBytes = Base64.decode(mensagemCriptografada, Base64.DEFAULT);
            byte[] mensagemDescriptografada = cipher.doFinal(mensagemBytes);
            return new String(mensagemDescriptografada, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            // Em caso de erro, retorna a mensagem original
            return mensagemCriptografada;
        }
    }

    /**
     * Verifica se uma string está criptografada (Base64 válido)
     * @param texto Texto a ser verificado
     * @return true se parece estar criptografado, false caso contrário
     */
    public static boolean estaCriptografado(String texto) {
        try {
            // Verifica se é um Base64 válido e tem características de texto criptografado
            byte[] decoded = Base64.decode(texto, Base64.DEFAULT);
            String reencoded = Base64.encodeToString(decoded, Base64.DEFAULT).trim();
            return reencoded.equals(texto.trim()) && texto.length() > 10;
        } catch (Exception e) {
            return false;
        }
    }
}