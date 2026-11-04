package com.ximilib.application.port;

import java.math.BigDecimal;
import java.util.List;

public interface ReportPort {

    record ReportKpiSummary(int totalBooks, int activeLoans, int overdueLoans, BigDecimal totalFines) {}

    ReportKpiSummary getKpiSummary();

    List<Object[]> getLoansReport();

    List<Object[]> getOverdueReport();

    List<Object[]> getDamagedBooksReport();

    List<Object[]> getTopBooksReport();

    record DailyMetric(String dateLabel, int borrowCount, BigDecimal fineRevenue) {}

    List<DailyMetric> getBorrowAndFineTrend(int days);

    void updateLoanStatus(String ticketId, int newStatusCode, String currentUserId) throws Exception;
}
