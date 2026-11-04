package com.ximilib.application.dto;

public record ZoneDto(
    String zoneId,
    String zoneName,
    String description,
    int shelfCount
) {}
