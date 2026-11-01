package com.ximilib.application.dto;

/**
 * ShelfDto: DTO tóm tắt kệ sách kèm số lượng cuốn sách đang lưu trên kệ.
 */
public record ShelfDto(
    String shelfId,
    String zoneId,
    String zoneName,
    String shelfName,
    int floor,
    String description,
    int copyCount
) {}
