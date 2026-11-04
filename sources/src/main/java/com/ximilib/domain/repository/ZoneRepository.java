package com.ximilib.domain.repository;

import com.ximilib.domain.model.Zone;
import java.util.List;
import java.util.Optional;

public interface ZoneRepository {

    Optional<Zone> findById(String zoneId);

    List<Zone> findAll();

    void save(Zone zone);

    void update(Zone zone);

    void delete(String zoneId);

    int countShelvesInZone(String zoneId);
}
