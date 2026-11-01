package com.ximilib.infrastructure.persistence.connection;

import com.ximilib.infrastructure.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DatabaseConnectionManager: Quản lý kết nối JDBC tập trung qua HikariCP Connection Pool (R-01, R-07, R-08).
 * - Sử dụng HikariDataSource với cơ chế khởi tạo Lazy Thread-Safe (Double-Checked Locking).
 * - Thiết lập đầy đủ: poolName, maximumPoolSize, minimumIdle, maxLifetime, leakDetectionThreshold, shutdown hook.
 * - Bọc ngoại lệ SQLException khi kết nối thành thông báo thân thiện, không lộ thông tin kỹ thuật nhạy cảm.
 * - Giữ nguyên API getConnection() cũ để các Repository không bị ảnh hưởng.
 */
public class DatabaseConnectionManager implements AutoCloseable {

    private static volatile HikariDataSource sharedDataSource;
    private static final Object LOCK = new Object();
    private static volatile boolean shutdownHookRegistered = false;

    private final boolean isCustom;
    private final String customUrl;
    private final String customUser;
    private final String customPassword;
    private volatile HikariDataSource customDataSource;

    /**
     * Khởi tạo mặc định: Sử dụng cấu hình nạp từ biến môi trường hoặc config/db.properties.
     * Chia sẻ chung một pool kết nối HikariDataSource singleton cho toàn bộ ứng dụng.
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
     * Lấy kết nối JDBC từ Connection Pool.
     * Nếu gặp lỗi kết nối (timeout, link failure, sai thông tin), ngoại lệ được bọc thành
     * thông báo tiếng Việt thân thiện, che giấu chi tiết nhạy cảm.
     */
    public Connection getConnection() throws SQLException {
        try {
            HikariDataSource ds = getDataSource();
            return ds.getConnection();
        } catch (SQLException e) {
            throw wrapConnectionException(e);
        } catch (Exception e) {
            throw wrapConnectionException(e);
        }
    }

    /**
     * Lấy DataSource quản lý pool kết nối (hỗ trợ kiểm tra trạng thái pool hoặc đóng kết nối).
     */
    public HikariDataSource getDataSource() {
        if (isCustom) {
            if (customDataSource == null || customDataSource.isClosed()) {
                synchronized (this) {
                    if (customDataSource == null || customDataSource.isClosed()) {
                        customDataSource = createCustomDataSource(customUrl, customUser, customPassword);
                    }
                }
            }
            return customDataSource;
        }
        return getSharedDataSource();
    }

    public static HikariDataSource getSharedDataSource() {
        if (sharedDataSource == null || sharedDataSource.isClosed()) {
            synchronized (LOCK) {
                if (sharedDataSource == null || sharedDataSource.isClosed()) {
                    DatabaseConfig config = DatabaseConfig.getInstance();
                    sharedDataSource = createDataSource(config);
                    registerShutdownHook(sharedDataSource);
                }
            }
        }
        return sharedDataSource;
    }

    private static HikariDataSource createDataSource(DatabaseConfig dbConfig) {
        HikariConfig config = new HikariConfig();
        config.setPoolName(dbConfig.getPoolName());
        config.setJdbcUrl(dbConfig.getJdbcUrl());
        config.setUsername(dbConfig.getUser());
        config.setPassword(dbConfig.getPassword());

        config.setMaximumPoolSize(dbConfig.getMaxPoolSize());
        config.setMinimumIdle(dbConfig.getMinIdle());
        config.setConnectionTimeout(dbConfig.getConnectionTimeout());
        config.setIdleTimeout(dbConfig.getIdleTimeout());
        config.setMaxLifetime(dbConfig.getMaxLifetime());
        config.setLeakDetectionThreshold(dbConfig.getLeakDetectionThreshold());

        // Tối ưu hiệu năng MySQL Driver
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        return new HikariDataSource(config);
    }

    private static HikariDataSource createCustomDataSource(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("XimiLib-CustomPool-" + (System.currentTimeMillis() % 10000));
        config.setJdbcUrl(url);
        config.setUsername(user != null ? user : "");
        config.setPassword(password != null ? password : "");
        config.setMaximumPoolSize(2);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(1000L);
        config.setInitializationFailTimeout(1000L);
        return new HikariDataSource(config);
    }

    private static void registerShutdownHook(HikariDataSource ds) {
        if (!shutdownHookRegistered) {
            synchronized (LOCK) {
                if (!shutdownHookRegistered) {
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        shutdownSharedPool();
                    }, "XimiLib-HikariShutdownHook"));
                    shutdownHookRegistered = true;
                }
            }
        }
    }

    public static void shutdownSharedPool() {
        synchronized (LOCK) {
            if (sharedDataSource != null && !sharedDataSource.isClosed()) {
                sharedDataSource.close();
                sharedDataSource = null;
            }
        }
    }

    @Override
    public void close() {
        if (isCustom && customDataSource != null && !customDataSource.isClosed()) {
            customDataSource.close();
            customDataSource = null;
        }
    }

    /**
     * Đóng pool kết nối (tương đương {@link #close()}).
     * Dùng cho Integration Test khi cần đóng manager sau mỗi test class.
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

    private SQLException wrapConnectionException(Exception e) {
        StringBuilder msgBuilder = new StringBuilder(e.getMessage() != null ? e.getMessage().toLowerCase() : "");
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause.getMessage() != null) {
                msgBuilder.append(" ").append(cause.getMessage().toLowerCase());
            }
            cause = cause.getCause();
        }
        String originalMsg = msgBuilder.toString();
        String friendly;
        if (originalMsg.contains("communications link failure")
                || originalMsg.contains("connection refused")
                || originalMsg.contains("connectexception")
                || originalMsg.contains("connection is not available")
                || originalMsg.contains("poolinitializationexception")
                || originalMsg.contains("timed out")) {
            friendly = "Không thể kết nối đến máy chủ CSDL MySQL. Vui lòng kiểm tra lại dịch vụ MySQL và cấu hình mạng.";
        } else if (originalMsg.contains("access denied for user")) {
            friendly = "Xác thực cơ sở dữ liệu thất bại: Tên người dùng hoặc mật khẩu CSDL không chính xác.";
        } else if (originalMsg.contains("unknown database")) {
            friendly = "Không tìm thấy cơ sở dữ liệu trên máy chủ MySQL. Vui lòng kiểm tra lại cấu hình.";
        } else if (originalMsg.contains("too many connections")) {
            friendly = "Máy chủ CSDL quá tải: Số lượng kết nối vượt quá giới hạn của MySQL.";
        } else {
            friendly = "Lỗi kết nối cơ sở dữ liệu: Không thể khởi tạo phiên làm việc với MySQL.";
        }
        if (e instanceof SQLException sqlEx) {
            return new SQLException(friendly, sqlEx.getSQLState(), sqlEx.getErrorCode(), sqlEx);
        }
        return new SQLException(friendly, e);
    }
}
