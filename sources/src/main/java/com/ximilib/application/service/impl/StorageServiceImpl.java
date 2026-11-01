package com.ximilib.application.service.impl;

import com.ximilib.application.dto.ShelfDto;
import com.ximilib.application.dto.ZoneDto;
import com.ximilib.application.port.StorageQueryPort;
import com.ximilib.application.service.StorageService;
import com.ximilib.domain.exception.BookException;
import com.ximilib.domain.exception.CopyException;
import com.ximilib.domain.model.Shelf;
import com.ximilib.domain.model.Zone;
import com.ximilib.domain.repository.ShelfRepository;
import com.ximilib.domain.repository.ZoneRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * StorageServiceImpl: Triển khai các quy tắc nghiệp vụ quản lý Kho và Vị trí Kệ sách.
 * Tuân thủ R-01, R-02 (xác thực hợp lệ và kiểm tra ràng buộc sách tại Application).
 */
public class StorageServiceImpl implements StorageService {

    private final ZoneRepository zoneRepository;
    private final ShelfRepository shelfRepository;
    private final StorageQueryPort storageQueryPort;

    public StorageServiceImpl(ZoneRepository zoneRepository, ShelfRepository shelfRepository) {
        this(zoneRepository, shelfRepository,
                (shelfRepository instanceof StorageQueryPort sqp) ? sqp : null);
    }

    public StorageServiceImpl(ZoneRepository zoneRepository, ShelfRepository shelfRepository, StorageQueryPort storageQueryPort) {
        this.zoneRepository = Objects.requireNonNull(zoneRepository, "zoneRepository không được null");
        this.shelfRepository = Objects.requireNonNull(shelfRepository, "shelfRepository không được null");
        this.storageQueryPort = storageQueryPort;
    }

    @Override
    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    @Override
    public Optional<Zone> getZoneById(String zoneId) {
        if (zoneId == null || zoneId.trim().isEmpty()) {
            return Optional.empty();
        }
        return zoneRepository.findById(zoneId.trim());
    }

    @Override
    public void createZone(String zoneId, String zoneName, String description) {
        if (zoneId == null || zoneId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khu vực không được để trống.");
        }
        if (zoneName == null || zoneName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khu vực không được để trống.");
        }
        if (zoneRepository.findById(zoneId.trim()).isPresent()) {
            throw new IllegalArgumentException("Mã khu vực \"" + zoneId + "\" đã tồn tại.");
        }
        zoneRepository.save(new Zone(zoneId.trim(), zoneName.trim(), description));
    }

    @Override
    public void updateZone(String zoneId, String zoneName, String description) {
        if (zoneId == null || zoneId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khu vực không được để trống.");
        }
        if (zoneName == null || zoneName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khu vực không được để trống.");
        }
        if (zoneRepository.findById(zoneId.trim()).isEmpty()) {
            throw new IllegalArgumentException("Khu vực \"" + zoneId + "\" không tồn tại.");
        }
        zoneRepository.update(new Zone(zoneId.trim(), zoneName.trim(), description));
    }

    @Override
    public void deleteZone(String zoneId) {
        if (zoneId == null || zoneId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khu vực không được để trống.");
        }
        int shelves = zoneRepository.countShelvesInZone(zoneId.trim());
        if (shelves > 0) {
            throw new BookException("ZONE_NOT_EMPTY",
                    "Không thể xóa khu vực \"" + zoneId + "\": Hiện vẫn còn " + shelves + " kệ sách trực thuộc khu vực này.");
        }
        zoneRepository.delete(zoneId.trim());
    }

    @Override
    public List<Shelf> getAllShelves() {
        return shelfRepository.findAll();
    }

    @Override
    public List<Shelf> getShelvesByZone(String zoneId) {
        if (zoneId == null || zoneId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return shelfRepository.findByZoneId(zoneId.trim());
    }

    @Override
    public Optional<Shelf> getShelfById(String shelfId) {
        if (shelfId == null || shelfId.trim().isEmpty()) {
            return Optional.empty();
        }
        return shelfRepository.findById(shelfId.trim());
    }

    @Override
    public void createShelf(String shelfId, String zoneId, String shelfName, int floor, String description) {
        if (shelfId == null || shelfId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã kệ sách không được để trống.");
        }
        if (zoneId == null || zoneId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khu vực không được để trống.");
        }
        if (shelfName == null || shelfName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên kệ sách không được để trống.");
        }
        if (zoneRepository.findById(zoneId.trim()).isEmpty()) {
            throw new IllegalArgumentException("Khu vực \"" + zoneId + "\" không tồn tại.");
        }
        if (shelfRepository.findById(shelfId.trim()).isPresent()) {
            throw new IllegalArgumentException("Mã kệ sách \"" + shelfId + "\" đã tồn tại.");
        }
        shelfRepository.save(new Shelf(shelfId.trim(), zoneId.trim(), shelfName.trim(), floor, description));
    }

    @Override
    public void updateShelf(String shelfId, String zoneId, String shelfName, int floor, String description) {
        if (shelfId == null || shelfId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã kệ sách không được để trống.");
        }
        if (zoneId == null || zoneId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khu vực không được để trống.");
        }
        if (shelfName == null || shelfName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên kệ sách không được để trống.");
        }
        if (zoneRepository.findById(zoneId.trim()).isEmpty()) {
            throw new IllegalArgumentException("Khu vực \"" + zoneId + "\" không tồn tại.");
        }
        if (shelfRepository.findById(shelfId.trim()).isEmpty()) {
            throw new IllegalArgumentException("Kệ sách \"" + shelfId + "\" không tồn tại.");
        }
        shelfRepository.update(new Shelf(shelfId.trim(), zoneId.trim(), shelfName.trim(), floor, description));
    }

    @Override
    public void deleteShelf(String shelfId) {
        if (shelfId == null || shelfId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã kệ sách không được để trống.");
        }
        int copies = shelfRepository.countCopiesOnShelf(shelfId.trim());
        if (copies > 0) {
            throw new CopyException("SHELF_NOT_EMPTY",
                    "Không thể xóa kệ sách \"" + shelfId + "\": Hiện vẫn còn " + copies + " cuốn sách đang được xếp trên kệ.");
        }
        shelfRepository.delete(shelfId.trim());
    }

    @Override
    public List<ZoneDto> getAllZoneSummaries() {
        if (storageQueryPort != null) {
            return storageQueryPort.getAllZoneSummaries();
        }
        return Collections.emptyList();
    }

    @Override
    public List<ShelfDto> getShelfSummariesByZone(String zoneId) {
        if (storageQueryPort != null && zoneId != null) {
            return storageQueryPort.getShelvesByZone(zoneId);
        }
        return Collections.emptyList();
    }

    @Override
    public List<ShelfDto> getAllShelfSummaries() {
        if (storageQueryPort != null) {
            return storageQueryPort.getAllShelfSummaries();
        }
        return Collections.emptyList();
    }
}
