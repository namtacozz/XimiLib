package com.ximilib.application.port;

import com.ximilib.application.dto.ShelfDto;
import com.ximilib.application.dto.ZoneDto;
import java.util.List;

/**
 * StorageQueryPort: Cổng truy vấn thông tin vị trí lưu trữ kho và kệ sách (CQRS Read-Model).
 */
public interface StorageQueryPort {

    List<ZoneDto> getAllZoneSummaries();

    List<ShelfDto> getShelvesByZone(String zoneId);

    List<ShelfDto> getAllShelfSummaries();
}
