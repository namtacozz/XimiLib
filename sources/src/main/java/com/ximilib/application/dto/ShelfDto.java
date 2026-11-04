package com.ximilib.application.dto;

public record ShelfDto(
    String shelfId,
    String zoneId,
    String zoneName,
    String shelfName,
    int floor,
    String description,
    int copyCount
) {}
