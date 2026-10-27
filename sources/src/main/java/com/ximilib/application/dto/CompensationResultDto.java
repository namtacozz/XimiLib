package com.ximilib.application.dto;

import java.math.BigDecimal;

/**
 * CompensationResultDto: Bảng kê chi tiết bồi thường khi báo mất sách (UC-9, BR-08).
 */
public class CompensationResultDto {
    private final String fineTicketId;
    private final String borrowTicketId;
    private final String bookTitle;
    private final BigDecimal coverPrice;
    private final BigDecimal bookLossAmount; // 200% giá bìa
    private final BigDecimal techFee;        // 20.000 VNĐ
    private final int overdueDays;
    private final BigDecimal overdueFine;   // 2.000 VNĐ / ngày
    private final BigDecimal totalAmount;

    public CompensationResultDto(String fineTicketId, String borrowTicketId, String bookTitle,
                                 BigDecimal coverPrice, BigDecimal bookLossAmount,
                                 BigDecimal techFee, int overdueDays, BigDecimal overdueFine,
                                 BigDecimal totalAmount) {
        this.fineTicketId = fineTicketId;
        this.borrowTicketId = borrowTicketId;
        this.bookTitle = bookTitle;
        this.coverPrice = coverPrice;
        this.bookLossAmount = bookLossAmount;
        this.techFee = techFee;
        this.overdueDays = overdueDays;
        this.overdueFine = overdueFine;
        this.totalAmount = totalAmount;
    }

    public String getFineTicketId() { return fineTicketId; }
    public String getBorrowTicketId() { return borrowTicketId; }
    public String getBookTitle() { return bookTitle; }
    public BigDecimal getCoverPrice() { return coverPrice; }
    public BigDecimal getBookLossAmount() { return bookLossAmount; }
    public BigDecimal getTechFee() { return techFee; }
    public int getOverdueDays() { return overdueDays; }
    public BigDecimal getOverdueFine() { return overdueFine; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}
