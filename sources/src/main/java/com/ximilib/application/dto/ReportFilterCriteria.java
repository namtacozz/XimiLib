package com.ximilib.application.dto;

import java.time.LocalDate;

/**
 * Tiêu chí lọc nâng cao cho các loại báo cáo & thống kê thư viện.
 */
public class ReportFilterCriteria {

    // === 1. Thống kê mượn - trả ===
    private String readerName;     // Tên bạn đọc hoặc mã bạn đọc
    private String loanType;       // Hình thức: ALL, DOC_TAI_CHO, VE_NHA
    private LocalDate fromDate;    // Ngày mượn từ ngày
    private LocalDate toDate;      // Ngày mượn đến ngày
    private Integer status;        // Mã trạng thái phiếu: null (Tất cả), 0..6

    // === 2. Độc giả quá hạn & nợ phạt ===
    private String overdueKeyword; // Tên bạn đọc, mã thẻ, số ĐT
    private Boolean onlyHasFine;   // Chỉ hiện người đang nợ phạt > 0

    // === 3. Sách hỏng - mất ===
    private String damagedKeyword; // Mã cuốn, tựa sách
    private String copyStatus;     // Trạng thái: ALL, HONG, DA_MAT, HONG_MAT
    private String location;       // Khu vực hoặc kệ sách

    // === 4. Top 10 đầu sách ===
    private String bookKeyword;    // Tên đầu sách hoặc mã sách
    private String categoryName;   // Thể loại sách

    public ReportFilterCriteria() {}

    // Getters & Setters
    public String getReaderName() { return readerName; }
    public void setReaderName(String readerName) { this.readerName = readerName; }

    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }

    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }

    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getOverdueKeyword() { return overdueKeyword; }
    public void setOverdueKeyword(String overdueKeyword) { this.overdueKeyword = overdueKeyword; }

    public Boolean getOnlyHasFine() { return onlyHasFine; }
    public void setOnlyHasFine(Boolean onlyHasFine) { this.onlyHasFine = onlyHasFine; }

    public String getDamagedKeyword() { return damagedKeyword; }
    public void setDamagedKeyword(String damagedKeyword) { this.damagedKeyword = damagedKeyword; }

    public String getCopyStatus() { return copyStatus; }
    public void setCopyStatus(String copyStatus) { this.copyStatus = copyStatus; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getBookKeyword() { return bookKeyword; }
    public void setBookKeyword(String bookKeyword) { this.bookKeyword = bookKeyword; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
