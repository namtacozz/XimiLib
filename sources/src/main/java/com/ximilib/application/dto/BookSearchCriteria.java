package com.ximilib.application.dto;

/**
 * BookSearchCriteria: Tiêu chí tìm kiếm và lọc danh mục sách từ UI chuyển giao sang Application Service.
 */
public class BookSearchCriteria {

    public enum AvailabilityFilter {
        ALL,            // Tất cả
        AVAILABLE_ONLY, // Còn sách (> 0 khả dụng)
        OUT_OF_STOCK    // Hết sách (= 0 khả dụng)
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
