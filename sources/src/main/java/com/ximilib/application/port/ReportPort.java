package com.ximilib.application.port;

import java.math.BigDecimal;
import java.util.List;

/**
 * ReportPort: Cổng phục vụ Báo cáo, Thống kê & Quản lý trạng thái phiếu mượn (SRS UC-7, F-12).
 * Tên port trung tính, bao gồm cả truy vấn thống kê và thao tác cập nhật trạng thái phiếu mượn.
 */
public interface ReportPort {

    /**
     * Dữ liệu 4 chỉ số KPI tổng quan trên màn hình báo cáo.
     */
    record ReportKpiSummary(int totalBooks, int activeLoans, int overdueLoans, BigDecimal totalFines) {}

    /**
     * Lấy tổng hợp 4 chỉ số KPI: Tổng đầu sách, Đang cho mượn, Quá hạn, Tiền phạt đã thu.
     *
     * @return Dữ liệu KPI
     */
    ReportKpiSummary getKpiSummary();

    /**
     * Báo cáo 1: Thống kê 100 lượt mượn/trả sách mới nhất.
     */
    List<Object[]> getLoansReport();

    /**
     * Báo cáo 2: Danh sách 100 độc giả quá hạn và nợ phạt tồn đọng.
     */
    List<Object[]> getOverdueReport();

    /**
     * Báo cáo 3: Danh sách 100 cuốn sách bị hỏng hoặc mất cần thanh lý.
     */
    List<Object[]> getDamagedBooksReport();

    /**
     * Báo cáo 4: Top 10 đầu sách được mượn nhiều nhất.
     */
    List<Object[]> getTopBooksReport();

    /**
     * Dữ liệu thống kê theo mốc thời gian (lượt mượn sách và doanh thu phạt) phục vụ biểu đồ Dashboard (UC-7).
     */
    record DailyMetric(String dateLabel, int borrowCount, BigDecimal fineRevenue) {}

    /**
     * Lấy dữ liệu thống kê xu hướng lượt mượn và doanh thu phạt trong N ngày gần nhất.
     *
     * @param days Số ngày cần lấy (ví dụ: 7 hoặc 30 ngày)
     * @return Danh sách các mốc thống kê theo ngày tăng dần
     */
    List<DailyMetric> getBorrowAndFineTrend(int days);

    /**
     * Cập nhật trạng thái phiếu mượn kèm transaction đồng bộ trạng thái cuốn sách và ghi audit log.
     *
     * @param ticketId      Mã phiếu mượn
     * @param newStatusCode Trạng thái mới (0..6)
     * @param currentUserId Mã người dùng thực hiện thao tác
     * @throws Exception nếu có lỗi trong quá trình thực hiện transaction
     */
    void updateLoanStatus(String ticketId, int newStatusCode, String currentUserId) throws Exception;
}
