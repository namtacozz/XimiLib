package com.ximilib.infrastructure.persistence;

import com.ximilib.application.dto.BorrowTicketDto;
import com.ximilib.application.service.CirculationService;
import com.ximilib.application.service.impl.CirculationServiceImpl;
import com.ximilib.domain.model.LoanType;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import com.ximilib.infrastructure.persistence.connection.JdbcTransactionCoordinator;
import com.ximilib.infrastructure.persistence.impl.CirculationRepositoryImpl;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class CirculationConcurrencyIT {

    private static DatabaseConnectionManager manager;
    private static boolean dbAvailable = false;
    private static CirculationRepositoryImpl repo;
    private static JdbcTransactionCoordinator coordinator;
    private static CirculationService circulationService;

    @BeforeAll
    static void setUp() {
        try {
            manager = new DatabaseConnectionManager();
            try (Connection conn = manager.getConnection()) {
                dbAvailable = conn != null && !conn.isClosed();
            }
            if (dbAvailable) {
                repo = new CirculationRepositoryImpl(manager);
                coordinator = new JdbcTransactionCoordinator(manager);
                circulationService = new CirculationServiceImpl(repo, repo, null, coordinator);
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
    @DisplayName("IT-CONCURRENCY-01: Hai luồng cùng mượn 1 cuốn sách: đúng 1 luồng thành công, 1 thất bại, không deadlock")
    void testConcurrentBorrowSameCopy() throws InterruptedException, ExecutionException {
        assumeTrue(dbAvailable, "Bỏ qua: Không có kết nối MySQL");

        String targetBarcode = findFirstAvailableCopyBarcode();
        assumeTrue(targetBarcode != null, "Không có cuốn sách KHA_DUNG để thử nghiệm tương tranh");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);

        Callable<Boolean> task1 = () -> {
            try {
                barrier.await(3, TimeUnit.SECONDS);
                circulationService.createLoan("USR-ADMIN-01", List.of(targetBarcode), LoanType.TAI_CHO, "IT-Thread-1");
                return true;
            } catch (Exception e) {
                return false;
            }
        };

        Callable<Boolean> task2 = () -> {
            try {
                barrier.await(3, TimeUnit.SECONDS);
                circulationService.createLoan("USR-ADMIN-01", List.of(targetBarcode), LoanType.TAI_CHO, "IT-Thread-2");
                return true;
            } catch (Exception e) {
                return false;
            }
        };

        Future<Boolean> f1 = executor.submit(task1);
        Future<Boolean> f2 = executor.submit(task2);

        boolean r1 = f1.get();
        boolean r2 = f2.get();

        executor.shutdown();

        assertTrue((r1 && !r2) || (!r1 && r2),
                "Phải có đúng 1 luồng thành công và 1 luồng bị chặn, thực tế: r1=" + r1 + ", r2=" + r2);
    }

    @Test
    @DisplayName("IT-CONCURRENCY-02: Lỗi giữa chừng trong transaction phải rollback toàn bộ")
    void testRollbackOnErrorMidway() {
        assumeTrue(dbAvailable, "Bỏ qua: Không có kết nối MySQL");

        String fakeTicketId = "TEST-ROLLBACK-" + System.currentTimeMillis();

        assertThrows(RuntimeException.class, () -> {
            coordinator.executeVoid(tx -> {

                repo.createBorrowTicket(tx, fakeTicketId, "USR-ADMIN-01", "CARD-ADMIN", LoanType.TAI_CHO,
                        java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusDays(1),
                        Collections.emptyList(), "Ghi chú test rollback");

                throw new RuntimeException("Cố ý ném lỗi để kiểm tra hoàn tác transaction");
            });
        });

        String checkSql = "SELECT COUNT(*) FROM phieumuon WHERE Maphieumuon = ?";
        try (Connection conn = manager.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, fakeTicketId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1), "Dữ liệu phải được rollback hoàn toàn khi gặp lỗi");
            }
        } catch (SQLException e) {
            fail("Lỗi truy vấn kiểm tra rollback: " + e.getMessage());
        }
    }

    private String findFirstAvailableCopyBarcode() {
        String sql = "SELECT Macuonsach FROM cuonsach WHERE Trangthaisach = 'KHA_DUNG' LIMIT 1";
        try (Connection conn = manager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }
}
