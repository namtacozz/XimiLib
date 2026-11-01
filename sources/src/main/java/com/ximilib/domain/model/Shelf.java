package com.ximilib.domain.model;

import java.util.Objects;

/**
 * Shelf: Thực thể Kệ sách lưu trữ sách vật lý đặt tại từng phân khu (bảng kesach).
 */
public class Shelf {

    private String shelfId;
    private String zoneId;
    private String shelfName;
    private int floor;
    private String description;

    public Shelf() {
        this.floor = 1;
    }

    public Shelf(String shelfId, String zoneId, String shelfName, int floor, String description) {
        this.shelfId = Objects.requireNonNull(shelfId, "Mã kệ sách không được null");
        this.zoneId = Objects.requireNonNull(zoneId, "Mã khu vực không được null");
        this.shelfName = Objects.requireNonNull(shelfName, "Tên kệ sách không được null");
        this.floor = floor;
        this.description = description;
    }

    public String getShelfId() {
        return shelfId;
    }

    public void setShelfId(String shelfId) {
        this.shelfId = shelfId;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getShelfName() {
        return shelfName;
    }

    public void setShelfName(String shelfName) {
        this.shelfName = shelfName;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
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
        if (!(o instanceof Shelf shelf)) return false;
        return Objects.equals(shelfId, shelf.shelfId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shelfId);
    }

    @Override
    public String toString() {
        return "Shelf{" +
                "shelfId='" + shelfId + '\'' +
                ", zoneId='" + zoneId + '\'' +
                ", shelfName='" + shelfName + '\'' +
                ", floor=" + floor +
                ", description='" + description + '\'' +
                '}';
    }
}
