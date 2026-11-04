package com.ximilib.infrastructure.logging;

import com.ximilib.infrastructure.config.AppContainer;
import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AuditLogger: Ghi nhật ký kiểm toán hệ thống vào logs/audit.log và CSDL MySQL bảng auditlog (DEV-04, SRS YC-07).
 * Ghi nhận các hành động quan trọng: đăng nhập, mượn/trả, báo mất, nộp phạt, xuất báo cáo.
 * Tuân thủ R-08: Tuyệt đối không ghi mật khẩu, token hay dữ liệu nhạy cảm.
 */
public class AuditLogger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "logs/audit.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static DatabaseConnectionManager customConnectionManager = null;

    static {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static void setConnectionManager(DatabaseConnectionManager cm) {
        customConnectionManager = cm;
    }

    public static synchronized void log(String userId, String action, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String safeUserId = (userId != null && !userId.isBlank()) ? userId : "ANONYMOUS";
        String logEntry = String.format("[%s] [AUDIT] [%s] [%s] %s", timestamp, safeUserId, action, details);

        System.out.println(logEntry);

        // 1. Ghi file logs/audit.log
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logEntry);
        } catch (IOException e) {
            System.err.println("Không thể ghi log kiểm toán ra file: " + e.getMessage());
        }

        // 2. Dual-write vào CSDL MySQL bảng auditlog (DEV-04)
        DatabaseConnectionManager cm = customConnectionManager;
        if (cm == null) {
            try {
                cm = AppContainer.getInstance().getConnectionManager();
            } catch (Throwable ignored) {}
        }

        if (cm != null) {
            try (Connection conn = cm.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO auditlog (user_id, hanhdong, chitiet, ip_address) VALUES (?, ?, ?, '127.0.0.1')")) {
                ps.setString(1, safeUserId);
                ps.setString(2, action != null ? action : "UNKNOWN");
                ps.setString(3, details != null ? details : "");
                ps.executeUpdate();
            } catch (Throwable ignored) {
                // Đảm bảo không làm gãy luồng chính nếu DB tạm thời bận
            }
        }
    }
}
