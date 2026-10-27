package com.ximilib.infrastructure.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AuditLogger: Ghi nhật ký kiểm toán hệ thống vào logs/audit.log và console.
 * Ghi nhận các hành động quan trọng: đăng nhập, mượn/trả, báo mất, nộp phạt, xuất báo cáo.
 * Tuân thủ R-08: Tuyệt đối không ghi mật khẩu, token hay dữ liệu nhạy cảm.
 */
public class AuditLogger {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "logs/audit.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static synchronized void log(String userId, String action, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String safeUserId = (userId != null && !userId.isBlank()) ? userId : "ANONYMOUS";
        String logEntry = String.format("[%s] [AUDIT] [%s] [%s] %s", timestamp, safeUserId, action, details);

        System.out.println(logEntry);

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(logEntry);
        } catch (IOException e) {
            System.err.println("Không thể ghi log kiểm toán: " + e.getMessage());
        }
    }
}
