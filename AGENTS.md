<!--
Version: 1.1 | Last updated: 2026-10-27 | Owner: Architecture Team
Related files: docs/ARCHITECTURE.md, docs/DATABASE_SCHEMA.md, docs/BUSINESS_RULES.md, docs/PROJECT_STATUS.md
Quy ước nhãn: [TBD] = chưa chốt, cần xác nhận | [ASSUMPTION] = đề xuất kỹ thuật chưa được nghiệm thu
-->

# AGENTS.md — Điểm vào cho AI Agent (XimiLib)

> Đọc file này đầu tiên. Chi tiết nằm trong `docs/`; file này chỉ giữ quy tắc cốt lõi và con trỏ.

## 1. Tổng quan
- **XimiLib**: ứng dụng desktop quản lý thư viện, đồ án PBL – Trường ĐH Bách Khoa, ĐH Đà Nẵng.
- **Stack**: Java 17, Swing + FlatLaf, MySQL 8.0+ tập trung (JDBC + HikariCP), JUnit 5.
- **Kiến trúc**: Clean Architecture (`presentation → application → domain ← infrastructure`), Pure DI tại Composition Root, Repository Pattern, Observer kết hợp quét DB định kỳ để đồng bộ nhiều máy.
- **Tác nhân (RBAC)**: Admin, Thủ thư, Độc giả.
- `docs/` là Single Source of Truth (SSOT).

## 2. Phạm vi
| Nhóm | Nội dung |
|---|---|
| In-scope (Must) | Quản lý sách, Mượn trả, Tài khoản, Thẻ (F-01..F-04) |
| In-scope (Should) | Thanh toán VietQR (đang thực hiện), Log (F-05, F-06) |
| Nice | Gợi ý sách, Chat box, Bảng xếp hạng, Góp ý, Đánh giá sách (F-07..F-11) |
| Out-of-scope | OTP, SMS bên ngoài: Không triển khai; hệ thống sử dụng thông báo nội bộ qua bảng `thongbao` (C12) |

## 3. Thứ tự đọc và thứ tự ưu tiên khi mâu thuẫn
1. `docs/ARCHITECTURE.md`: package, quy tắc import, DI, Observer, cổng thanh toán.
2. `docs/DATABASE_SCHEMA.md`: 20 bảng, kiểu dữ liệu, enum, locking, chỉ mục.
3. `docs/BUSINESS_RULES.md`: FSM, công thức, ma trận validation, BR-01..BR-09.
4. `docs/PROJECT_STATUS.md`: F-01..F-11, Definition of Done, Change Log.

Khi hai nguồn mâu thuẫn: tên bảng/cột/enum theo `DATABASE_SCHEMA.md` > công thức/FSM theo `BUSINESS_RULES.md` > cấu trúc code theo `ARCHITECTURE.md` > file này. Không tự chọn ngầm: ghi vào mục Open Issues (§8) và báo người dùng.

## 4. Lệnh build, test, chạy
- Build: `mvn clean package -f sources/pom.xml -DskipTests` (hoặc `scripts/build.bat`)
- Test: `mvn test -f sources/pom.xml` (gồm 34 test cases: unit tests + IT MySQL thật)
- Chạy: `java -jar dist/XimiLib.jar` (hoặc `run.bat`)
- Khởi tạo DB: `mysql -u root -p < db/schema.sql` (tạo CSDL `ximilib_db` với 20 bảng chuẩn `utf8mb4`)
- Nạp dữ liệu sách mẫu (Seed): `mysql -u root -p ximilib_db < db/seed/seed_books.sql` (nạp toàn bộ 221 đầu sách, 62 tác giả, 11 thể loại, 20 NXB)
- Công cụ nhập liệu độc lập: `tools/DataMigrator.java` (hỗ trợ `--dry-run` và `--execute` với rollback transaction)
- Cấu hình kết nối DB qua biến môi trường: `XIMILIB_DB_HOST` (mặc định localhost), `XIMILIB_DB_PORT` (3306), `XIMILIB_DB_NAME` (ximilib_db), `XIMILIB_DB_USER` (ximilib_app), `XIMILIB_DB_PASS` (hoặc `MYSQL_PWD`). Tuyệt đối không commit mật khẩu.
- Tài nguyên ảnh bìa sách: lưu trữ tại `sources/src/main/resources/assets/covers/` (221 file `.jpg`)
- Dự án hướng tới Windows/macOS/Linux: mỗi script `.bat` cần bản `.sh` tương ứng `[TBD]`

