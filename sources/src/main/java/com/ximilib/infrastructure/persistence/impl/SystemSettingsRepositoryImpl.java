package com.ximilib.infrastructure.persistence.impl;

import com.ximilib.application.port.SystemSettingsPort;
import com.ximilib.infrastructure.logging.AuditLogger;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import java.util.List;

public class SystemSettingsRepositoryImpl extends BaseJdbcRepository implements SystemSettingsPort {

    public SystemSettingsRepositoryImpl(DatabaseConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    public List<Object[]> getAllSettings() {
        return queryObjectRows("SELECT khoa, giatri, mota FROM cauhinh ORDER BY khoa ASC", 3);
    }

    @Override
    public void updateSetting(String key, String value, String currentUserId) {
        executeUpdate("UPDATE cauhinh SET giatri = ? WHERE khoa = ?",
                value != null ? value.trim() : "", key);

        String user = (currentUserId != null && !currentUserId.isBlank()) ? currentUserId : "ADMIN";
        AuditLogger.log(user, "UPDATE_CONFIG", "Cập nhật tham số " + key + " = " + value);
    }
}
