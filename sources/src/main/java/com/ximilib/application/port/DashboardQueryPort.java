package com.ximilib.application.port;

import com.ximilib.application.dto.ReaderDashboardDto;
import com.ximilib.application.dto.StaffDashboardDto;
import java.time.LocalDate;
import java.util.List;

public interface DashboardQueryPort {

    StaffDashboardDto.StaffKpiSummary getStaffKpiSummary();

    List<StaffDashboardDto.TodoLoanItem> getTodoLoans(LocalDate today, int limit);

    List<StaffDashboardDto.PendingPickupItem> getPendingPickups(int limit);

    List<StaffDashboardDto.NewBookTileDto> getNewBooks(int limit);

    int countUnreadNotifications(String userId);

    ReaderDashboardDto.ReaderSummary getReaderSummary(String userId);

    List<ReaderDashboardDto.ReaderActiveLoan> getReaderActiveLoans(String userId, LocalDate today);
}