## 5. Quy tắc bất khả xâm phạm
| ID | Quy tắc | Tham chiếu |
|---|---|---|
| R-01 | **Ranh giới tầng**: Presentation chỉ gọi Application Service, không gọi DAO/DB. Domain không import `javax.swing`, `java.sql`, HikariCP. | ARCHITECTURE |
| R-02 | **Nghiệp vụ đúng chỗ**: công thức phạt, bồi thường, mọi kiểm tra hợp lệ nằm ở Domain/Application, không viết trong View, Controller hay SQL. | BUSINESS_RULES |
| R-03 | **Soft-delete**: không hard-delete `sach` và bản ghi nghiệp vụ (phiếu mượn, phạt, thanh toán). Cấm xóa sách đang được mượn (BR-09). Tên cột cờ xóa chuẩn là `IsDeleted` (BOOLEAN). | BR-09, C7 |
| R-04 | **Tiền tệ**: `BigDecimal` trong Java, `DECIMAL` trong MySQL; cấm `float`/`double`. | DATABASE_SCHEMA §1 |
| R-05 | **Đồng thời**: cập nhật `cuonsach` trong transaction với `SELECT ... FOR UPDATE`, khóa theo thứ tự id tăng dần để tránh deadlock; lỗi thì rollback. Transaction đặt ở Application. | DATABASE_SCHEMA §Concurrency |
| R-06 | **Luồng Swing**: cập nhật UI trên EDT (`SwingUtilities.invokeLater`); DB, I/O, HTTP chạy ngoài EDT (`SwingWorker`/`ExecutorService`). Không truy vấn nặng trước khi hiện cửa sổ đầu tiên (khởi động nguội < 3 giây). | ARCHITECTURE |
| R-07 | **SQL**: 100% `PreparedStatement` với tham số `?`, không cộng chuỗi SQL. | — |
| R-08 | **Bảo mật**: mật khẩu mọi tài khoản (Admin, Thủ thư, Độc giả) băm BCrypt; không log mật khẩu, token hay dữ liệu mã hóa AES-256; không commit thông tin kết nối DB. Kiểm tra quyền RBAC ở Application Service, không chỉ ẩn nút trên UI. | ARCHITECTURE |
| R-09 | **Enum và hằng số**: trạng thái, vai trò, loại phạt dùng enum domain khớp DATABASE_SCHEMA §Enum. Các hằng quy định (3 cuốn, 14 ngày, 2.000đ/ngày...) khai báo một nơi duy nhất, không rải số trong code. | BR-02..BR-08 |
| R-10 | **Mất/hỏng sách**: khi báo mất sách, phiếu mượn chuyển `CHO_XU_LY_BOI_THUONG` và lập phiếu phạt đền bù theo BR-08; chỉ chính thức đóng phiếu mượn sang `DA_MAT` sau khi hoàn tất nộp tiền đền bù (SRS UC-9, C1). | BR-08, UC-9 |
| R-11 | **Chuẩn hóa UI (DJ-Raven Suite)**: Tuyệt đối cấm sử dụng `JOptionPane`, `JDialog` thuần của OS hoặc `JTextField` nhập ngày thủ công. Bắt buộc dùng `ModalDialog`/`Toast` (`swing-modal-dialog`) cho form CRUD/xác nhận/thông báo; `GlassPanePopup` (`swing-glasspane-popup`) cho quick overlay (thẻ thư viện số, chuông thông báo); và `DatePicker` (`swing-datetime-picker`) cho mọi trường `LocalDate`/`LocalDateTime`. Toàn bộ tương tác UI thông qua `UiModalHelper`. | ARCHITECTURE |
| R-12 | **Không bịa**: không thêm bảng, cột, use case, thư viện ngoài tài liệu. Đổi schema phải cập nhật `DATABASE_SCHEMA.md` trong cùng commit. Thiếu dữ kiện thì ghi Open Issue, không suy diễn. | — |

## 6. Quy chuẩn Thiết kế Giao diện (UI Component Standards - DJ-Raven Suite)
Hệ thống sử dụng bộ 3 thư viện DJ-Raven trên nền FlatLaf tập trung qua `com.ximilib.presentation.util.UiModalHelper`:
1. **ModalDialog & Toast (`io.github.dj-raven:modal-dialog`):**
   - Dùng cho toàn bộ form CRUD, hộp thoại xác nhận (YES/NO) và thông báo nhanh.
   - Code mẫu:
     ```java
     // Xác nhận hành động:
     UiModalHelper.showConfirm(parent, "Xác Nhận Xóa Sách", "Bạn có chắc chắn muốn xóa mềm?", () -> service.delete(id));
     // Thông báo Toast không chặn UI:
     UiModalHelper.showToastSuccess(parent, "Lưu thành công!");
     UiModalHelper.showToastError(parent, "Lỗi kết nối CSDL!");
     ```
2. **GlassPanePopup (`raven.popup:swing-glasspane-popup`):**
   - Khởi tạo 1 lần trong JFrame: `UiModalHelper.initMainFrame(frame)`.
   - Dùng cho popup thông tin nhanh (Thẻ thư viện điện tử QR, Hộp thư thông báo chuông).
   - Code mẫu:
     ```java
     UiModalHelper.showQuickPopup(new DigitalCardPopupPanel(cardId, fullName));
     ```
