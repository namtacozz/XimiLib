package com.ximilib.domain.model;

import java.util.Objects;

/**
 * Zone: Thực thể Phân khu vị trí lưu trữ trong thư viện (bảng khuvuc).
 * Ví dụ: Tầng 1, Khu Tự nhiên, Khu Xã hội.
 */
public class Zone {

    private String zoneId;
    private String zoneName;
    private String description;

    public Zone() {
    }

    public Zone(String zoneId, String zoneName, String description) {
        this.zoneId = Objects.requireNonNull(zoneId, "Mã khu vực không được null");
        this.zoneName = Objects.requireNonNull(zoneName, "Tên khu vực không được null");
        this.description = description;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Zone zone)) return false;
        return Objects.equals(zoneId, zone.zoneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zoneId);
    }

    @Override
    public String toString() {
        return "Zone{" +
                "zoneId='" + zoneId + '\'' +
                ", zoneName='" + zoneName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
