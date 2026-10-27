package com.ximilib.application.dto;

import java.math.BigDecimal;

/**
 * ReturnResultDto: Kết quả xử lý trả sách tại quầy.
 */
public class ReturnResultDto {
    private final String ticketId;
    private final String copyBarcode;
    private final int overdueDays;
    private final BigDecimal overdueFine;
    private final BigDecimal damageFine;
    private final BigDecimal totalFine;
    private final boolean fullyReturned;

    public ReturnResultDto(String ticketId, String copyBarcode, int overdueDays,
                           BigDecimal overdueFine, BigDecimal damageFine,
                           BigDecimal totalFine, boolean fullyReturned) {
        this.ticketId = ticketId;
        this.copyBarcode = copyBarcode;
        this.overdueDays = overdueDays;
        this.overdueFine = overdueFine;
        this.damageFine = damageFine;
        this.totalFine = totalFine;
        this.fullyReturned = fullyReturned;
    }

    public String getTicketId() { return ticketId; }
    public String getCopyBarcode() { return copyBarcode; }
    public int getOverdueDays() { return overdueDays; }
    public BigDecimal getOverdueFine() { return overdueFine; }
    public BigDecimal getDamageFine() { return damageFine; }
    public BigDecimal getTotalFine() { return totalFine; }
    public boolean isFullyReturned() { return fullyReturned; }
}
