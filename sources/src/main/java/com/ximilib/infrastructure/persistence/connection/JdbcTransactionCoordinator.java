package com.ximilib.infrastructure.persistence.connection;

import com.ximilib.application.port.TransactionCoordinator;
import com.ximilib.domain.repository.TransactionContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * JdbcTransactionCoordinator: Triển khai JDBC của TransactionCoordinator.
 * Quản lý vòng đời transaction (setAutoCommit(false), commit, rollback, khôi phục autoCommit(true)).
 */
public class JdbcTransactionCoordinator implements TransactionCoordinator {

    private final DatabaseConnectionManager connectionManager;

    public JdbcTransactionCoordinator(DatabaseConnectionManager connectionManager) {
        this.connectionManager = Objects.requireNonNull(connectionManager, "DatabaseConnectionManager không được null");
    }

    @Override
    public <T> T execute(Function<TransactionContext, T> action) {
        Connection conn = null;
        try {
            conn = connectionManager.getConnection();
            conn.setAutoCommit(false);
            JdbcTransactionContext context = new JdbcTransactionContext(conn);
            T result = action.apply(context);
            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    /* ignore rollback failure */
                }
            }
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException("Giao dịch CSDL thất bại, đã hoàn tác dữ liệu: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    /* ignore */
                }
                try {
                    conn.close();
                } catch (SQLException ex) {
                    /* ignore */
                }
            }
        }
    }

    @Override
    public void executeVoid(Consumer<TransactionContext> action) {
        execute(context -> {
            action.accept(context);
            return null;
        });
    }
}
