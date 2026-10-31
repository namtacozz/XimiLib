package com.ximilib.application.service;

import com.ximilib.application.dto.ReaderDashboardDto;
import com.ximilib.application.dto.StaffDashboardDto;
import com.ximilib.application.port.DashboardQueryPort;
import com.ximilib.application.service.impl.DashboardServiceImpl;
import com.ximilib.domain.model.LoanDueClassification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Test: DashboardService")
class DashboardServiceTest {

    private DashboardQueryPort fakeRepository;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        fakeRepository = new DashboardQueryPort() {
            @Override
            public StaffDashboardDto.StaffKpiSummary getStaffKpiSummary() {
                return new StaffDashboardDto.StaffKpiSummary(221, 663, 10, 26, 2);
            }

            @Override
            public List<StaffDashboardDto.TodoLoanItem> getTodoLoans(LocalDate today, int limit) {
                return List.of(
                    new StaffDashboardDto.TodoLoanItem("PM001", "Nguyễn Văn A", "Clean Architecture", 1, today, LoanDueClassification.classify(today, today))
                );
            }

            @Override
            public List<StaffDashboardDto.PendingPickupItem> getPendingPickups(int limit) {
                return List.of(
                    new StaffDashboardDto.PendingPickupItem("PM002", "Trần Thị B", "Dế Mèn Phiêu Lưu Ký", today, "Chờ nhận tại quầy")
                );
            }

            @Override
            public List<StaffDashboardDto.NewBookTileDto> getNewBooks(int limit) {
                return List.of(
                    new StaffDashboardDto.NewBookTileDto("S001", "Nhà Giả Kim", "Paulo Coelho", "cover1.jpg", 3)
                );
            }

            @Override
            public int countUnreadNotifications(String userId) {
                return "ND001".equals(userId) ? 3 : 0;
            }

            @Override
            public ReaderDashboardDto.ReaderSummary getReaderSummary(String userId) {
                return new ReaderDashboardDto.ReaderSummary(userId, "Độc giả Test", "TV2026-001", true, false, 2, 1, new BigDecimal("10000"));
            }

            @Override
            public List<ReaderDashboardDto.ReaderActiveLoan> getReaderActiveLoans(String userId, LocalDate today) {
                return List.of(
                    new ReaderDashboardDto.ReaderActiveLoan(
                        "PM001", "CT001", "CS001", "S001", "Nhà Giả Kim", "Paulo Coelho", "cover1.jpg",
                        today.minusDays(7), today.plusDays(7), false, true, "",
                        LoanDueClassification.classify(today.plusDays(7), today)
                    )
                );
            }
        };

        dashboardService = new DashboardServiceImpl(fakeRepository);
    }

    private final LocalDate today = LocalDate.now();

    @Test
    @DisplayName("Staff KPI Summary phản ánh đúng dữ liệu từ repository")
    void testStaffKpiSummary() {
        StaffDashboardDto.StaffKpiSummary kpi = dashboardService.getStaffKpiSummary();
        assertNotNull(kpi);
        assertEquals(221, kpi.totalTitles());
        assertEquals(663, kpi.availableCopies());
        assertEquals(10, kpi.activeBorrowCopies());
        assertEquals(26, kpi.activeReaders());
        assertEquals(2, kpi.overdueLoans());
    }

    @Test
    @DisplayName("Reader Dashboard nạp đầy đủ Summary, Active Loans và New Books")
    void testReaderDashboardData() {
        ReaderDashboardDto.ReaderDashboardData data = dashboardService.getReaderDashboardData("ND004");
        assertNotNull(data);
        assertNotNull(data.summary());
        assertEquals("Độc giả Test", data.summary().fullName());
        assertTrue(data.summary().hasViolationOrDebt());
        assertEquals(1, data.activeLoans().size());
        assertTrue(data.activeLoans().get(0).canRenew());
        assertEquals(1, data.newBooks().size());
    }

    @Test
    @DisplayName("Đếm số thông báo chưa đọc chính xác theo userId")
    void testUnreadNotificationCount() {
        assertEquals(3, dashboardService.getUnreadNotificationCount("ND001"));
        assertEquals(0, dashboardService.getUnreadNotificationCount("ND999"));
        assertEquals(0, dashboardService.getUnreadNotificationCount(null));
    }
}
