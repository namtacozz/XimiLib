package com.ximilib.infrastructure.persistence;

import com.ximilib.infrastructure.config.DatabaseConfig;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Đơn Vị Quản Lý Kết Nối & Cấu Hình CSDL (DatabaseConnectionManager)")
class DatabaseConnectionManagerTest {

    @Test
    @DisplayName("TC-DB-01: Báo lỗi tiếng Việt rõ ràng khi thiếu cấu hình tài khoản hoặc mật khẩu CSDL")
    void testMissingCredentialsThrowsDescriptiveException() {
        DatabaseConfig configWithoutUser = new DatabaseConfig.Builder()
                .host("localhost")
                .port("3306")
                .dbName("ximilib_db")
                .user(null)
                .password("some_pass")
                .build();

        IllegalStateException exUser = assertThrows(IllegalStateException.class, configWithoutUser::getUser);
        assertTrue(exUser.getMessage().contains("Thiếu cấu hình tài khoản kết nối CSDL"));

        DatabaseConfig configWithoutPass = new DatabaseConfig.Builder()
                .host("localhost")
                .port("3306")
                .dbName("ximilib_db")
                .user("ximilib_app")
                .password(null)
                .build();

        IllegalStateException exPass = assertThrows(IllegalStateException.class, configWithoutPass::getPassword);
        assertTrue(exPass.getMessage().contains("Thiếu cấu hình mật khẩu kết nối CSDL"));
    }

    @Test
    @DisplayName("TC-DB-02: Bọc lỗi SQLException thành thông báo thân thiện người dùng khi kết nối thất bại")
    void testWrapConnectionExceptionFriendliness() {
        DatabaseConnectionManager brokenManager = new DatabaseConnectionManager(
                "jdbc:mysql://localhost:9999/non_existent_db?connectTimeout=500",
                "invalid_user",
                "invalid_pass"
        );

        SQLException ex = assertThrows(SQLException.class, brokenManager::getConnection);
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("Không thể kết nối đến máy chủ CSDL MySQL")
                || ex.getMessage().contains("Lỗi kết nối cơ sở dữ liệu"),
                "Thông báo ngoại lệ phải được dịch sang tiếng Việt thân thiện: " + ex.getMessage());
        brokenManager.close();
    }

    @Test
    @DisplayName("TC-DB-03: Báo lỗi tiếng Việt rõ ràng khi giá trị cấu hình vẫn là CHANGE_ME")
    void testChangeMePlaceholderThrowsDescriptiveException() {
        DatabaseConfig configChangeMeUser = new DatabaseConfig.Builder()
                .user("CHANGE_ME")
                .password("valid_pass")
                .aesKey("valid_aes_key")
                .build();
        IllegalStateException exUser = assertThrows(IllegalStateException.class, configChangeMeUser::getUser);
        assertTrue(exUser.getMessage().contains("CHANGE_ME"), "Phải báo rõ CHANGE_ME chưa được thay đổi: " + exUser.getMessage());

        DatabaseConfig configChangeMePass = new DatabaseConfig.Builder()
                .user("ximilib_app")
                .password("CHANGE_ME")
                .aesKey("valid_aes_key")
                .build();
        IllegalStateException exPass = assertThrows(IllegalStateException.class, configChangeMePass::getPassword);
        assertTrue(exPass.getMessage().contains("CHANGE_ME"), "Phải báo rõ CHANGE_ME chưa được thay đổi: " + exPass.getMessage());

        DatabaseConfig configChangeMeAes = new DatabaseConfig.Builder()
                .user("ximilib_app")
                .password("valid_pass")
                .aesKey("CHANGE_ME")
                .build();
        IllegalStateException exAes = assertThrows(IllegalStateException.class, configChangeMeAes::getAesKey);
        assertTrue(exAes.getMessage().contains("CHANGE_ME"), "Phải báo rõ CHANGE_ME chưa được thay đổi: " + exAes.getMessage());
    }

    @Test
    @DisplayName("TC-DB-04: Tự động trim() mật khẩu khi nạp từ file properties")
    void testPasswordTrimmingFromFileProps() throws Exception {
        java.io.File tempFile = java.io.File.createTempFile("db-test-trim-", ".properties");
        try {
            java.nio.file.Files.writeString(tempFile.toPath(), "db.user=ximilib_app\ndb.password=  my_secret_pass  \nsecurity.aes.key=12345678901234567890123456789012\n");
            System.setProperty("ximilib.config.path", tempFile.getAbsolutePath());
            DatabaseConfig.resetInstance();
            DatabaseConfig loaded = DatabaseConfig.load();
            assertEquals("my_secret_pass", loaded.getPassword(), "Mật khẩu đọc từ file phải được trim() khoảng trắng đầu/cuối");
        } finally {
            System.clearProperty("ximilib.config.path");
            DatabaseConfig.resetInstance();
            tempFile.delete();
        }
    }

    @Test
    @DisplayName("TC-DB-05: Phân biệt chính xác mã lỗi 1045, 1044, 1049 và lỗi dịch vụ MySQL chưa chạy")
    void testWrapConnectionExceptionErrorCodes() {

        SQLException ex1045 = new SQLException("Access denied for user 'app'@'localhost'", "28000", 1045);
        SQLException wrapped1045 = DatabaseConnectionManager.wrapConnectionException(ex1045);
        assertTrue(wrapped1045.getMessage().contains("Tên người dùng hoặc mật khẩu CSDL không chính xác"),
                "Mã lỗi 1045 phải báo sai user/mật khẩu: " + wrapped1045.getMessage());

        SQLException ex1044 = new SQLException("Access denied for user 'app'@'localhost' to database 'ximilib_db'", "42000", 1044);
        SQLException wrapped1044 = DatabaseConnectionManager.wrapConnectionException(ex1044);
        assertTrue(wrapped1044.getMessage().contains("không có quyền truy cập cơ sở dữ liệu"),
                "Mã lỗi 1044 phải báo thiếu quyền truy cập CSDL: " + wrapped1044.getMessage());

        SQLException ex1049 = new SQLException("Unknown database 'ximilib_db'", "42000", 1049);
        SQLException wrapped1049 = DatabaseConnectionManager.wrapConnectionException(ex1049);
        assertTrue(wrapped1049.getMessage().contains("Không tìm thấy cơ sở dữ liệu"),
                "Mã lỗi 1049 phải báo database không tồn tại: " + wrapped1049.getMessage());

        SQLException exConnRefused = new SQLException("Communications link failure: Connection refused", "08S01", 0);
        SQLException wrappedConn = DatabaseConnectionManager.wrapConnectionException(exConnRefused);
        assertTrue(wrappedConn.getMessage().contains("Không thể kết nối đến máy chủ CSDL MySQL"),
                "Lỗi kết nối phải báo MySQL chưa chạy hoặc lỗi mạng: " + wrappedConn.getMessage());
    }
}
