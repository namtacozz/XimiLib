package com.ximilib.infrastructure.security;

import com.ximilib.application.port.PasswordHasher;
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
 * - Kiểm tra độ mạnh mật khẩu và định dạng CCCD theo đúng bản Đặc tả SRS.
 */
public final class SecurityUtil implements PasswordHasher {

    private static final String DEFAULT_AES_KEY = "XimiLib_Library_Secret_Key_2026_Secure_AES";
    private static final Pattern PWD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$");
    private static final Pattern CCCD_PATTERN = Pattern.compile("^(\\d{9}|\\d{12})$");

    public SecurityUtil() {}

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
        } catch (Exception e) {
            return plainText; // Fallback an toàn nếu lỗi crypto
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
        } catch (Exception e) {
            return cipherText;
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
        String key = System.getenv("XIMILIB_AES_KEY");
        if (key == null || key.trim().isEmpty()) {
            key = DEFAULT_AES_KEY;
        }
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        keyBytes = sha.digest(keyBytes);
        keyBytes = Arrays.copyOf(keyBytes, 32); // 256 bit
        return new SecretKeySpec(keyBytes, "AES");
    }
}
