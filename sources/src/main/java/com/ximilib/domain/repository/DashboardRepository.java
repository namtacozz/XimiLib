package com.ximilib.domain.repository;

import com.ximilib.application.dto.ReaderDashboardDto;
import com.ximilib.application.dto.StaffDashboardDto;
import java.time.LocalDate;
import java.util.List;

/**
 * DashboardRepository: Hợp đồng truy vấn dữ liệu vận hành Trang chủ từ MySQL.
 * Tuân thủ Clean Architecture (Domain interface, không import SQL/JDBC/Swing - R-01).
 */
public interface DashboardRepository {

    StaffDashboardDto.StaffKpiSummary getStaffKpiSummary();

    List<StaffDashboardDto.TodoLoanItem> getTodoLoans(LocalDate today, int limit);

    List<StaffDashboardDto.PendingPickupItem> getPendingPickups(int limit);

    List<StaffDashboardDto.NewBookTileDto> getNewBooks(int limit);

    int countUnreadNotifications(String userId);

    ReaderDashboardDto.ReaderSummary getReaderSummary(String userId);

    List<ReaderDashboardDto.ReaderActiveLoan> getReaderActiveLoans(String userId, LocalDate today);
}
