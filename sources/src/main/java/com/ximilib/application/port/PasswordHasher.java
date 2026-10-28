package com.ximilib.application.port;

/**
 * PasswordHasher: Port trừu tượng hóa cơ chế băm, so khớp mật khẩu và mã hóa an toàn (R-08, UC-1, UC-2).
 * Phục vụ Clean Architecture: Tầng Application định nghĩa port, tầng Infrastructure triển khai chi tiết kỹ thuật (BCrypt, AES).
 */
public interface PasswordHasher {

    /**
     * Băm mật khẩu thuần thành chuỗi băm BCrypt an toàn.
     *
     * @param rawPassword mật khẩu thô
     * @return chuỗi đã băm BCrypt
     */
    String hash(String rawPassword);

    /**
     * So khớp mật khẩu thô với chuỗi băm lưu trữ trong CSDL.
     *
     * @param rawPassword    mật khẩu thô do người dùng nhập
     * @param hashedPassword chuỗi băm BCrypt lưu trong CSDL
     * @return true nếu mật khẩu chính xác; false nếu sai hoặc chuỗi băm không hợp lệ
     */
    boolean matches(String rawPassword, String hashedPassword);

    /**
     * Mã hóa dữ liệu nhạy cảm (như CCCD) bằng thuật toán mã hóa đối xứng (C4).
     *
     * @param plainText chuỗi thuần cần mã hóa
     * @return chuỗi đã mã hóa
     */
    String encrypt(String plainText);
}