3. **DateTime Picker (`io.github.dj-raven:swing-datetime-picker`):**
   - Chuẩn hóa 100% đầu vào ngày tháng năm (`LocalDate`) với format `dd/MM/yyyy`. Cấm cho người dùng gõ chuỗi ngày tự do vào `JTextField`.
   - Code mẫu:
     ```java
     JFormattedTextField txtDate = new JFormattedTextField();
     DatePicker datePicker = UiModalHelper.createDatePicker(txtDate, false); // false = chọn 1 ngày, true = chọn khoảng
     datePicker.setSelectedDate(LocalDate.now());
     ```

## 7. Quy trình sau mỗi thay đổi
1. Chạy `mvn test`; nghiệp vụ mới phải có test (ít nhất TC-1..TC-4 trong SRS: mượn đúng hạn, trả trễ 5 ngày phạt 10.000đ, nợ phạt bị chặn mượn, báo mất).
2. Cập nhật `docs/PROJECT_STATUS.md` (trạng thái F-xx và Change Log).
3. Đổi schema, enum, công thức thì cập nhật file tài liệu tương ứng trong cùng commit.

## 8. Thuật ngữ đối chiếu (20 bảng)
Tên cột viết thường; chi tiết cột đầy đủ xem `DATABASE_SCHEMA.md`.

| Tiếng Việt | English | Java class | Bảng / cột khóa |
|---|---|---|---|
| Người dùng, tài khoản | User | `User` | `nguoidung` / `id`, `tendangnhap` |
| Vai trò | Role | `Role` | `vaitro` / `mavaitro` |
| Thẻ thư viện | Library Card | `LibraryCard` | `thethuvien` / `mathe` |
| Thông báo | Notification | `Notification` | `thongbao` |
| Thể loại | Category | `Category` | `theloai` / `matheloai` |
| Tác giả | Author | `Author` | `tacgia` / `matacgia` |
| Nhà xuất bản | Publisher | `Publisher` | `nhaxuatban` / `manxb` |
| Đầu sách | Book Title | `Book` | `sach` / `masach`, `tensach` |
| Sách – tác giả | Book–Author link | `BookAuthor` | `sach_tacgia` / (`masach`, `matacgia`) |
| Khu vực | Zone | `Zone` | `khuvuc` / `makhuvuc` |
| Kệ sách | Shelf | `Shelf` | `kesach` / `makesach` |
| Cuốn sách vật lý | Book Copy | `BookCopy` | `cuonsach` / `macuonsach`, `mavach` |
| Phiếu mượn | Borrow Ticket | `BorrowTicket` | `phieumuon` / `maphieumuon` |
| Chi tiết phiếu mượn | Borrow Detail | `BorrowTicketDetail` | `chitietphieumuon` / `machitiet` |
| Gia hạn mượn | Loan Extension | `LoanExtension` | `giahanmuon` / `magiahan` |
| Đặt chỗ, giữ sách | Reservation | `Reservation` | `datcho` / `madatcho` |
| Phiếu phạt | Fine Ticket | `FineTicket` | `phieuphat` / `maphieuphat` |
| Bồi thường mất/hỏng | Compensation | `Compensation` | `phieuphat` với `loaiphat = 'BOI_THUONG'` `[ASSUMPTION]` |
| Thanh toán | Payment | `Payment` | `thanhtoan` / `mathanhtoan` |
| Phiếu nhập sách | Stock Receipt | `StockReceipt` | `nhapsach` / `manhapsach` |
| Chi tiết nhập sách | Stock Receipt Detail | `StockReceiptDetail` | `chitietnhapsach` |

## 9. Trạng thái Các Vấn Đề Kỹ Thuật (Đã Giải Quyết 100%)
1. **VietQR**: Đã tích hợp thành công qua chuẩn Napas247 VietQR (`VietQrModalPanel`), sinh mã QR động kèm nội dung đối soát `XML{Maphieuphat}`, tự động lưu giao dịch vào bảng `thanhtoan`.
2. **Cờ soft-delete**: Đã thống nhất 100% dùng cột `IsDeleted` (BOOLEAN DEFAULT FALSE) theo `DATABASE_SCHEMA.md` v1.1 và quy tắc R-03.
3. **Phiếu mượn khi báo mất**: Đã hiện thực chuẩn FSM 2 bước theo SRS UC-9 và R-10: Khi báo mất chuyển `CHO_XU_LY_BOI_THUONG (4)`; sau khi thanh toán tiền đền bù mới chính thức đóng sang `DA_MAT (5)`.
4. **Scale tiền tệ**: Đã thống nhất `DECIMAL(12,2)` trong MySQL và `BigDecimal` (scale 2, `RoundingMode.HALF_UP`) trong Java theo R-04.
5. **Lệnh build/run**: Đã xác minh hoàn chỉnh trên cả Windows và Linux/macOS (`scripts/build.bat`, `scripts/build.sh`, `run.bat`, `run.sh`, `dist/XimiLib.jar`). Khởi động nguội < 1 giây.
