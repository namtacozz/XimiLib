package tools;

import com.ximilib.infrastructure.persistence.connection.DatabaseConnectionManager;
import com.ximilib.infrastructure.security.SecurityUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;

public class SeedUsers {
    public static void main(String[] args) {
        System.out.println("Seeding default users into MySQL ximilib_db...");
        DatabaseConnectionManager cm = new DatabaseConnectionManager();

        String sqlUser = "INSERT INTO `nguoidung` (`ID`, `Tendangnhap`, `Matkhau`, `Hoten`, `Email`, `Gioitinh`, `Ngaysinh`, `Diachi`, `Cccd`, `Sdt`, `Mavaitro`, `Trangthai`, `solansai`, `khoaden`) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 0, NULL) "
                       + "ON DUPLICATE KEY UPDATE `Matkhau` = VALUES(`Matkhau`), `Hoten` = VALUES(`Hoten`), `Email` = VALUES(`Email`), `Mavaitro` = VALUES(`Mavaitro`), `Trangthai` = 1, `solansai` = 0, `khoaden` = NULL";

        String sqlCard = "INSERT INTO `thethuvien` (`Mathe`, `ID`, `Ngaycap`, `Ngayhethan`, `Trangthai`) "
                       + "VALUES (?, ?, ?, ?, 1) "
                       + "ON DUPLICATE KEY UPDATE `Ngayhethan` = VALUES(`Ngayhethan`), `Trangthai` = 1";

        String hash123456 = SecurityUtil.hashPassword("123456");

        try (Connection conn = cm.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlUser)) {
                // 1. Admin
                ps.setString(1, "ND001");
                ps.setString(2, "admin");
                ps.setString(3, hash123456);
                ps.setString(4, "Nguyễn Việt Nhật");
                ps.setString(5, "nhat.nguyen@dut.udn.vn");
                ps.setBoolean(6, true);
                ps.setObject(7, LocalDate.of(2000, 1, 1));
                ps.setString(8, "54 Nguyễn Lương Bằng, Đà Nẵng");
                ps.setString(9, SecurityUtil.encryptAes("201234567890"));
                ps.setString(10, "0905111222");
                ps.setString(11, "ADMIN");
                ps.addBatch();

                // 2. Thủ thư Kiên
                ps.setString(1, "ND002");
                ps.setString(2, "kien.phan");
                ps.setString(3, hash123456);
                ps.setString(4, "Phan Vũ Xuân Kiên");
                ps.setString(5, "kien.phan@dut.udn.vn");
                ps.setBoolean(6, true);
                ps.setObject(7, LocalDate.of(2002, 5, 15));
                ps.setString(8, "Hải Châu, Đà Nẵng");
                ps.setString(9, SecurityUtil.encryptAes("201234567891"));
                ps.setString(10, "0905123456");
                ps.setString(11, "LIBRARIAN");
                ps.addBatch();

                // 3. Thủ thư Nam
                ps.setString(1, "ND003");
                ps.setString(2, "nam.le");
                ps.setString(3, hash123456);
                ps.setString(4, "Lê Võ Thành Nam");
                ps.setString(5, "nam.le@dut.udn.vn");
                ps.setBoolean(6, true);
                ps.setObject(7, LocalDate.of(2002, 8, 20));
                ps.setString(8, "Cẩm Lệ, Đà Nẵng");
                ps.setString(9, SecurityUtil.encryptAes("201234567892"));
                ps.setString(10, "0905333444");
                ps.setString(11, "LIBRARIAN");
                ps.addBatch();

                // 4. Độc giả sinh viên
                ps.setString(1, "ND004");
                ps.setString(2, "sinhvien");
                ps.setString(3, hash123456);
                ps.setString(4, "Trần Văn An");
                ps.setString(5, "an.tran@sv.dut.udn.vn");
                ps.setBoolean(6, true);
                ps.setObject(7, LocalDate.of(2004, 3, 10));
                ps.setString(8, "Ký túc xá Bách Khoa, Đà Nẵng");
                ps.setString(9, SecurityUtil.encryptAes("201234567893"));
                ps.setString(10, "0914999888");
                ps.setString(11, "READER");
                ps.addBatch();

                ps.executeBatch();
            }

            // Tạo thẻ thư viện cho sinh viên
            try (PreparedStatement ps = conn.prepareStatement(sqlCard)) {
                ps.setString(1, "LIB-2026-00001");
                ps.setString(2, "ND004");
                ps.setObject(3, LocalDate.now());
                ps.setObject(4, LocalDate.now().plusYears(1));
                ps.executeUpdate();
            }

            conn.commit();
            System.out.println("-> Seeded users successfully into MySQL!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
