package com.ximilib.infrastructure.persistence;

import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assumptions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DBConnectionLeakIT: Integration Test kiểm tra kết nối MySQL thật.
 * Yêu cầu: MySQL đang chạy và config/db.properties hợp lệ.
 * Dùng Assumptions.assumeTrue để skip gracefully khi thiếu DB.
 */
public class DBConnectionLeakIT {

    private static DatabaseConnectionManager manager;
    private static boolean dbAvailable = false;

    @BeforeAll
    static void setUp() {
        try {
            manager = new DatabaseConnectionManager();
            // Thử lấy connection để kiểm tra DB có sẵn không
            try (Connection conn = manager.getConnection()) {
                dbAvailable = conn != null && !conn.isClosed();
            }
        } catch (Exception e) {
            dbAvailable = false;
        }
    }

    @AfterAll
    static void tearDown() {
        if (manager != null) {
            manager.shutdown();
        }
    }

    @Test
    @DisplayName("IT-DB-01: Số bảng trong CSDL phải là 23 (20 gốc + 3 bảng V2)")
    void testTableCount() throws SQLException {
        assumeTrue(dbAvailable, "Bỏ qua: Không có kết nối MySQL");
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()";
        try (Connection conn = manager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Assertions.assertTrue(rs.next());
            int count = rs.getInt(1);
            Assertions.assertEquals(23, count,
                    "Kỳ vọng 23 bảng (schema.sql + V2), thực tế: " + count);
        }
    }

    @Test
    @DisplayName("IT-DB-02: Số đầu sách phải là 221 (sau seed_books.sql)")
    void testBookCount() throws SQLException {
        assumeTrue(dbAvailable, "Bỏ qua: Không có kết nối MySQL");
        String sql = "SELECT COUNT(*) FROM sach WHERE IsDeleted = FALSE";
        try (Connection conn = manager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            Assertions.assertTrue(rs.next());
            int count = rs.getInt(1);
            Assertions.assertEquals(221, count,
                    "Kỳ vọng 221 đầu sách (sau seed_books.sql), thực tế: " + count);
        }
    }

    @Test
    @DisplayName("IT-DB-03: Không rò rỉ connection sau khi dùng try-with-resources N=100 lần (đo Threads_connected)")
    void testNoConnectionLeak() throws SQLException {
        assumeTrue(dbAvailable, "Bỏ qua: Không có kết nối MySQL");

        int initialThreadsConnected = getThreadsConnected();

        int n = 100;
        for (int i = 0; i < n; i++) {
            try (Connection conn = manager.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT 1");
                 ResultSet rs = ps.executeQuery()) {
                Assertions.assertTrue(rs.next());
            }
        }

        int afterThreadsConnected = getThreadsConnected();
        Assertions.assertEquals(initialThreadsConnected, afterThreadsConnected,
                "Rò rỉ connection: Threads_connected tăng từ " + initialThreadsConnected + " lên " + afterThreadsConnected);
    }

    private int getThreadsConnected() throws SQLException {
        String sql = "SHOW STATUS LIKE 'Threads_connected'";
        try (Connection conn = manager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Integer.parseInt(rs.getString(2));
            }
        }
        return -1;
    }
}
