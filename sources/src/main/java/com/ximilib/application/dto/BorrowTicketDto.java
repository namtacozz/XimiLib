package com.ximilib.application.dto;

import com.ximilib.domain.model.LoanTicketStatus;
import com.ximilib.domain.model.LoanType;
import java.time.LocalDateTime;

/**
 * BorrowTicketDto: Đối tượng truyền dữ liệu phiếu mượn sách.
 */
public class BorrowTicketDto {
    private String ticketId;
    private String userId;
    private String userFullName;
    private String cardNumber;
    private LoanType loanType;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private int status;
    private String bookTitle;
    private String copyBarcode;
    private String notes;
    private boolean overdue;

    public BorrowTicketDto() {}

    public BorrowTicketDto(String ticketId, String userId, String userFullName, String cardNumber,
                           LoanType loanType, LocalDateTime borrowDate, LocalDateTime dueDate,
                           LocalDateTime returnDate, int status, String bookTitle,
                           String copyBarcode, String notes) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.userFullName = userFullName;
        this.cardNumber = cardNumber;
        this.loanType = loanType;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.bookTitle = bookTitle;
        this.copyBarcode = copyBarcode;
        this.notes = notes;
        this.overdue = (status == 1 && dueDate != null && LocalDateTime.now().isAfter(dueDate));
    }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public LoanType getLoanType() { return loanType; }
    public void setLoanType(LoanType loanType) { this.loanType = loanType; }

    public LocalDateTime getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate) { this.borrowDate = borrowDate; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getStatusLabel() {
        return LoanTicketStatus.fromCode(status).getDescription();
    }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getCopyBarcode() { return copyBarcode; }
    public String getCopyBarcodes() { return copyBarcode; }
    public void setCopyBarcode(String copyBarcode) { this.copyBarcode = copyBarcode; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }
}
