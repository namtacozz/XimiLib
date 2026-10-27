package com.ximilib.application.dto;

import com.ximilib.domain.model.LoanDueClassification;
import java.time.LocalDate;
import java.util.List;

/**
 * StaffDashboardDto: Bộ dữ liệu vận hành Trang chủ dành cho Thủ thư / Quản trị viên.
 * Hoàn toàn được truy vấn từ MySQL, không chứa dữ liệu mẫu.
 */
public record StaffDashboardDto(
    StaffKpiSummary kpiSummary,
    List<TodoLoanItem> todoLoans,
    List<PendingPickupItem> pendingPickups,
    List<NewBookTileDto> newBooks
) {

    public record StaffKpiSummary(
        int totalTitles,
        int availableCopies,
        int activeBorrowCopies,
        int activeReaders,
        int overdueLoans
    ) {}

    public record TodoLoanItem(
        String ticketId,
        String readerName,
        String firstBookTitle,
        int additionalBooksCount,
        LocalDate dueDate,
        LoanDueClassification classification
    ) {
        public String getDisplayBookTitle() {
            if (additionalBooksCount > 0) {
                return firstBookTitle + " (+" + additionalBooksCount + ")";
            }
            return firstBookTitle != null ? firstBookTitle : "Chưa rõ";
        }
    }

    public record PendingPickupItem(
        String ticketId,
        String readerName,
        String firstBookTitle,
        LocalDate orderDate,
        String pickupDeadlineNote
    ) {}

    public record NewBookTileDto(
        String bookId,
        String title,
        String author,
        String coverPath,
        int availableCopies
    ) {}
}
