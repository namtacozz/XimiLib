package com.ximilib.application.dto;

import com.ximilib.domain.model.LoanDueClassification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ReaderDashboardDto: Dữ liệu vận hành Trang chủ dành cho Độc giả (Goodreads / Libby style).
 */
public class ReaderDashboardDto {

    public record ReaderSummary(
        String userId,
        String fullName,
        String cardNumber,
        boolean cardActive,
        boolean cardExpired,
        int activeLoansCount,
        int overdueLoansCount,
        BigDecimal unpaidFineAmount
    ) {
        public boolean hasViolationOrDebt() {
            return overdueLoansCount > 0 || (unpaidFineAmount != null && unpaidFineAmount.compareTo(BigDecimal.ZERO) > 0);
        }
    }

    public record ReaderActiveLoan(
        String ticketId,
        String detailId,
        String copyId,
        String bookId,
        String bookTitle,
        String authorName,
        String coverImage,
        LocalDate borrowDate,
        LocalDate dueDate,
        boolean isRenewed,
        boolean canRenew,
        String renewDisabledReason,
        LoanDueClassification classification
    ) {}

    public record ReaderDashboardData(
        ReaderSummary summary,
        List<ReaderActiveLoan> activeLoans,
        List<StaffDashboardDto.NewBookTileDto> newBooks
    ) {}
}
