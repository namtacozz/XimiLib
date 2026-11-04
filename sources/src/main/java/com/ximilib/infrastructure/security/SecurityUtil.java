package com.ximilib.infrastructure.security;

import com.ximilib.application.port.PasswordHasher;
import com.ximilib.infrastructure.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;

public final class SecurityUtil implements PasswordHasher {

    private static final Pattern PWD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$");
    private static final Pattern CCCD_PATTERN = Pattern.compile("^(\\d{9}|\\d{12})$");
    private static volatile String testAesKey = null;
    private static volatile boolean forceMissingAesKey = false;

    public SecurityUtil() {}

    public static void setTestAesKey(String key) {
        testAesKey = key;
        forceMissingAesKey = false;
    }

    public static void setForceMissingAesKey(boolean force) {
        forceMissingAesKey = force;
    }

    public static void clearTestAesKey() {
        testAesKey = null;
        forceMissingAesKey = false;
    }

    @Override
    public String hash(String rawPassword) {
        return hashPassword(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return checkPassword(rawPassword, hashedPassword);
    }

    @Override
    public String encrypt(String plainText) {
        return encryptAes(plainText);
    }

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) return "";
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    public static String encryptAes(String plainText) {
        if (plainText == null || plainText.trim().isEmpty()) return plainText;
        try {
            SecretKeySpec secretKey = getAesKeySpec();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(plainText.trim().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa dữ liệu AES: " + e.getMessage(), e);
        }
    }

    public static String decryptAes(String cipherText) {
        if (cipherText == null || cipherText.trim().isEmpty()) return cipherText;
        try {
            SecretKeySpec secretKey = getAesKeySpec();
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText.trim()));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi giải mã dữ liệu AES: " + e.getMessage(), e);
        }
    }

    public static boolean validatePasswordStrength(String password) {
        if (password == null) return false;
        return PWD_PATTERN.matcher(password).matches();
    }

    public static boolean validateCccd(String cccd) {
        if (cccd == null) return false;
        return CCCD_PATTERN.matcher(cccd.trim()).matches();
    }

    private static SecretKeySpec getAesKeySpec() throws Exception {
        if (forceMissingAesKey) {
            throw new IllegalStateException("Thiếu cấu hình khóa mã hóa AES! Vui lòng thiết lập biến môi trường XIMILIB_AES_KEY hoặc khai báo 'security.aes.key' trong file config/db.properties.");
        }
        String key = testAesKey;
        if (key == null || key.trim().isEmpty()) {
            key = DatabaseConfig.getInstance().getAesKey();
        }
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalStateException("Thiếu cấu hình khóa mã hóa AES! Vui lòng thiết lập biến môi trường XIMILIB_AES_KEY hoặc khai báo 'security.aes.key' trong file config/db.properties.");
        }
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        keyBytes = sha.digest(keyBytes);
        keyBytes = Arrays.copyOf(keyBytes, 32);
        return new SecretKeySpec(keyBytes, "AES");
    }
}
