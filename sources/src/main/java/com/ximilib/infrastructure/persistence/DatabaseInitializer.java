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

/**
 * DatabaseInitializer: Khởi tạo CSDL bằng cách chạy các script migration V1–V3.
 * <p>
 * Logic kiểm tra:
 * <ul>
 *   <li>count &lt; 1 → chạy V1 → V2 → V3</li>
 *   <li>1 ≤ count &lt; 20 → bỏ V1, chạy V2 → V3 (V1 đã chạy một phần)</li>
 *   <li>count &gt;= 20 → bỏ qua toàn bộ migration</li>
 * </ul>
 * Script được đọc từ file system (đường dẫn tương đối so với thư mục làm việc).
 * Ghi log ra {@code logs/db-init-<timestamp>.log}.
 */
public class DatabaseInitializer {

    private static final String V1_PATH = "db/migrations/V1__initial_schema.sql";
    private static final String V2_PATH = "db/migrations/V2__srs_schema_updates.sql";
    private static final String V3_PATH = "db/migrations/V3__enforce_bcrypt_passwords.sql";

    private static final DateTimeFormatter LOG_TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss");

    private final DatabaseConnectionManager connectionManager;

    public DatabaseInitializer(DatabaseConnectionManager connectionManager) {
        this.connectionManager = java.util.Objects.requireNonNull(connectionManager,
                "DatabaseConnectionManager không được null");
    }

    /**
     * Thực hiện kiểm tra và migration CSDL nếu cần.
     * Toàn bộ thông tin được ghi ra log file trong thư mục {@code logs/}.
     *
     * @throws RuntimeException nếu không thể kết nối CSDL hoặc migration thất bại
     */
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
            try (Connection conn = connectionManager.getConnection()) {
                tableCount = countTables(conn);
            } catch (SQLException e) {
                String msg = "Không thể kết nối CSDL để kiểm tra trạng thái: " + e.getMessage();
                log("[LỖI] " + msg, log);
                throw new RuntimeException(msg, e);
            }

            log("[INFO] Số bảng hiện có trong CSDL: " + tableCount, log);

            if (tableCount >= 20) {
                log("[INFO] CSDL đã được khởi tạo trước đó (" + tableCount + " bảng). Bỏ qua migration.", log);
                log("=== DatabaseInitializer KẾT THÚC ===", log);
                return;
            }

            // Xác định danh sách script cần chạy
            List<String> scriptsToRun = new ArrayList<>();
            if (tableCount == 0) {
                log("[INFO] CSDL trống, chạy V1 → V2 → V3.", log);
                scriptsToRun.add(V1_PATH);
                scriptsToRun.add(V2_PATH);
                scriptsToRun.add(V3_PATH);
            } else {
                log("[WARNING] CSDL có " + tableCount + " bảng (không đủ 20). Bỏ qua V1, chạy V2 → V3.", log);
                scriptsToRun.add(V2_PATH);
                scriptsToRun.add(V3_PATH);
            }

            // Chạy từng script trên một connection riêng
            for (String scriptPath : scriptsToRun) {
                log("[INFO] Bắt đầu chạy script: " + scriptPath, log);
                runScript(scriptPath, log);
                log("[OK] Hoàn thành script: " + scriptPath, log);
            }

            log("=== DatabaseInitializer HOÀN TẤT THÀNH CÔNG ===", log);

        } catch (IOException e) {
            // Không thể ghi log → vẫn tiếp tục, không ném exception vì log chỉ là thông tin phụ
            System.err.println("[DatabaseInitializer] Không thể ghi log: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

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

        // Tách thành từng statement (DDL/DML) theo dấu ;
        String[] rawStatements = content.split(";\\s*(?:\\r?\\n|$)");

        boolean fkChecksDisabled = false;
        try (Connection conn = connectionManager.getConnection()) {
            // Tắt autocommit để có thể rollback toàn bộ script khi lỗi
            conn.setAutoCommit(false);

            try (Statement st = conn.createStatement()) {
                for (String raw : rawStatements) {
                    String stmt = raw.trim();
                    if (stmt.isEmpty() || stmt.startsWith("--") || stmt.startsWith("/*")) {
                        continue;
                    }

                    // Theo dõi FOREIGN_KEY_CHECKS = 0 để đảm bảo khôi phục trong finally
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
                try { conn.rollback(); } catch (SQLException ex) { /* bỏ qua */ }
                String msg = "Script " + scriptPath + " thất bại: " + e.getMessage();
                log("[LỖI] " + msg, log);
                throw new RuntimeException(msg, e);
            } finally {
                // Đảm bảo FOREIGN_KEY_CHECKS luôn được khôi phục = 1 nếu bị tắt
                if (fkChecksDisabled) {
                    try (Statement st2 = conn.createStatement()) {
                        st2.execute("SET FOREIGN_KEY_CHECKS = 1");
                        log("[INFO] Đã khôi phục FOREIGN_KEY_CHECKS = 1", log);
                    } catch (SQLException ex) {
                        log("[WARNING] Không thể khôi phục FOREIGN_KEY_CHECKS: " + ex.getMessage(), log);
                    }
                }
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* bỏ qua */ }
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
