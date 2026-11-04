package com.ximilib.application.port;

import java.util.List;

/**
 * SystemSettingsPort: Cổng cấu hình tham số quy định nghiệp vụ thư viện (SRS & Bảng cauhinh).
 * Tuân thủ Clean Architecture (R-01).
 */
public interface SystemSettingsPort {

    /**
     * Lấy toàn bộ danh sách tham số cấu hình hệ thống.
     *
     * @return Danh sách các dòng dữ liệu Object[]: [khoa, giatri, mota]
     */
    List<Object[]> getAllSettings();

    /**
     * Cập nhật giá trị của một tham số cấu hình và ghi nhận Audit log.
     *
     * @param key           Khóa tham số cấu hình
     * @param value         Giá trị mới
     * @param currentUserId Mã người dùng thực hiện cập nhật
     */
    void updateSetting(String key, String value, String currentUserId);

    default int getInt(String key, int defaultValue) {
        for (Object[] row : getAllSettings()) {
            if (key.equalsIgnoreCase((String) row[0])) {
                try {
                    return Integer.parseInt(((String) row[1]).trim());
                } catch (Exception ignored) {}
            }
        }
        return defaultValue;
    }

    default java.math.BigDecimal getBigDecimal(String key, java.math.BigDecimal defaultValue) {
        for (Object[] row : getAllSettings()) {
            if (key.equalsIgnoreCase((String) row[0])) {
                try {
                    return new java.math.BigDecimal(((String) row[1]).trim());
                } catch (Exception ignored) {}
            }
        }
        return defaultValue;
    }
}
