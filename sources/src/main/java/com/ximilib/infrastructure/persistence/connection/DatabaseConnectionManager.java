package com.ximilib.infrastructure.persistence.connection;

import com.ximilib.infrastructure.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnectionManager: Quản lý kết nối JDBC thuần (java.sql.DriverManager) tập trung (R-01, R-07, R-08).
 * - Sử dụng DriverManager.getConnection(url, user, password), không pool, không shutdown hook, không double-checked locking.
 * - Lựa chọn có chủ đích cho quy mô PBL3 (vài máy trạm, mỗi truy vấn mở và đóng một kết nối độc lập).
 * - Bọc ngoại lệ SQLException khi kết nối thành thông báo thân thiện, không lộ thông tin kỹ thuật nhạy cảm hay chuỗi kết nối.
 * - Giữ nguyên tên lớp và API public Connection getConnection() throws SQLException để Repository không phải sửa.
 */
public class DatabaseConnectionManager implements AutoCloseable {

    private final boolean isCustom;
    private final String customUrl;
    private final String customUser;
    private final String customPassword;

    /**
     * Khởi tạo mặc định: Sử dụng cấu hình nạp từ biến môi trường hoặc config/db.properties.
     */
    public DatabaseConnectionManager() {
        this.isCustom = false;
        this.customUrl = null;
        this.customUser = null;
        this.customPassword = null;
    }

    /**
     * Khởi tạo với thông số kết nối tùy biến (chủ yếu phục vụ kiểm thử tích hợp / mock).
     */
    public DatabaseConnectionManager(String url, String user, String password) {
        this.isCustom = true;
        this.customUrl = url;
        this.customUser = user;
        this.customPassword = password;
    }

    /**
     * Lấy kết nối JDBC thuần từ DriverManager.
     * Nếu gặp lỗi kết nối (timeout, link failure, sai thông tin), ngoại lệ được bọc thành
     * thông báo tiếng Việt thân thiện, che giấu chi tiết nhạy cảm.
     */
    public Connection getConnection() throws SQLException {
        String url;
        String user;
        String password;
        if (isCustom) {
            url = customUrl;
            user = customUser != null ? customUser : "";
            password = customPassword != null ? customPassword : "";
        } else {
            DatabaseConfig config = DatabaseConfig.getInstance();
            url = config.getJdbcUrl();
            user = config.getUser();
            password = config.getPassword();
        }

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw wrapConnectionException(e);
        }
    }

    @Override
    public void close() {
        // Pure JDBC không giữ pool kết nối, mỗi kết nối được đóng độc lập bởi caller
    }

    /**
     * Đóng tài nguyên (tương đương {@link #close()}).
     * Giữ nguyên để tương thích ngược với các Integration Tests.
     */
    public void shutdown() {
        close();
    }

    public String getUrl() {
        if (isCustom) return customUrl;
        return DatabaseConfig.getInstance().getJdbcUrl();
    }

    public String getUser() {
        if (isCustom) return customUser;
        return DatabaseConfig.getInstance().getUser();
    }

    public static SQLException wrapConnectionException(Exception e) {
        if (e instanceof SQLException sqlEx) {
            int errorCode = sqlEx.getErrorCode();
            String friendly;
            switch (errorCode) {
                case 1045 ->
                    friendly = "Xác thực cơ sở dữ liệu thất bại: Tên người dùng hoặc mật khẩu CSDL không chính xác.";
                case 1044 ->
                    friendly = "Tài khoản CSDL không có quyền truy cập cơ sở dữ liệu thư viện. Vui lòng cấp quyền (GRANT) cho tài khoản.";
                case 1049 ->
                    friendly = "Không tìm thấy cơ sở dữ liệu trên máy chủ MySQL. Vui lòng kiểm tra lại cấu hình tên CSDL hoặc khởi tạo database.";
                default -> {
                    String msg = getExceptionMessage(sqlEx);
                    if (msg.contains("communications link failure")
                            || msg.contains("connection refused")
                            || msg.contains("connectexception")
                            || msg.contains("timed out")
                            || "08S01".equals(sqlEx.getSQLState())
                            || "08001".equals(sqlEx.getSQLState())) {
                        friendly = "Không thể kết nối đến máy chủ CSDL MySQL. Vui lòng kiểm tra lại dịch vụ MySQL (đã bật chưa) và cấu hình mạng.";
                    } else if (errorCode == 1040 || msg.contains("too many connections")) {
                        friendly = "Máy chủ CSDL quá tải: Số lượng kết nối vượt quá giới hạn của MySQL.";
                    } else {
                        friendly = "Lỗi kết nối cơ sở dữ liệu: Không thể khởi tạo phiên làm việc với MySQL.";
                    }
                }
            }
            return new SQLException(friendly, sqlEx.getSQLState(), sqlEx.getErrorCode(), sqlEx);
        }

        String msg = getExceptionMessage(e);
        String friendly;
        if (msg.contains("communications link failure")
                || msg.contains("connection refused")
                || msg.contains("connectexception")
                || msg.contains("timed out")) {
            friendly = "Không thể kết nối đến máy chủ CSDL MySQL. Vui lòng kiểm tra lại dịch vụ MySQL (đã bật chưa) và cấu hình mạng.";
        } else {
            friendly = "Lỗi kết nối cơ sở dữ liệu: Không thể khởi tạo phiên làm việc với MySQL.";
        }
        return new SQLException(friendly, e);
    }

    private static String getExceptionMessage(Throwable e) {
        StringBuilder msgBuilder = new StringBuilder(e.getMessage() != null ? e.getMessage().toLowerCase() : "");
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause.getMessage() != null) {
                msgBuilder.append(" ").append(cause.getMessage().toLowerCase());
            }
            cause = cause.getCause();
        }
        return msgBuilder.toString();
    }
}
