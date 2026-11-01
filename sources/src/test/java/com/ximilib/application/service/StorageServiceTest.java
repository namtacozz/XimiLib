package com.ximilib.application.service;

import com.ximilib.application.dto.ShelfDto;
import com.ximilib.application.dto.ZoneDto;
import com.ximilib.application.port.StorageQueryPort;
import com.ximilib.application.service.impl.StorageServiceImpl;
import com.ximilib.domain.exception.BookException;
import com.ximilib.domain.exception.CopyException;
import com.ximilib.domain.model.Shelf;
import com.ximilib.domain.model.Zone;
import com.ximilib.domain.repository.ShelfRepository;
import com.ximilib.domain.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Nghiệp vụ Kho & Vị trí Kệ sách (StorageService)")
class StorageServiceTest {

    private StorageService storageService;
    private StubZoneRepository zoneRepo;
    private StubShelfRepository shelfRepo;

    @BeforeEach
    void setUp() {
        zoneRepo = new StubZoneRepository();
        shelfRepo = new StubShelfRepository();
        storageService = new StorageServiceImpl(zoneRepo, shelfRepo, shelfRepo);
    }

    @Test
    @DisplayName("TC-ST-01: Lấy danh sách phân khu và tìm theo mã")
    void testGetAllZonesAndFindById() {
        zoneRepo.zones.put("KV-A", new Zone("KV-A", "Khu Tự Nhiên", "Tầng 1"));
        zoneRepo.zones.put("KV-B", new Zone("KV-B", "Khu Xã Hội", "Tầng 2"));

        List<Zone> all = storageService.getAllZones();
        assertEquals(2, all.size());

        Optional<Zone> found = storageService.getZoneById("KV-A");
        assertTrue(found.isPresent());
        assertEquals("Khu Tự Nhiên", found.get().getZoneName());
    }

