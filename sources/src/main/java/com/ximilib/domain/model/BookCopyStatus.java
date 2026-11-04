package com.ximilib.domain.model;

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
