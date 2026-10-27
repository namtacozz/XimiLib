package com.ximilib.application.service.impl;

import com.ximilib.application.dto.ReaderDashboardDto;
import com.ximilib.application.dto.StaffDashboardDto;
import com.ximilib.application.service.DashboardService;
import com.ximilib.domain.repository.DashboardRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * DashboardServiceImpl: Thực thi nghiệp vụ nạp dữ liệu Trang chủ.
 */
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardServiceImpl(DashboardRepository repository) {
        this.dashboardRepository = repository;
    }

    @Override
    public StaffDashboardDto.StaffKpiSummary getStaffKpiSummary() {
        return dashboardRepository.getStaffKpiSummary();
    }

    @Override
    public List<StaffDashboardDto.TodoLoanItem> getTodoLoans(int limit) {
        int max = limit > 0 ? limit : 5;
        return dashboardRepository.getTodoLoans(LocalDate.now(), max);
    }

    @Override
    public List<StaffDashboardDto.PendingPickupItem> getPendingPickups(int limit) {
        int max = limit > 0 ? limit : 5;
        return dashboardRepository.getPendingPickups(max);
    }

    @Override
    public List<StaffDashboardDto.NewBookTileDto> getNewBooks(int limit) {
        int max = limit > 0 ? limit : 6;
        return dashboardRepository.getNewBooks(max);
    }

    @Override
    public int getUnreadNotificationCount(String userId) {
        if (userId == null || userId.isBlank()) {
            return 0;
        }
        return dashboardRepository.countUnreadNotifications(userId);
    }

    @Override
    public ReaderDashboardDto.ReaderDashboardData getReaderDashboardData(String userId) {
        if (userId == null || userId.isBlank()) {
            return new ReaderDashboardDto.ReaderDashboardData(
                new ReaderDashboardDto.ReaderSummary("", "", "Chưa cấp thẻ", false, false, 0, 0, java.math.BigDecimal.ZERO),
                Collections.emptyList(),
                dashboardRepository.getNewBooks(6)
            );
        }

        ReaderDashboardDto.ReaderSummary summary = dashboardRepository.getReaderSummary(userId);
        List<ReaderDashboardDto.ReaderActiveLoan> activeLoans = dashboardRepository.getReaderActiveLoans(userId, LocalDate.now());
        List<StaffDashboardDto.NewBookTileDto> newBooks = dashboardRepository.getNewBooks(6);

        return new ReaderDashboardDto.ReaderDashboardData(summary, activeLoans, newBooks);
    }
}
