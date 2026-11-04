package com.ximilib.infrastructure.persistence.connection;
import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface VoidTransactionCallback {
    void execute(Connection conn) throws SQLException;
}
