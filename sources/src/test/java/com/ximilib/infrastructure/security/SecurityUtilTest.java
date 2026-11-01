package com.ximilib.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Đơn Vị Tiện Ích Bảo Mật (SecurityUtil)")
class SecurityUtilTest {

    private static final String SAMPLE_TEST_KEY = "TestSecretKeyForAesEncryption2026";

    @BeforeEach
    void setUp() {
        SecurityUtil.clearTestAesKey();
    }

    @AfterEach
    void tearDown() {
        SecurityUtil.clearTestAesKey();
    }

    @Test
    @DisplayName("TC-SEC-01: Băm mật khẩu BCrypt và đối soát chính xác")
    void testHashAndCheckPassword() {
        String rawPassword = "Password@123";
        String hashed = SecurityUtil.hashPassword(rawPassword);

        assertNotNull(hashed);
        assertTrue(hashed.startsWith("$2a$") || hashed.startsWith("$2b$") || hashed.startsWith("$2y$"));
        assertTrue(SecurityUtil.checkPassword(rawPassword, hashed));
        assertFalse(SecurityUtil.checkPassword("WrongPassword@123", hashed));
        assertFalse(SecurityUtil.checkPassword(null, hashed));
        assertFalse(SecurityUtil.checkPassword(rawPassword, null));
    }

    @Test
    @DisplayName("TC-SEC-02: Mã hóa và giải mã AES-256 thành công khi có khóa hợp lệ")
    void testEncryptAndDecryptAesSuccessWithConfiguredKey() {
        SecurityUtil.setTestAesKey(SAMPLE_TEST_KEY);

        String rawCccd = "201234567890";
        String encrypted = SecurityUtil.encryptAes(rawCccd);

        assertNotNull(encrypted);
        assertNotEquals(rawCccd, encrypted);

        String decrypted = SecurityUtil.decryptAes(encrypted);
        assertEquals(rawCccd, decrypted);
    }

    @Test
    @DisplayName("TC-SEC-03: Ném lỗi tiếng Việt rõ ràng khi thiếu cấu hình khóa AES")
    void testEncryptAesThrowsWhenKeyMissing() {
        SecurityUtil.setForceMissingAesKey(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            SecurityUtil.encryptAes("201234567890");
        });

        assertTrue(ex.getMessage().contains("Thiếu cấu hình khóa mã hóa AES"));
    }

    @Test
    @DisplayName("TC-SEC-04: Kiểm tra độ mạnh mật khẩu theo SRS UC-2 (>= 8 ký tự, 1 hoa, 1 số, 1 đặc biệt)")
    void testValidatePasswordStrength() {
        assertTrue(SecurityUtil.validatePasswordStrength("Admin@12345"));
        assertTrue(SecurityUtil.validatePasswordStrength("XimiLib#2026"));

        // Thiếu ký tự đặc biệt
        assertFalse(SecurityUtil.validatePasswordStrength("Admin12345"));
        // Thiếu chữ hoa
        assertFalse(SecurityUtil.validatePasswordStrength("admin@12345"));
        // Thiếu số
        assertFalse(SecurityUtil.validatePasswordStrength("Admin@Password"));
        // Quá ngắn (< 8 ký tự)
        assertFalse(SecurityUtil.validatePasswordStrength("Adm@1"));
        // Null
        assertFalse(SecurityUtil.validatePasswordStrength(null));
    }

    @Test
    @DisplayName("TC-SEC-05: Kiểm tra định dạng CCCD theo SRS UC-2 (9 hoặc 12 chữ số)")
    void testValidateCccd() {
        assertTrue(SecurityUtil.validateCccd("123456789"));
        assertTrue(SecurityUtil.validateCccd("012345678901"));

        // Sai độ dài
        assertFalse(SecurityUtil.validateCccd("12345678"));
        assertFalse(SecurityUtil.validateCccd("1234567890"));
        assertFalse(SecurityUtil.validateCccd("0123456789012"));
        // Chứa chữ
        assertFalse(SecurityUtil.validateCccd("12345678A"));
        // Null
        assertFalse(SecurityUtil.validateCccd(null));
    }
}
