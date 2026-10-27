package com.ximilib.infrastructure.jobs;

import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.infrastructure.logging.AuditLogger;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * BackgroundJobManager: Quản lý các tác vụ nền định kỳ của hệ thống (SRS Mục 3.2.7 & Non-functional).
 * Chạy daemon thread quét cơ sở dữ liệu:
 * 1. Đánh dấu phiếu mượn QUÁ HẠN (Trangthai 1 -> 3) khi Hantranhan < NOW().
 * 2. Hủy đặt chỗ giữ sách (datcho) quá hạn 24 giờ chưa đến lấy.
 * Tuân thủ R-05, R-06, R-08.
 */
public class BackgroundJobManager {

    private static BackgroundJobManager instance;
    private final ScheduledExecutorService scheduler;
    private final DatabaseConnectionManager connectionManager;

    private BackgroundJobManager() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "XimiLib-BackgroundJob");
            t.setDaemon(true);
            return t;
        });
        this.connectionManager = AppContainer.getInstance().getConnectionManager();
    }

    public static synchronized BackgroundJobManager getInstance() {
        if (instance == null) {
            instance = new BackgroundJobManager();
        }
        return instance;
    }

    public void start() {
        // Chạy ngay sau 5 giây khởi động, sau đó lặp lại mỗi 1 giờ
        scheduler.scheduleAtFixedRate(this::runDailyMaintenance, 5, 3600, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void runDailyMaintenance() {
        try (Connection conn = connectionManager.getConnection()) {
            // 1. Quét quá hạn phiếu mượn
            String updateOverdueSql = "UPDATE phieumuon SET Trangthai = 3 WHERE Trangthai = 1 AND Hantranhan < NOW()";
            int overdueCount;
            try (PreparedStatement ps = conn.prepareStatement(updateOverdueSql)) {
                overdueCount = ps.executeUpdate();
            }

            // 2. Quét hủy đặt chỗ quá 24h
            String cancelReservationSql = "UPDATE datcho SET Trangthai = 'DA_HUY' WHERE Trangthai = 'CHO_LAY' AND Ngayhethan < NOW()";
            int cancelCount = 0;
            try (PreparedStatement ps = conn.prepareStatement(cancelReservationSql)) {
                cancelCount = ps.executeUpdate();
            } catch (SQLException ignored) {
                // Bảng datcho có thể chưa phát sinh dữ liệu
            }

            if (overdueCount > 0 || cancelCount > 0) {
                AuditLogger.log("SYSTEM", "BACKGROUND_JOB",
                    String.format("Bảo trì định kỳ: Cập nhật %d phiếu quá hạn, hủy %d đặt chỗ hết hạn.", overdueCount, cancelCount));
            }
        } catch (Exception e) {
            System.err.println("Lỗi tác vụ nền bảo trì: " + e.getMessage());
        }
    }
}
