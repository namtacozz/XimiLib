package com.ximilib.infrastructure.persistence.connection;

import com.ximilib.domain.repository.TransactionContext;
import java.sql.Connection;
import java.util.Objects;

public class JdbcTransactionContext implements TransactionContext {

    private final Connection connection;

    public JdbcTransactionContext(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "Connection không được null");
    }

    public Connection getConnection() {
        return connection;
    }
}
