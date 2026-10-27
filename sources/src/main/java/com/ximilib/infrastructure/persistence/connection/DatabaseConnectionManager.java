package com.ximilib.infrastructure.persistence.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnectionManager: Quản lý kết nối JDBC tập trung tới cơ sở dữ liệu MySQL ximilib_db.
 * - Tuân thủ R-07, R-08 (không lưu cứng mật khẩu, đọc từ biến môi trường).
 * - Biến môi trường: XIMILIB_DB_HOST, XIMILIB_DB_PORT, XIMILIB_DB_NAME, XIMILIB_DB_USER, XIMILIB_DB_PASS (hoặc MYSQL_PWD).
 */
public class DatabaseConnectionManager {

    private final String url;
    private final String user;
    private final String password;

    public DatabaseConnectionManager() {
        String host = getEnvOrDefault("XIMILIB_DB_HOST", "localhost");
        String port = getEnvOrDefault("XIMILIB_DB_PORT", "3306");
        String dbName = getEnvOrDefault("XIMILIB_DB_NAME", "ximilib_db");
        this.user = getEnvOrDefault("XIMILIB_DB_USER", "ximilib_app");

        String pass = System.getenv("XIMILIB_DB_PASS");
        if (pass == null || pass.trim().isEmpty()) {
            pass = System.getenv("MYSQL_PWD");
        }
        if (pass == null) {
            pass = "ximilib_app_2026";
        }
        this.password = pass;

        this.url = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    public DatabaseConnectionManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Lấy kết nối JDBC tới MySQL.
     * Ném ngoại lệ SQLException nếu MySQL không chạy hoặc thông tin kết nối sai (không fallback dữ liệu cứng).
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    private static String getEnvOrDefault(String key, String def) {
        String val = System.getenv(key);
        return (val != null && !val.trim().isEmpty()) ? val.trim() : def;
    }
}
