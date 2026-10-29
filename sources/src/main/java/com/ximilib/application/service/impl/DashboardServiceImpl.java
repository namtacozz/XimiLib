package com.ximilib.application.service.impl;

import com.ximilib.application.dto.ReaderDashboardDto;
import com.ximilib.application.dto.StaffDashboardDto;
import com.ximilib.application.port.DashboardQueryPort;
import com.ximilib.application.service.DashboardService;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * DashboardServiceImpl: Thực thi nghiệp vụ nạp dữ liệu Trang chủ.
 */
public class DashboardServiceImpl implements DashboardService {

    private final DashboardQueryPort dashboardQueryPort;

    public DashboardServiceImpl(DashboardQueryPort queryPort) {
        this.dashboardQueryPort = Objects.requireNonNull(queryPort, "dashboardQueryPort không được null");
    }

    @Override
    public StaffDashboardDto.StaffKpiSummary getStaffKpiSummary() {
        return dashboardQueryPort.getStaffKpiSummary();
    }

    @Override
    public List<StaffDashboardDto.TodoLoanItem> getTodoLoans(int limit) {
        int max = limit > 0 ? limit : 5;
        return dashboardQueryPort.getTodoLoans(LocalDate.now(), max);
    }

    @Override
    public List<StaffDashboardDto.PendingPickupItem> getPendingPickups(int limit) {
        int max = limit > 0 ? limit : 5;
        return dashboardQueryPort.getPendingPickups(max);
    }

    @Override
    public List<StaffDashboardDto.NewBookTileDto> getNewBooks(int limit) {
        int max = limit > 0 ? limit : 6;
        return dashboardQueryPort.getNewBooks(max);
    }

    @Override
    public int getUnreadNotificationCount(String userId) {
        if (userId == null || userId.isBlank()) {
            return 0;
        }
        return dashboardQueryPort.countUnreadNotifications(userId);
    }

    @Override
    public ReaderDashboardDto.ReaderDashboardData getReaderDashboardData(String userId) {
        if (userId == null || userId.isBlank()) {
            return new ReaderDashboardDto.ReaderDashboardData(
                new ReaderDashboardDto.ReaderSummary("", "", "Chưa cấp thẻ", false, false, 0, 0, java.math.BigDecimal.ZERO),
                Collections.emptyList(),
                dashboardQueryPort.getNewBooks(6)
            );
        }

        ReaderDashboardDto.ReaderSummary summary = dashboardQueryPort.getReaderSummary(userId);
        List<ReaderDashboardDto.ReaderActiveLoan> activeLoans = dashboardQueryPort.getReaderActiveLoans(userId, LocalDate.now());
        List<StaffDashboardDto.NewBookTileDto> newBooks = dashboardQueryPort.getNewBooks(6);

        return new ReaderDashboardDto.ReaderDashboardData(summary, activeLoans, newBooks);
    }
}
