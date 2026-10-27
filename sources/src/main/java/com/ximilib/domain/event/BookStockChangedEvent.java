package com.ximilib.domain.event;

import java.time.Instant;

/**
 * BookStockChangedEvent: Sự kiện miền phát sinh khi số lượng tồn kho cuốn sách thay đổi.
 * Phục vụ cơ chế Observer cập nhật giao diện thời gian thực và đồng bộ đa máy.
 */
public class BookStockChangedEvent {

    private final String bookId;
    private final String copyId;
    private final String oldStatus;
    private final String newStatus;
    private final Instant timestamp;

    public BookStockChangedEvent(String bookId, String newStatus) {
        this(bookId, null, null, newStatus, Instant.now());
    }

    public BookStockChangedEvent(String bookId, String copyId, String oldStatus, String newStatus, Instant timestamp) {
        this.bookId = bookId;
        this.copyId = copyId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.timestamp = (timestamp != null) ? timestamp : Instant.now();
    }

    public String getBookId() {
        return bookId;
    }

    public String getCopyId() {
        return copyId;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
