package com.ximilib.application.service;

import com.ximilib.application.dto.ShelfDto;
import com.ximilib.application.dto.ZoneDto;
import com.ximilib.domain.model.Shelf;
import com.ximilib.domain.model.Zone;
import java.util.List;
import java.util.Optional;

/**
 * StorageService: Hợp đồng Application Service quản lý Phân khu và Kệ sách thư viện (Kho & Vị trí).
 * Tuân thủ Clean Architecture (R-01: Presentation chỉ gọi Application Service, không gọi DAO/DB).
 */
public interface StorageService {

    List<Zone> getAllZones();

    Optional<Zone> getZoneById(String zoneId);

    void createZone(String zoneId, String zoneName, String description);

    void updateZone(String zoneId, String zoneName, String description);

    void deleteZone(String zoneId);

    List<Shelf> getAllShelves();

    List<Shelf> getShelvesByZone(String zoneId);

    Optional<Shelf> getShelfById(String shelfId);

    void createShelf(String shelfId, String zoneId, String shelfName, int floor, String description);

    void updateShelf(String shelfId, String zoneId, String shelfName, int floor, String description);

    void deleteShelf(String shelfId);

    List<ZoneDto> getAllZoneSummaries();

    List<ShelfDto> getShelfSummariesByZone(String zoneId);

    List<ShelfDto> getAllShelfSummaries();
}
