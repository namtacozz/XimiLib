package com.ximilib.application.service;

import com.ximilib.application.dto.ReaderDashboardDto;
import com.ximilib.application.dto.StaffDashboardDto;
import java.time.LocalDate;
import java.util.List;

/**
 * DashboardService: Cung cấp dữ liệu vận hành Trang chủ cho Thủ thư/Admin và Độc giả.
 * Tuân thủ R-01, R-02 (Logic nghiệp vụ và điều phối nằm ở tầng Application).
 */
public interface DashboardService {

    StaffDashboardDto.StaffKpiSummary getStaffKpiSummary();

    List<StaffDashboardDto.TodoLoanItem> getTodoLoans(int limit);

    List<StaffDashboardDto.PendingPickupItem> getPendingPickups(int limit);

    List<StaffDashboardDto.NewBookTileDto> getNewBooks(int limit);

    int getUnreadNotificationCount(String userId);

    ReaderDashboardDto.ReaderDashboardData getReaderDashboardData(String userId);
}
