package com.ximilib.application.service;

import com.ximilib.application.dto.ReaderDashboardDto;
import com.ximilib.application.dto.StaffDashboardDto;
import java.util.List;

public interface DashboardService {

    StaffDashboardDto.StaffKpiSummary getStaffKpiSummary();

    List<StaffDashboardDto.TodoLoanItem> getTodoLoans(int limit);

    List<StaffDashboardDto.PendingPickupItem> getPendingPickups(int limit);

    List<StaffDashboardDto.NewBookTileDto> getNewBooks(int limit);

    int getUnreadNotificationCount(String userId);

    ReaderDashboardDto.ReaderDashboardData getReaderDashboardData(String userId);
}
