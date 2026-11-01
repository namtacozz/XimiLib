package com.ximilib.domain.repository;

import com.ximilib.domain.model.Shelf;
import java.util.List;
import java.util.Optional;

/**
 * ShelfRepository: Hợp đồng lưu trữ và truy vấn kệ sách (bảng kesach).
 * Không import DTO, JDBC, SQL hay Swing (Quy tắc R-01).
 */
public interface ShelfRepository {

    Optional<Shelf> findById(String shelfId);

    List<Shelf> findAll();

    List<Shelf> findByZoneId(String zoneId);

    void save(Shelf shelf);

    void update(Shelf shelf);

    void delete(String shelfId);

    int countCopiesOnShelf(String shelfId);
}
