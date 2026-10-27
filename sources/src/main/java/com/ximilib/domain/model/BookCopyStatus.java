package com.ximilib.domain.model;

/**
 * BookCopyStatus: Trạng thái của cuốn sách vật lý (cuonsach.Trangthaisach)
 * Khớp với FSM trong BUSINESS_RULES.md và DATABASE_SCHEMA.md.
 */
public enum BookCopyStatus {
    KHA_DUNG("Khả dụng"),
    DANG_MUON("Đang mượn"),
    DANG_DAT_TRUOC("Đang đặt trước (24h)"),
    HONG_MAT("Hỏng / Mất");

    private final String description;

    BookCopyStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
