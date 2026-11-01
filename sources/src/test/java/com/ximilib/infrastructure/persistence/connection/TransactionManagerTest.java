package com.ximilib.infrastructure.persistence.connection;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TransactionManagerTest: Unit test cho TransactionManager, không cần MySQL thật.
 * Sử dụng java.lang.reflect.Proxy để fake Connection và anonymous class để fake DatabaseConnectionManager.
 */
class TransactionManagerTest {

    // -------------------------------------------------------------------------
    // Helpers: fake Connection và fake DatabaseConnectionManager
    // -------------------------------------------------------------------------

    /** Tạo một Connection Proxy ghi lại các lệnh gọi setAutoCommit / commit / rollback / close. */
    private static Connection fakeConnection(
            AtomicBoolean committed,
            AtomicBoolean rolledBack,
            AtomicBoolean closed,
            AtomicInteger autoCommitSetCount,
            boolean throwOnExecute
    ) {
        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "setAutoCommit" -> autoCommitSetCount.incrementAndGet();
                case "commit" -> committed.set(true);
                case "rollback" -> rolledBack.set(true);
                case "close" -> closed.set(true);
                case "isClosed" -> { return closed.get(); }
                default -> {
                    // mọi method khác: không làm gì, trả về default
                }
            }
            // Nếu được yêu cầu throw khi callback gọi bất kỳ method nào khác (không phải lifecycle)
            if (throwOnExecute && !java.util.Set.of(
                    "setAutoCommit", "commit", "rollback", "close", "isClosed").contains(method.getName())) {
                throw new SQLException("Lỗi giả lập từ Connection");
            }
            Class<?> returnType = method.getReturnType();
            if (returnType == boolean.class) return false;
            if (returnType == int.class) return 0;
            return null;
        };
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                handler);
    }

    /** Fake DatabaseConnectionManager trả về Connection đã chuẩn bị. */
    private static DatabaseConnectionManager fakeManager(Connection conn) {
        return new DatabaseConnectionManager("jdbc:fake://localhost/fake", "user", "pass") {
            @Override
            public Connection getConnection() {
                return conn;
            }
        };
    }

    // -------------------------------------------------------------------------
    // Test cases
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("TM-01: execute(callback) trả về giá trị đúng, commit được gọi, rollback KHÔNG được gọi")
    void execute_returnsCorrectValue_andCommits() throws Exception {
        AtomicBoolean committed = new AtomicBoolean(false);
        AtomicBoolean rolledBack = new AtomicBoolean(false);
        AtomicBoolean closed = new AtomicBoolean(false);
        AtomicInteger autoCommitCount = new AtomicInteger(0);

        Connection conn = fakeConnection(committed, rolledBack, closed, autoCommitCount, false);
        DatabaseConnectionManager manager = fakeManager(conn);
        TransactionManager tm = new TransactionManager(manager);

        String result = tm.execute((TransactionCallback<String>) c -> "ximilib-ok");

        assertEquals("ximilib-ok", result, "Kết quả phải đúng với giá trị callback trả về");
        assertTrue(committed.get(), "commit() phải được gọi khi thành công");
        assertFalse(rolledBack.get(), "rollback() không được gọi khi thành công");
        assertTrue(closed.get(), "close() phải được gọi trong finally");
        assertEquals(2, autoCommitCount.get(), "setAutoCommit phải được gọi 2 lần (false + true)");
    }

    @Test
    @DisplayName("TM-02: execute(callback) khi callback ném SQLException → ném RuntimeException chứa message tiếng Việt, rollback được gọi")
    void execute_whenCallbackThrows_throwsRuntimeExceptionAndRollsBack() {
        AtomicBoolean committed = new AtomicBoolean(false);
        AtomicBoolean rolledBack = new AtomicBoolean(false);
        AtomicBoolean closed = new AtomicBoolean(false);
        AtomicInteger autoCommitCount = new AtomicInteger(0);

        Connection conn = fakeConnection(committed, rolledBack, closed, autoCommitCount, false);
        DatabaseConnectionManager manager = fakeManager(conn);
        TransactionManager tm = new TransactionManager(manager);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                tm.execute((TransactionCallback<Void>) c -> {
                    throw new SQLException("Giả lập lỗi DB");
                })
        );

        assertTrue(ex.getMessage().contains("Giao dịch CSDL thất bại"),
                "Message phải chứa 'Giao dịch CSDL thất bại', thực tế: " + ex.getMessage());
        assertFalse(committed.get(), "commit() không được gọi khi lỗi");
        assertTrue(rolledBack.get(), "rollback() phải được gọi khi lỗi");
        assertTrue(closed.get(), "close() phải được gọi trong finally dù có lỗi");
    }

    @Test
    @DisplayName("TM-03: execute(VoidTransactionCallback) hoạt động bình thường, commit được gọi")
    void execute_voidCallback_works() {
        AtomicBoolean committed = new AtomicBoolean(false);
        AtomicBoolean rolledBack = new AtomicBoolean(false);
        AtomicBoolean closed = new AtomicBoolean(false);
        AtomicInteger autoCommitCount = new AtomicInteger(0);
        AtomicBoolean callbackExecuted = new AtomicBoolean(false);

        Connection conn = fakeConnection(committed, rolledBack, closed, autoCommitCount, false);
        DatabaseConnectionManager manager = fakeManager(conn);
        TransactionManager tm = new TransactionManager(manager);

        assertDoesNotThrow(() ->
                tm.execute((VoidTransactionCallback) c -> callbackExecuted.set(true))
        );

        assertTrue(callbackExecuted.get(), "VoidCallback phải được thực thi");
        assertTrue(committed.get(), "commit() phải được gọi");
        assertFalse(rolledBack.get(), "rollback() không được gọi khi thành công");
        assertTrue(closed.get(), "close() phải được gọi");
    }

    @Test
    @DisplayName("TM-04: constructor ném NullPointerException khi connectionManager là null")
    void constructor_throwsNPE_whenManagerIsNull() {
        assertThrows(NullPointerException.class, () -> new TransactionManager(null));
    }

    @Test
    @DisplayName("TM-05: execute(VoidTransactionCallback) khi callback ném exception → rollback được gọi và ném RuntimeException")
    void execute_voidCallback_whenThrows_rollsBack() {
        AtomicBoolean committed = new AtomicBoolean(false);
        AtomicBoolean rolledBack = new AtomicBoolean(false);
        AtomicBoolean closed = new AtomicBoolean(false);
        AtomicInteger autoCommitCount = new AtomicInteger(0);

        Connection conn = fakeConnection(committed, rolledBack, closed, autoCommitCount, false);
        DatabaseConnectionManager manager = fakeManager(conn);
        TransactionManager tm = new TransactionManager(manager);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                tm.execute((VoidTransactionCallback) c -> {
                    throw new SQLException("Lỗi void callback");
                })
        );

        assertTrue(ex.getMessage().contains("Giao dịch CSDL thất bại"),
                "Message phải chứa 'Giao dịch CSDL thất bại'");
        assertFalse(committed.get(), "commit() không được gọi khi lỗi");
        assertTrue(rolledBack.get(), "rollback() phải được gọi");
        assertTrue(closed.get(), "close() phải được gọi trong finally");
    }
}
