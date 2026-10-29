package com.ximilib.domain.repository;

/**
 * DashboardRepository: Hợp đồng lưu trữ và truy vấn thông tin nghiệp vụ vận hành Trang chủ từ Domain.
 * Tuân thủ Clean Architecture (Domain interface, không import DTO/SQL/JDBC/Swing - R-01).
 */
public interface DashboardRepository {

    int countUnreadNotifications(String userId);
}
