package com.ximilib.infrastructure.persistence;

import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {

    private static final String V1_PATH = "db/migrations/V1__initial_schema.sql";
    private static final String V2_PATH = "db/migrations/V2__srs_schema_updates.sql";
    private static final String V3_PATH = "db/migrations/V3__enforce_bcrypt_passwords.sql";
    private static final String V4_PATH = "db/migrations/V4__report_and_search_indexes.sql";
    private static final String V5_PATH = "db/migrations/V5__srs_alignment_and_audit.sql";

    private static final DateTimeFormatter LOG_TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");

    private final DatabaseConnectionManager connectionManager;

    public DatabaseInitializer(DatabaseConnectionManager connectionManager) {
        this.connectionManager = java.util.Objects.requireNonNull(connectionManager,
                "DatabaseConnectionManager không được null");
    }

    public void initialize() {
        String logFileName = "logs/db-init-" + LocalDateTime.now().format(LOG_TS_FMT) + ".log";
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        try (PrintWriter log = new PrintWriter(new BufferedWriter(
                new FileWriter(logFileName, StandardCharsets.UTF_8, true)))) {

            log("=== DatabaseInitializer BẮT ĐẦU: " + LocalDateTime.now() + " ===", log);

            int tableCount;
            boolean hasAudit;
            try (Connection conn = connectionManager.getConnection()) {
                tableCount = countTables(conn);
                hasAudit = hasTable(conn, "auditlog");
            } catch (SQLException e) {
                String msg = "Không thể kết nối CSDL để kiểm tra trạng thái: " + e.getMessage();
                log("[LỖI] " + msg, log);
                throw new RuntimeException(msg, e);
            }

            log("[INFO] Số bảng hiện có trong CSDL: " + tableCount, log);

            if (tableCount >= 24 && hasAudit) {
                log("[INFO] CSDL đã được khởi tạo đầy đủ (" + tableCount + " bảng). Bỏ qua migration.", log);
                log("=== DatabaseInitializer KẾT THÚC ===", log);
                return;
            }

            List<String> scriptsToRun = new ArrayList<>();
            if (tableCount == 0) {
                log("[INFO] CSDL trống, chạy V1 → V2 → V3 → V4 → V5.", log);
                scriptsToRun.add(V1_PATH);
                scriptsToRun.add(V2_PATH);
                scriptsToRun.add(V3_PATH);
                scriptsToRun.add(V4_PATH);
                scriptsToRun.add(V5_PATH);
            } else if (!hasAudit) {
                log("[INFO] CSDL cần nâng cấp Migration V5 (SRS alignment & auditlog). Đang chạy V5.", log);
                scriptsToRun.add(V5_PATH);
            } else {
                log("[WARNING] CSDL có " + tableCount + " bảng (không đủ 24). Chạy V2 → V3 → V4 → V5.", log);
                scriptsToRun.add(V2_PATH);
                scriptsToRun.add(V3_PATH);
                scriptsToRun.add(V4_PATH);
                scriptsToRun.add(V5_PATH);
            }

            for (String scriptPath : scriptsToRun) {
                log("[INFO] Bắt đầu chạy script: " + scriptPath, log);
                runScript(scriptPath, log);
                log("[OK] Hoàn thành script: " + scriptPath, log);
            }

            log("=== DatabaseInitializer HOÀN TẤT THÀNH CÔNG ===", log);

        } catch (IOException e) {

            System.err.println("[DatabaseInitializer] Không thể ghi log: " + e.getMessage());
        }
    }

    private int countTables(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    private boolean hasTable(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = '" + tableName + "'";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private void runScript(String scriptPath, PrintWriter log) {
        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists()) {
            String msg = "Không tìm thấy file script: " + scriptFile.getAbsolutePath();
            log("[LỖI] " + msg, log);
            throw new RuntimeException(msg);
        }

        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(scriptPath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            String msg = "Không thể đọc file script " + scriptPath + ": " + e.getMessage();
            log("[LỖI] " + msg, log);
            throw new RuntimeException(msg, e);
        }

        String[] rawStatements = content.split(";\\s*(?:\\r?\\n|$)");

        boolean fkChecksDisabled = false;
        try (Connection conn = connectionManager.getConnection()) {

            conn.setAutoCommit(false);

            try (Statement st = conn.createStatement()) {
                for (String raw : rawStatements) {
                    String stmt = raw.trim();
                    if (stmt.isEmpty() || stmt.startsWith("--") || stmt.startsWith("/*")) {
                        continue;
                    }

                    String lower = stmt.toLowerCase();
                    if (lower.contains("foreign_key_checks") && lower.contains("0")) {
                        fkChecksDisabled = true;
                    } else if (lower.contains("foreign_key_checks") && lower.contains("1")) {
                        fkChecksDisabled = false;
                    }

                    log("[SQL] " + stmt.replaceAll("\\s+", " ").substring(0,
                            Math.min(100, stmt.replaceAll("\\s+", " ").length())) + "...", log);
                    st.execute(stmt);
                }

                conn.commit();

            } catch (SQLException e) {
                try { conn.rollback(); } catch (SQLException ex) {   }
                String msg = "Script " + scriptPath + " thất bại: " + e.getMessage();
                log("[LỖI] " + msg, log);
                throw new RuntimeException(msg, e);
            } finally {

                if (fkChecksDisabled) {
                    try (Statement st2 = conn.createStatement()) {
                        st2.execute("SET FOREIGN_KEY_CHECKS = 1");
                        log("[INFO] Đã khôi phục FOREIGN_KEY_CHECKS = 1", log);
                    } catch (SQLException ex) {
                        log("[WARNING] Không thể khôi phục FOREIGN_KEY_CHECKS: " + ex.getMessage(), log);
                    }
                }
                try { conn.setAutoCommit(true); } catch (SQLException ex) {   }
            }

        } catch (SQLException e) {
            String msg = "Không thể lấy kết nối khi chạy script " + scriptPath + ": " + e.getMessage();
            log("[LỖI] " + msg, log);
            throw new RuntimeException(msg, e);
        }
    }

    private void log(String message, PrintWriter writer) {
        String line = "[" + LocalDateTime.now() + "] " + message;
        System.out.println(line);
        if (writer != null) {
            writer.println(line);
            writer.flush();
        }
    }
}
