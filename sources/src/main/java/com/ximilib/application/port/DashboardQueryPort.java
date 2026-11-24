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

    default List<UserNotificationItem> getUserNotifications(String userId, int limit) {
        return java.util.Collections.emptyList();
    }

    default void markNotificationsAsRead(String userId) {}

    ReaderDashboardDto.ReaderSummary getReaderSummary(String userId);

    List<ReaderDashboardDto.ReaderActiveLoan> getReaderActiveLoans(String userId, LocalDate today);

    class UserNotificationItem {
        private final String notificationId;
        private final String title;
        private final String message;
        private final String createdAt;
        private final boolean warning;
        private final boolean read;

        public UserNotificationItem(String notificationId, String title, String message, String createdAt, boolean warning, boolean read) {
            this.notificationId = notificationId;
            this.title = title;
            this.message = message;
            this.createdAt = createdAt;
            this.warning = warning;
            this.read = read;
        }

        public String getNotificationId() { return notificationId; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getCreatedAt() { return createdAt; }
        public boolean isWarning() { return warning; }
        public boolean isRead() { return read; }
    }
}
