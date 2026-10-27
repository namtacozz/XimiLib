package com.ximilib.application.dto;

import java.util.Collections;
import java.util.List;

/**
 * PagedResult: Đóng gói kết quả phân trang phía Server trả về cho View.
 */
public class PagedResult<T> {

    private final List<T> items;
    private final int totalElements;
    private final int page;
    private final int pageSize;
    private final int totalPages;

    public PagedResult(List<T> items, int totalElements, int page, int pageSize) {
        this.items = (items != null) ? items : Collections.emptyList();
        this.totalElements = totalElements;
        this.page = page;
        this.pageSize = Math.max(1, pageSize);
        this.totalPages = (int) Math.ceil((double) totalElements / this.pageSize);
    }

    public List<T> getItems() {
        return items;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return Math.max(1, totalPages);
    }

    public boolean hasPrevious() {
        return page > 1;
    }

    public boolean hasNext() {
        return page < totalPages;
    }
}
