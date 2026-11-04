package com.ximilib.domain.model;

public enum LoanType {
    TAI_CHO("Đọc tại chỗ"),
    VE_NHA("Mượn về nhà");

    private final String description;

    LoanType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