    @Test
    @DisplayName("TC-ST-02: Thêm mới phân khu hợp lệ và ném lỗi khi trùng mã")
    void testCreateZone() {
        storageService.createZone("KV-01", "Khu Kỹ Thuật", "Khu C");
        assertTrue(zoneRepo.zones.containsKey("KV-01"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storageService.createZone("KV-01", "Khu Trùng", null));
        assertTrue(ex.getMessage().contains("đã tồn tại"));
    }

    @Test
    @DisplayName("TC-ST-03: Xóa phân khu còn kệ sách ném BookException")
    void testDeleteZoneWithShelvesThrowsBookException() {
        zoneRepo.zones.put("KV-01", new Zone("KV-01", "Khu Kỹ Thuật", null));
        zoneRepo.shelfCounts.put("KV-01", 3);

        BookException ex = assertThrows(BookException.class, () ->
                storageService.deleteZone("KV-01"));
        assertEquals("ZONE_NOT_EMPTY", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("3 kệ sách trực thuộc"));
    }

    @Test
    @DisplayName("TC-ST-04: Xóa phân khu rỗng thành công")
    void testDeleteZoneSuccessWhenEmpty() {
        zoneRepo.zones.put("KV-01", new Zone("KV-01", "Khu Rỗng", null));
        zoneRepo.shelfCounts.put("KV-01", 0);

        storageService.deleteZone("KV-01");
        assertFalse(zoneRepo.zones.containsKey("KV-01"));
    }

    @Test
    @DisplayName("TC-ST-05: Lấy danh sách kệ sách và tìm theo phân khu")
    void testGetShelvesByZone() {
        shelfRepo.shelves.put("KE-01", new Shelf("KE-01", "KV-A", "Kệ Toán", 1, null));
        shelfRepo.shelves.put("KE-02", new Shelf("KE-02", "KV-A", "Kệ Lý", 2, null));
        shelfRepo.shelves.put("KE-03", new Shelf("KE-03", "KV-B", "Kệ Văn", 1, null));

        List<Shelf> zoneA = storageService.getShelvesByZone("KV-A");
        assertEquals(2, zoneA.size());
    }

    @Test
    @DisplayName("TC-ST-06: Thêm kệ sách vào phân khu không tồn tại ném IllegalArgumentException")
    void testCreateShelfInNonExistentZoneThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                storageService.createShelf("KE-99", "KV-NONEXISTENT", "Kệ Mới", 1, null));
        assertTrue(ex.getMessage().contains("không tồn tại"));
    }

    @Test
    @DisplayName("TC-ST-07: Xóa kệ sách còn sách lưu trên kệ ném CopyException")
    void testDeleteShelfWithCopiesThrowsCopyException() {
        shelfRepo.shelves.put("KE-01", new Shelf("KE-01", "KV-A", "Kệ Toán", 1, null));
        shelfRepo.copyCounts.put("KE-01", 5);

        CopyException ex = assertThrows(CopyException.class, () ->
                storageService.deleteShelf("KE-01"));
        assertEquals("SHELF_NOT_EMPTY", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("5 cuốn sách"));
    }

    @Test
    @DisplayName("TC-ST-08: Xóa kệ sách rỗng thành công")
    void testDeleteShelfSuccessWhenEmpty() {
        shelfRepo.shelves.put("KE-01", new Shelf("KE-01", "KV-A", "Kệ Toán", 1, null));
        shelfRepo.copyCounts.put("KE-01", 0);

        storageService.deleteShelf("KE-01");
        assertFalse(shelfRepo.shelves.containsKey("KE-01"));
    }

    // --- STUB IMPLEMENTATIONS ---

    private static class StubZoneRepository implements ZoneRepository {
        final Map<String, Zone> zones = new HashMap<>();
        final Map<String, Integer> shelfCounts = new HashMap<>();

        @Override
        public Optional<Zone> findById(String zoneId) {
            return Optional.ofNullable(zones.get(zoneId));
        }

        @Override
        public List<Zone> findAll() {
            return new ArrayList<>(zones.values());
        }

        @Override
        public void save(Zone zone) {
            zones.put(zone.getZoneId(), zone);
        }

        @Override
        public void update(Zone zone) {
            zones.put(zone.getZoneId(), zone);
        }

        @Override
        public void delete(String zoneId) {
            zones.remove(zoneId);
        }

        @Override
        public int countShelvesInZone(String zoneId) {
            return shelfCounts.getOrDefault(zoneId, 0);
        }
    }

    private static class StubShelfRepository implements ShelfRepository, StorageQueryPort {
        final Map<String, Shelf> shelves = new HashMap<>();
        final Map<String, Integer> copyCounts = new HashMap<>();

        @Override
        public Optional<Shelf> findById(String shelfId) {
            return Optional.ofNullable(shelves.get(shelfId));
        }

        @Override
        public List<Shelf> findAll() {
            return new ArrayList<>(shelves.values());
        }

        @Override
        public List<Shelf> findByZoneId(String zoneId) {
            List<Shelf> result = new ArrayList<>();
            for (Shelf s : shelves.values()) {
                if (Objects.equals(s.getZoneId(), zoneId)) {
                    result.add(s);
                }
            }
            return result;
        }

        @Override
        public void save(Shelf shelf) {
            shelves.put(shelf.getShelfId(), shelf);
        }

        @Override
        public void update(Shelf shelf) {
            shelves.put(shelf.getShelfId(), shelf);
        }

        @Override
        public void delete(String shelfId) {
            shelves.remove(shelfId);
        }

        @Override
        public int countCopiesOnShelf(String shelfId) {
            return copyCounts.getOrDefault(shelfId, 0);
        }

        @Override
        public List<ZoneDto> getAllZoneSummaries() {
            return Collections.emptyList();
        }

        @Override
        public List<ShelfDto> getShelvesByZone(String zoneId) {
            return Collections.emptyList();
        }

        @Override
        public List<ShelfDto> getAllShelfSummaries() {
            return Collections.emptyList();
        }
    }
}
