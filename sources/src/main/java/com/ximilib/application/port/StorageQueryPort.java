package com.ximilib.application.port;

import com.ximilib.application.dto.ShelfDto;
import com.ximilib.application.dto.ZoneDto;
import java.util.List;

public interface StorageQueryPort {

    List<ZoneDto> getAllZoneSummaries();

    List<ShelfDto> getShelvesByZone(String zoneId);

    List<ShelfDto> getAllShelfSummaries();
}
