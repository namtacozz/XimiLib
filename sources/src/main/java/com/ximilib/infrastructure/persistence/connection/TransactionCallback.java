package com.ximilib.infrastructure.persistence.connection;
import java.sql.Connection;
import java.sql.SQLException;

/** Callback thực thi trong phạm vi một transaction JDBC đã mở. */
@FunctionalInterface
public interface TransactionCallback<T> {
    T execute(Connection conn) throws SQLException;
}
