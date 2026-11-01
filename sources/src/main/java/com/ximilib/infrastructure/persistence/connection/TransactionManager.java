package com.ximilib.infrastructure.persistence.connection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * TransactionManager: Quản lý vòng đời Transaction JDBC tái sử dụng nội bộ tầng infrastructure.
 * Application Service KHÔNG gọi class này trực tiếp — tầng repository tự quản lý transaction.
 * Luôn setAutoCommit(false), commit khi thành công, rollback khi lỗi, setAutoCommit(true) trong finally.
 */
public final class TransactionManager {

    private final DatabaseConnectionManager connectionManager;

    public TransactionManager(DatabaseConnectionManager connectionManager) {
        this.connectionManager = java.util.Objects.requireNonNull(connectionManager,
                "DatabaseConnectionManager không được null");
    }

    /**
     * Thực thi callback trong một transaction, trả về giá trị T.
     * Tự động rollback và ném RuntimeException (message tiếng Việt) khi lỗi.
     */
    public <T> T execute(TransactionCallback<T> callback) {
        Connection conn = null;
        try {
            conn = connectionManager.getConnection();
            conn.setAutoCommit(false);
            T result = callback.execute(conn);
            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* bỏ qua lỗi rollback */ }
            }
            throw new RuntimeException("Giao dịch CSDL thất bại, đã hoàn tác dữ liệu: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* bỏ qua */ }
                try { conn.close(); } catch (SQLException ex) { /* bỏ qua */ }
            }
        }
    }

    /**
     * Thực thi callback void trong một transaction.
     * Tự động rollback và ném RuntimeException (message tiếng Việt) khi lỗi.
     */
    public void execute(VoidTransactionCallback callback) {
        execute(conn -> { callback.execute(conn); return null; });
    }
}
