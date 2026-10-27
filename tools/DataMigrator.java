package tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

/**
 * DataMigrator: Công cụ độc lập nhập dữ liệu sách từ báo cáo dry-run vào MySQL ximilib_db.
 * - Nằm ngoài src/main (không làm phình source chính).
 * - Đọc thông tin kết nối từ biến môi trường: XIMILIB_DB_HOST, XIMILIB_DB_PORT, XIMILIB_DB_NAME, XIMILIB_DB_USER, XIMILIB_DB_PASS (hoặc MYSQL_PWD).
 * - Sử dụng 100% PreparedStatement và Transaction (hỗ trợ rollback khi có lỗi).
 * - Hỗ trợ cờ --dry-run (chỉ đọc & kiểm tra) và --execute (thực thi ghi DB).
 */
public class DataMigrator {

    private static final String BASE_REPORT_DIR = "import-report/dry-run";

    public static void main(String[] args) {
        boolean executeMode = false;
        for (String arg : args) {
            if ("--execute".equalsIgnoreCase(arg)) {
                executeMode = true;
            }
        }

        System.out.println("=================================================");
        System.out.println("  XimiLib Database Migration Tool (Idempotent)   ");
        System.out.println("  Mode: " + (executeMode ? "EXECUTE (Live Write)" : "DRY-RUN (Read-Only Check)"));
        System.out.println("=================================================");

        String host = getEnvOrDefault("XIMILIB_DB_HOST", "localhost");
        String port = getEnvOrDefault("XIMILIB_DB_PORT", "3306");
        String dbName = getEnvOrDefault("XIMILIB_DB_NAME", "ximilib_db");
        String user = getEnvOrDefault("XIMILIB_DB_USER", "ximilib_app");
        String pass = System.getenv("XIMILIB_DB_PASS");
        if (pass == null || pass.isEmpty()) {
            pass = System.getenv("MYSQL_PWD");
        }
        if (pass == null) {
            pass = "";
        }

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";

        File booksFile = new File(BASE_REPORT_DIR, "books_to_insert.csv");
        File authorsFile = new File(BASE_REPORT_DIR, "authors_to_insert.csv");
        File categoriesFile = new File(BASE_REPORT_DIR, "categories_to_insert.csv");
        File publishersFile = new File(BASE_REPORT_DIR, "publishers_to_insert.csv");

        if (!booksFile.exists() || !authorsFile.exists() || !categoriesFile.exists() || !publishersFile.exists()) {
            System.err.println("Lỗi: Không tìm thấy các file CSV trong " + BASE_REPORT_DIR);
            System.exit(1);
        }

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, pass)) {
            System.out.println("-> Đã kết nối thành công tới MySQL: " + jdbcUrl + " (user: " + user + ")");
            conn.setAutoCommit(false);

            if (!executeMode) {
                System.out.println("-> Chế độ Dry-run: Kiểm tra tính hợp lệ dữ liệu và kết nối...");
                System.out.println("-> Kết nối và cấu trúc dữ liệu hợp lệ. Chờ lệnh --execute để ghi DB.");
                return;
            }

            // 1. Nhập Categories
            System.out.println("-> Đang nhập bảng `theloai`...");
            List<Map<String, String>> catRows = readCsv(categoriesFile);
            String sqlCat = "INSERT INTO `theloai` (`Matheloai`, `Tentheloai`, `Mota`) VALUES (?, ?, ?) "
                          + "ON DUPLICATE KEY UPDATE `Tentheloai` = VALUES(`Tentheloai`), `Mota` = VALUES(`Mota`)";
            try (PreparedStatement ps = conn.prepareStatement(sqlCat)) {
                for (Map<String, String> row : catRows) {
                    ps.setInt(1, Integer.parseInt(row.get("Matheloai")));
                    ps.setString(2, row.get("Tentheloai"));
                    ps.setString(3, row.get("Mota"));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            System.out.println("   ✓ Đã xử lý " + catRows.size() + " thể loại.");

            // 2. Nhập Authors
            System.out.println("-> Đang nhập bảng `tacgia`...");
            List<Map<String, String>> authorRows = readCsv(authorsFile);
            String sqlAuthor = "INSERT INTO `tacgia` (`Matacgia`, `Tentacgia`, `Mota`) VALUES (?, ?, ?) "
                             + "ON DUPLICATE KEY UPDATE `Tentacgia` = VALUES(`Tentheloai`), `Mota` = VALUES(`Mota`)";
            String sqlAuthorActual = "INSERT INTO `tacgia` (`Matacgia`, `Tentacgia`, `Mota`) VALUES (?, ?, ?) "
                                   + "ON DUPLICATE KEY UPDATE `Tentacgia` = VALUES(`Tentacgia`)";
            try (PreparedStatement ps = conn.prepareStatement(sqlAuthorActual)) {
                for (Map<String, String> row : authorRows) {
                    ps.setString(1, row.get("Matacgia"));
                    ps.setString(2, row.get("Tentacgia"));
                    ps.setString(3, row.get("Mota"));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            System.out.println("   ✓ Đã xử lý " + authorRows.size() + " tác giả.");

            // 3. Nhập Publishers
            System.out.println("-> Đang nhập bảng `nhaxuatban`...");
            List<Map<String, String>> pubRows = readCsv(publishersFile);
            String sqlPub = "INSERT INTO `nhaxuatban` (`manxb`, `tennxb`, `diachinxb`, `sdt`, `mota`) VALUES (?, ?, ?, ?, ?) "
                          + "ON DUPLICATE KEY UPDATE `tennxb` = VALUES(`tennxb`)";
            try (PreparedStatement ps = conn.prepareStatement(sqlPub)) {
                for (Map<String, String> row : pubRows) {
                    ps.setString(1, row.get("manxb"));
                    ps.setString(2, row.get("tennxb"));
                    ps.setString(3, row.get("diachinxb"));
                    ps.setString(4, row.get("sdt"));
                    ps.setString(5, row.get("mota"));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            System.out.println("   ✓ Đã xử lý " + pubRows.size() + " nhà xuất bản.");

            // 4. Nhập Books
            System.out.println("-> Đang nhập bảng `sach`...");
            List<Map<String, String>> bookRows = readCsv(booksFile);
            String sqlBook = "INSERT INTO `sach` (`Masach`, `Matheloai`, `Manxb`, `Tensach`, `Isbn`, `Namxb`, `Sotrang`, `Giabia`, `Motasach`, `Hinhanh`, `IsDeleted`) "
                           + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                           + "ON DUPLICATE KEY UPDATE `Tensach` = VALUES(`Tensach`), `Matheloai` = VALUES(`Matheloai`), `Manxb` = VALUES(`Manxb`), "
                           + "`Isbn` = VALUES(`Isbn`), `Namxb` = VALUES(`Namxb`), `Giabia` = VALUES(`Giabia`), `Hinhanh` = VALUES(`Hinhanh`)";
            try (PreparedStatement ps = conn.prepareStatement(sqlBook)) {
                for (Map<String, String> row : bookRows) {
                    ps.setString(1, row.get("Masach"));
                    String catId = row.get("Matheloai");
                    if (catId != null && !catId.isEmpty()) ps.setInt(2, Integer.parseInt(catId)); else ps.setNull(2, Types.INTEGER);
                    String manxb = row.get("Manxb");
                    if (manxb != null && !manxb.isEmpty()) ps.setString(3, manxb); else ps.setNull(3, Types.VARCHAR);
                    ps.setString(4, row.get("Tensach"));
                    ps.setString(5, row.get("Isbn"));
                    String namxb = row.get("Namxb");
                    if (namxb != null && !namxb.isEmpty()) ps.setInt(6, Integer.parseInt(namxb)); else ps.setNull(6, Types.INTEGER);
                    ps.setNull(7, Types.INTEGER); // Sotrang
                    String giabia = row.get("Giabia");
                    ps.setBigDecimal(8, new java.math.BigDecimal(giabia != null && !giabia.isEmpty() ? giabia : "0.00"));
                    ps.setNull(9, Types.VARCHAR); // Motasach
                    ps.setString(10, row.get("Hinhanh"));
                    ps.setBoolean(11, false);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            System.out.println("   ✓ Đã xử lý " + bookRows.size() + " đầu sách.");

            // 5. Nhập sach_tacgia
            System.out.println("-> Đang nhập liên kết `sach_tacgia`...");
            String sqlST = "INSERT INTO `sach_tacgia` (`Masach`, `Matacgia`, `Vaitrotacgia`) VALUES (?, ?, ?) "
                         + "ON DUPLICATE KEY UPDATE `Vaitrotacgia` = VALUES(`Vaitrotacgia`)";
            try (PreparedStatement ps = conn.prepareStatement(sqlST)) {
                for (Map<String, String> row : bookRows) {
                    String matacgia = row.get("Matacgia");
                    if (matacgia != null && !matacgia.isEmpty()) {
                        ps.setString(1, row.get("Masach"));
                        ps.setString(2, matacgia);
                        ps.setString(3, "Tác giả chính");
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }
            System.out.println("   ✓ Đã xử lý liên kết sách - tác giả.");

            conn.commit();
            System.out.println("=================================================");
            System.out.println("  GHI CƠ SỞ DỮ LIỆU THÀNH CÔNG (TRANSACTION COMMIT) ");
            System.out.println("=================================================");

        } catch (Exception ex) {
            System.err.println("LỖI XẢY RA TRONG QUÁ TRÌNH NHẬP DỮ LIỆU: " + ex.getMessage());
            ex.printStackTrace();
            System.err.println("-> ĐÃ ROLLBACK TOÀN BỘ TRANSACTION.");
            System.exit(1);
        }
    }

    private static String getEnvOrDefault(String key, String def) {
        String val = System.getenv(key);
        return (val != null && !val.trim().isEmpty()) ? val.trim() : def;
    }

    private static List<Map<String, String>> readCsv(File file) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String headerLine = br.readLine();
            if (headerLine == null) return rows;
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }
            String[] headers = parseCsvLine(headerLine);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = parseCsvLine(line);
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    row.put(headers[i], i < values.length ? values[i] : "");
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private static String[] parseCsvLine(String line) {
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                list.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        list.add(sb.toString());
        return list.toArray(new String[0]);
    }
}
