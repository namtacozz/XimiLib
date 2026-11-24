package com.ximilib.application.service;

import com.ximilib.application.dto.ReaderDashboardDto;
import com.ximilib.application.dto.StaffDashboardDto;
import com.ximilib.application.port.DashboardQueryPort;
import java.util.List;

public interface DashboardService {

    StaffDashboardDto.StaffKpiSummary getStaffKpiSummary();

    List<StaffDashboardDto.TodoLoanItem> getTodoLoans(int limit);

    List<StaffDashboardDto.PendingPickupItem> getPendingPickups(int limit);

    List<StaffDashboardDto.NewBookTileDto> getNewBooks(int limit);

    int getUnreadNotificationCount(String userId);

    List<DashboardQueryPort.UserNotificationItem> getUserNotifications(String userId, int limit);

    void markNotificationsAsRead(String userId);

    ReaderDashboardDto.ReaderDashboardData getReaderDashboardData(String userId);
}
