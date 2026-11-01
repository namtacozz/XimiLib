package com.ximilib.infrastructure.persistence.connection;
import java.sql.Connection;
import java.sql.SQLException;

/** Callback không trả giá trị, thực thi trong phạm vi một transaction JDBC. */
@FunctionalInterface
public interface VoidTransactionCallback {
    void execute(Connection conn) throws SQLException;
}
