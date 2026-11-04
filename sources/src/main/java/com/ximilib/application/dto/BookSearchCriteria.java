package com.ximilib.application.dto;

public class BookSearchCriteria {

    public enum AvailabilityFilter {
        ALL,
        AVAILABLE_ONLY,
        OUT_OF_STOCK
    }

    private String keyword;
    private Integer categoryId;
    private AvailabilityFilter availability;

    public BookSearchCriteria() {
        this("", null, AvailabilityFilter.ALL);
    }

    public BookSearchCriteria(String keyword, Integer categoryId, AvailabilityFilter availability) {
        this.keyword = (keyword != null) ? keyword.trim() : "";
        this.categoryId = categoryId;
        this.availability = (availability != null) ? availability : AvailabilityFilter.ALL;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = (keyword != null) ? keyword.trim() : "";
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public AvailabilityFilter getAvailability() {
        return availability;
    }

    public void setAvailability(AvailabilityFilter availability) {
        this.availability = (availability != null) ? availability : AvailabilityFilter.ALL;
    }
}
