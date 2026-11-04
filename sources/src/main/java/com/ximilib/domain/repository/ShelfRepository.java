package com.ximilib.domain.repository;

import com.ximilib.domain.model.Shelf;
import java.util.List;
import java.util.Optional;

public interface ShelfRepository {

    Optional<Shelf> findById(String shelfId);

    List<Shelf> findAll();

    List<Shelf> findByZoneId(String zoneId);

    void save(Shelf shelf);

    void update(Shelf shelf);

    void delete(String shelfId);

    int countCopiesOnShelf(String shelfId);
}
