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
}
