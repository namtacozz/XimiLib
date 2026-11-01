package com.ximilib.infrastructure.persistence;

import com.ximilib.domain.model.Shelf;
import com.ximilib.domain.model.Zone;
import com.ximilib.domain.repository.ShelfRepository;
import com.ximilib.domain.repository.ZoneRepository;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import com.ximilib.infrastructure.persistence.impl.ShelfRepositoryImpl;
import com.ximilib.infrastructure.persistence.impl.ZoneRepositoryImpl;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * StorageRepositoryIT: Integration Test cho ZoneRepository và ShelfRepository trên MySQL.
 * Bỏ qua gracefully nếu MySQL không sẵn sàng.
 */
public class StorageRepositoryIT {

    private static DatabaseConnectionManager manager;
    private static boolean dbAvailable = false;
    private static ZoneRepository zoneRepo;
    private static ShelfRepository shelfRepo;

    @BeforeAll
    static void setUp() {
        try {
            manager = new DatabaseConnectionManager();
            try (Connection conn = manager.getConnection()) {
                dbAvailable = conn != null && !conn.isClosed();
            }
            if (dbAvailable) {
                zoneRepo = new ZoneRepositoryImpl(manager);
                shelfRepo = new ShelfRepositoryImpl(manager);
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
    @DisplayName("IT-ST-01: Truy vấn danh sách phân khu kho sách từ CSDL MySQL")
    void testFindAllZones() {
        assumeTrue(dbAvailable, "Bỏ qua: Không có kết nối MySQL");
        List<Zone> zones = zoneRepo.findAll();
        assertNotNull(zones);
    }

    @Test
    @DisplayName("IT-ST-02: Thêm mới, cập nhật và xóa phân khu tạm")
    void testZoneCrudCycle() {
        assumeTrue(dbAvailable, "Bỏ qua: Không có kết nối MySQL");
        String testZoneId = "KV-IT-TEST-" + System.currentTimeMillis() % 10000;
        Zone zone = new Zone(testZoneId, "Khu Thử Nghiệm", "Ghi chú IT");

        zoneRepo.save(zone);

        Optional<Zone> found = zoneRepo.findById(testZoneId);
        assertTrue(found.isPresent());
        assertEquals("Khu Thử Nghiệm", found.get().getZoneName());

        zone.setZoneName("Khu Thử Nghiệm Đổi Tên");
        zoneRepo.update(zone);

        Optional<Zone> updated = zoneRepo.findById(testZoneId);
        assertTrue(updated.isPresent());
        assertEquals("Khu Thử Nghiệm Đổi Tên", updated.get().getZoneName());

        zoneRepo.delete(testZoneId);
        assertTrue(zoneRepo.findById(testZoneId).isEmpty());
    }
}
