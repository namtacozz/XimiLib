package com.ximilib.application.dto;

/**
 * ZoneDto: DTO tóm tắt khu vực kèm số lượng kệ sách trực thuộc.
 */
public record ZoneDto(
    String zoneId,
    String zoneName,
    String description,
    int shelfCount
) {}
