package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.domain.repository.TransactionContext;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import com.ximilib.infrastructure.persistence.connection.JdbcTransactionContext;
import com.ximilib.infrastructure.persistence.connection.JdbcTransactionCoordinator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lớp cơ sở trừu tượng cho tất cả JDBC Repositories.
 * Áp dụng Kế thừa (Inheritance) & Đa hình (Polymorphism) để tái sử dụng tối đa
 * các thao tác truy vấn dữ liệu, đóng gói Connection/PreparedStatement và Transaction.
 */
public abstract class BaseJdbcRepository {

    protected final DatabaseConnectionManager connectionManager;

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    public interface SqlConsumer {
        void accept(Connection conn) throws SQLException;
    }

    @FunctionalInterface
    public interface SqlFunction<R> {
        R apply(Connection conn) throws SQLException;
    }

    protected BaseJdbcRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = (connectionManager != null) ? connectionManager : new DatabaseConnectionManager();
    }

    protected Connection getConnection() throws SQLException {
        return connectionManager.getConnection();
    }

    protected void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
    }

    protected int queryInt(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    protected boolean queryExists(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Object val = rs.getObject(1);
                    if (val instanceof Number n) {
                        return n.intValue() > 0;
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected Optional<String> queryString(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    protected <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) {
        List<T> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    protected <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(mapper.map(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    protected List<Object[]> queryObjectRows(String sql, int colCount, Object... params) {
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[colCount];
                    for (int i = 0; i < colCount; i++) {
                        row[i] = rs.getObject(i + 1);
                    }
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    protected int executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setParameters(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi thực thi SQL: " + e.getMessage(), e);
        }
    }

    protected int executeUpdate(TransactionContext tx, String sql, Object... params) {
        if (tx instanceof JdbcTransactionContext jtx) {
            try {
                PreparedStatement ps = jtx.getConnection().prepareStatement(sql);
                setParameters(ps, params);
                return ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Lỗi thực thi SQL trong Transaction: " + e.getMessage(), e);
            }
        }
        return executeUpdate(sql, params);
    }

    protected void runInTransaction(TransactionContext tx, SqlConsumer action) {
        if (tx instanceof JdbcTransactionContext jtx) {
            try {
                action.accept(jtx.getConnection());
            } catch (SQLException e) {
                throw new RuntimeException("Lỗi giao dịch: " + e.getMessage(), e);
            }
            return;
        }

        JdbcTransactionCoordinator coordinator = new JdbcTransactionCoordinator(connectionManager);
        coordinator.executeVoid(context -> {
            try {
                action.accept(((JdbcTransactionContext) context).getConnection());
            } catch (SQLException e) {
                throw new RuntimeException("Lỗi thực thi giao dịch, đã rollback: " + e.getMessage(), e);
            }
        });
    }
}
