package com.ximilib.domain.model;

/**
 * LoanType: Hình thức mượn sách (phieumuon.Hinhthuc)
 * Phân biệt giữa đọc tại chỗ (hạn 17:00 trong ngày) và mượn về nhà (tối đa 14 ngày, 3 cuốn).
 */
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
