package com.ximilib.application.port;

import java.util.List;

public interface SystemSettingsPort {

    List<Object[]> getAllSettings();

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
