package com.ximilib.infrastructure.persistence.connection;

import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionManager {

    private final DatabaseConnectionManager connectionManager;

    public TransactionManager(DatabaseConnectionManager connectionManager) {
        this.connectionManager = java.util.Objects.requireNonNull(connectionManager,
                "DatabaseConnectionManager không được null");
    }

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
                try { conn.rollback(); } catch (SQLException ex) {   }
            }
            throw new RuntimeException("Giao dịch CSDL thất bại, đã hoàn tác dữ liệu: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) {   }
                try { conn.close(); } catch (SQLException ex) {   }
            }
        }
    }

    public void execute(VoidTransactionCallback callback) {
        execute(conn -> { callback.execute(conn); return null; });
    }
}
