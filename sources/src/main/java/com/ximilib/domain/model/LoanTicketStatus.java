package com.ximilib.domain.model;

/**
 * LoanTicketStatus: Trạng thái của phiếu mượn (phieumuon.Trangthai)
 * Khớp 100% với FSM trong BUSINESS_RULES.md và đặc tả SRS UC-5, UC-6, UC-9.
 */
public enum LoanTicketStatus {
    CHO_DUYET(0, "Chờ nhận sách (24h)"),
    DANG_MUON(1, "Đang mượn"),
    DA_TRA(2, "Đã trả"),
    QUA_HAN(3, "Quá hạn"),
    CHO_XU_LY_BOI_THUONG(4, "Chờ xử lý bồi thường"),
    DA_MAT(5, "Đã đền bù (Mất sách)"),
    DA_HUY(6, "Đã hủy");

    private final int code;
    private final String description;

    LoanTicketStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static LoanTicketStatus fromCode(int code) {
        for (LoanTicketStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return DANG_MUON;
    }
}
