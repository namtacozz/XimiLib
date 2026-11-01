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

/**
 * SecurityUtil: Tiện ích bảo mật chuẩn hóa toàn hệ thống (R-08, UC-1, UC-2).
 * - Triển khai PasswordHasher port cho tầng Application (Pure DI, DIP).
 * - Băm mật khẩu một chiều bằng BCrypt.
 * - Mã hóa hai chiều đối xứng AES-256 cho trường thông tin định danh CCCD.
 * - Đọc khóa mã hóa AES từ cấu hình (DatabaseConfig), không lưu cứng trong mã nguồn.
 * - Kiểm tra độ mạnh mật khẩu và định dạng CCCD theo đúng bản Đặc tả SRS.
 */
public final class SecurityUtil implements PasswordHasher {

    private static final Pattern PWD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$");
    private static final Pattern CCCD_PATTERN = Pattern.compile("^(\\d{9}|\\d{12})$");
    private static volatile String testAesKey = null;
    private static volatile boolean forceMissingAesKey = false;

    public SecurityUtil() {}

    /**
     * Thiết lập khóa AES giả lập cho môi trường kiểm thử (Unit Tests).
     */
    public static void setTestAesKey(String key) {
        testAesKey = key;
        forceMissingAesKey = false;
    }

    /**
     * Giả lập môi trường thiếu cấu hình khóa AES cho kiểm thử.
     */
    public static void setForceMissingAesKey(boolean force) {
        forceMissingAesKey = force;
    }

    /**
     * Xóa khóa AES giả lập sau khi kết thúc kiểm thử.
     */
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

    /**
     * Băm mật khẩu bằng thuật toán BCrypt với salt ngẫu nhiên.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) return "";
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /**
     * So khớp mật khẩu người dùng nhập với chuỗi băm trong CSDL.
     */
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

    /**
     * Mã hóa thông tin nhạy cảm (CCCD) bằng AES-256 (C4).
     */
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

    /**
     * Giải mã dữ liệu nhạy cảm AES-256.
     */
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

    /**
     * Kiểm tra mật khẩu: ≥ 8 ký tự, ≥ 1 chữ hoa, ≥ 1 chữ số, ≥ 1 ký tự đặc biệt (SRS UC-2).
     */
    public static boolean validatePasswordStrength(String password) {
        if (password == null) return false;
        return PWD_PATTERN.matcher(password).matches();
    }

    /**
     * Kiểm tra số CMND/CCCD: đúng 9 hoặc 12 chữ số (SRS UC-2).
     */
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
        keyBytes = Arrays.copyOf(keyBytes, 32); // 256 bit
        return new SecretKeySpec(keyBytes, "AES");
    }
}
