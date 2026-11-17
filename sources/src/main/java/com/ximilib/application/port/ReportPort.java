package com.ximilib.application.port;

import com.ximilib.application.dto.ReportFilterCriteria;
import java.math.BigDecimal;
import java.util.List;

public interface ReportPort {

    record ReportKpiSummary(int totalBooks, int activeLoans, int overdueLoans, BigDecimal totalFines) {}

    ReportKpiSummary getKpiSummary();

    List<Object[]> getLoansReport();

    default List<Object[]> getLoansReport(ReportFilterCriteria criteria) {
        return getLoansReport();
    }

    List<Object[]> getOverdueReport();

    default List<Object[]> getOverdueReport(ReportFilterCriteria criteria) {
        return getOverdueReport();
    }

    List<Object[]> getDamagedBooksReport();

    default List<Object[]> getDamagedBooksReport(ReportFilterCriteria criteria) {
        return getDamagedBooksReport();
    }

    List<Object[]> getTopBooksReport();

    default List<Object[]> getTopBooksReport(ReportFilterCriteria criteria) {
        return getTopBooksReport();
    }

    record DailyMetric(String dateLabel, int borrowCount, BigDecimal fineRevenue) {}

    List<DailyMetric> getBorrowAndFineTrend(int days);

    void updateLoanStatus(String ticketId, int newStatusCode, String currentUserId) throws Exception;
}
