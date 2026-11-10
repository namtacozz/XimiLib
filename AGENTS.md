<!--
Version: 1.4 | Last updated: 2026-11-04 | Owner: Architecture Team
Related files: docs/ARCHITECTURE.md, docs/DATABASE_SCHEMA.md, docs/BUSINESS_RULES.md, docs/PROJECT_STATUS.md, docs/TRACEABILITY.md, docs/CODING_STYLE.md
Quy ước nhãn: [TBD] = chưa chốt, cần xác nhận | [ASSUMPTION] = đề xuất kỹ thuật chưa được nghiệm thu
-->

> [!IMPORTANT]
> **Mọi sửa đổi, kể cả do AI thực hiện, phải tuân thủ §10 và qua `StyleRuleTest`. Không đưa lại comment, stream, Optional, tên tiếng Việt vào mã.**

# AGENTS.md — Điểm vào cho AI Agent (XimiLib)

> Đọc file này đầu tiên. Chi tiết nằm trong `docs/`; file này chỉ giữ quy tắc cốt lõi và con trỏ.

## 1. Tổng quan
- **XimiLib**: ứng dụng desktop quản lý thư viện cá nhân – Thư viện làng Xì Mi.
- **Stack**: Java 25 LTS, Swing + FlatLaf (`flatlaf`, `flatlaf-extras`, `miglayout-swing`), DJ-Raven Suite (`modal-dialog`, `swing-glasspane-popup`, `swing-datetime-picker`), MySQL 8.0+ tập trung (JDBC thuần `DriverManager`), BCrypt (`jbcrypt`), JUnit 5.
- **Danh mục thư viện đã duyệt (Whitelist)**: `flatlaf`, `flatlaf-extras`, `miglayout-swing`, `modal-dialog`, `swing-glasspane-popup`, `swing-datetime-picker`, `jbcrypt`, `mysql-connector-j`, `org.apache.poi:poi-ooxml` (phiên bản `5.2.5` - chỉ dùng cho xuất báo cáo Excel định dạng `.xlsx`).
- **Kiến trúc**: Clean Architecture (`presentation → application → domain ← infrastructure`), Pure DI tại Composition Root, Repository Pattern, Observer kết hợp quét DB định kỳ để đồng bộ nhiều máy.
- **Tác nhân (RBAC)**: Admin, Thủ thư, Độc giả.
- `docs/` là Single Source of Truth (SSOT).

## 2. Phạm vi (F-01 .. F-12 & SRS YC-01..YC-14)
| Nhóm | Nội dung |
|---|---|
| In-scope (Must) | Quản lý sách, Mượn trả, Tài khoản & Phân quyền, Thẻ thư viện (F-01..F-04) |
| In-scope (Should) | Thanh toán VietQR Napas247, Log kiểm toán hệ thống, Thống kê & Báo cáo POI Excel (F-05, F-06, F-12) |
| Nice | Gợi ý sách, Bình luận & Đánh giá sách, Bảng xếp hạng, Góp ý (F-07, F-09..F-11) |
| Out-of-scope | OTP, SMS bên ngoài: Không triển khai; hệ thống sử dụng thông báo nội bộ qua bảng `thongbao`. Chatbot riêng (F-08) hoãn; hiện thực nơi trao đổi qua UC 3.2.8 (`binhluan`). |

## 3. Nguồn Sự Thật & Thứ tự Ưu tiên khi Mâu thuẫn
1. **Tài liệu Đặc tả Yêu cầu Phần mềm (SRS gốc, IEEE Std 830-1998)**: BẬC CAO NHẤT (YC-01..YC-14, UC 3.2.1..3.2.9, Mục 4 Phi chức năng).
2. `docs/DATABASE_SCHEMA.md` > `docs/BUSINESS_RULES.md` > `docs/ARCHITECTURE.md` > `AGENTS.md` > `docs/CODING_STYLE.md` > `docs/PROJECT_STATUS.md` > `README.md`.
3. **Khi tài liệu nội bộ mâu thuẫn SRS**: Sửa tài liệu nội bộ theo SRS. Khi SRS không đề cập: Giữ tài liệu hiện hành, không tự suy diễn.
4. **Khi SRS không khả thi với kiến trúc hiện tại hoặc thay đổi quá lớn**: TUYỆT ĐỐI KHÔNG tự quyết. Ghi vào **Deviation Register** (`docs/TRACEABILITY.md`) kèm 2-3 phương án và dừng chờ người dùng phê duyệt trước khi lập trình.
5. **Quy tắc Đúng - Sai vs Phong cách**: Khi quy tắc phong cách (§10, R-13..R-17) mâu thuẫn quy tắc đúng-sai (R-01..R-12), quy tắc đúng-sai thắng. Không vì làm code "ngắn" hay "đẹp" mà phá vỡ ranh giới tầng, giao dịch an toàn hay kiểm quyền.

## 4. Lệnh build, test, chạy
- Build: `mvn clean package -f sources/pom.xml -DskipTests` (hoặc `run.bat` / `scripts/build.bat`)
- Test: `mvn test -f sources/pom.xml` (gồm 121 test cases: unit tests + IT MySQL thật)
- Chạy: `java -jar dist/XimiLib.jar` (hoặc `run.bat`)
- Khởi tạo DB: `java -jar dist/XimiLib.jar --init-db` (tự động nạp schema và toàn bộ migration V1 -> V5), hoặc chạy thủ công nạp migrations V1..V5 và `db/seed/seed_books.sql` (221 đầu sách, 62 tác giả, 11 thể loại, 20 NXB).
- Cấu hình kết nối DB qua biến môi trường: `XIMILIB_DB_HOST` (mặc định localhost), `XIMILIB_DB_PORT` (3306), `XIMILIB_DB_NAME` (ximilib_db), `XIMILIB_DB_USER` (ximilib_app), `XIMILIB_DB_PASS` (hoặc `MYSQL_PWD`). Tuyệt đối không commit mật khẩu.
- Tài nguyên ảnh bìa sách: lưu trữ tại `sources/src/main/resources/assets/covers/` (221 file `.jpg`)

## 5. Quy tắc bất khả xâm phạm
| ID | Quy tắc | Tham chiếu |
|---|---|---|
| R-01 | **Ranh giới tầng**: Presentation chỉ gọi Application Service, không gọi DAO/DB. Domain không import `javax.swing`, `java.sql`. | ARCHITECTURE |
| R-02 | **Nghiệp vụ đúng chỗ**: công thức phạt, bồi thường, mọi kiểm tra hợp lệ nằm ở Domain/Application, không viết trong View, Controller hay SQL. | BUSINESS_RULES |
| R-03 | **Soft-delete**: không hard-delete `sach` và bản ghi nghiệp vụ (phiếu mượn, phạt, thanh toán). Cấm xóa sách đang được mượn (BR-09). Tên cột cờ xóa chuẩn là `IsDeleted` (BOOLEAN). | BR-09, C7 |
| R-04 | **Tiền tệ**: `BigDecimal` trong Java, `DECIMAL` trong MySQL; cấm `float`/`double`. | DATABASE_SCHEMA §1 |
| R-05 | **Đồng thời**: cập nhật `cuonsach` trong transaction với `SELECT ... FOR UPDATE`, khóa theo thứ tự id tăng dần để tránh deadlock; lỗi thì rollback. Transaction đặt ở Application. | DATABASE_SCHEMA §Concurrency |
| R-06 | **Luồng Swing**: cập nhật UI trên EDT (`SwingUtilities.invokeLater`); DB, I/O, HTTP chạy ngoài EDT (`SwingWorker`/`ExecutorService`). Không truy vấn nặng trước khi hiện cửa sổ đầu tiên (khởi động nguội < 3 giây). | ARCHITECTURE |
| R-07 | **SQL**: 100% `PreparedStatement` với tham số `?`, không cộng chuỗi SQL. | — |
| R-08 | **Bảo mật**: mật khẩu mọi tài khoản (Admin, Thủ thư, Độc giả) băm BCrypt; không log mật khẩu, token hay dữ liệu mã hóa AES-256; không commit thông tin kết nối DB. Kiểm tra quyền RBAC ở Application Service, không chỉ ẩn nút trên UI. | ARCHITECTURE |
| R-09 | **Enum và hằng số**: trạng thái, vai trò, loại phạt dùng enum domain khớp DATABASE_SCHEMA §Enum. Các hằng quy định (3 cuốn, 14 ngày, 2.000đ/ngày...) khai báo một nơi duy nhất qua `LibraryPolicy`, hỗ trợ nạp đè từ bảng `cauhinh` qua `SystemSettingsPort` (DEV-06). | BR-02..BR-08, DEV-06 |
| R-10 | **Mất/hỏng sách**: khi báo mất sách, phiếu mượn chuyển `CHO_XU_LY_BOI_THUONG` và lập phiếu phạt đền bù theo BR-08; chỉ chính thức đóng phiếu mượn sang `DA_MAT` sau khi hoàn tất nộp tiền đền bù (SRS UC-9, C1). | BR-08, UC-9 |
| R-11 | **Chuẩn hóa UI (DJ-Raven Suite)**: Tuyệt đối cấm sử dụng `JOptionPane`, `JDialog` thuần của OS hoặc `JTextField` nhập ngày thủ công. Bắt buộc dùng `ModalDialog`/`Toast` (`swing-modal-dialog`) cho form CRUD/xác nhận/thông báo; `GlassPanePopup` (`swing-glasspane-popup`) cho quick overlay (thẻ thư viện số, chuông thông báo); và `DatePicker` (`swing-datetime-picker`) cho mọi trường `LocalDate`/`LocalDateTime`. Toàn bộ tương tác UI thông qua `UiModalHelper`. | ARCHITECTURE |
| R-12 | **Không bịa / Thư viện**: Chỉ sử dụng các thư viện đã được phê duyệt trong danh mục (FlatLaf, flatlaf-extras, miglayout, DJ-Raven Suite, jbcrypt, mysql-connector-j, org.apache.poi:poi-ooxml); không tự ý thêm bảng, cột, use case hay thư viện mới ngoài tài liệu. Đổi schema phải cập nhật `DATABASE_SCHEMA.md` trong cùng commit. Thiếu dữ kiện thì ghi Open Issue, không suy diễn. | — |

## 6. Quy chuẩn Thiết kế Giao diện (UI Component Standards - DJ-Raven Suite)
Hệ thống sử dụng bộ 3 thư viện DJ-Raven trên nền FlatLaf tập trung qua `com.ximilib.presentation.util.UiModalHelper`:
1. **ModalDialog & Toast (`io.github.dj-raven:modal-dialog`):**
   - Dùng cho toàn bộ form CRUD, hộp thoại xác nhận (YES/NO) và thông báo nhanh.
2. **GlassPanePopup (`raven.popup:swing-glasspane-popup`):**
   - Khởi tạo 1 lần trong JFrame: `UiModalHelper.initMainFrame(frame)`.
   - Dùng cho popup thông tin nhanh (Thẻ thư viện điện tử QR, Hộp thư thông báo chuông, Profile).
3. **DateTime Picker (`io.github.dj-raven:swing-datetime-picker`):**
   - Chuẩn hóa 100% đầu vào ngày tháng năm (`LocalDate`) với format `dd/MM/yyyy`. Cấm cho người dùng gõ chuỗi ngày tự do vào `JTextField`.
4. **Quy chuẩn Icon Vector (SVG Chuẩn - CẤM TỰ VẼ BẰNG TỌA ĐỘ):**
   - **Tuyệt đối cấm** AI tự viết code vẽ vector/icon thủ công bằng tọa độ Graphics2D (`drawLine`, `drawArc`, `drawPolygon`). Mã tự vẽ dễ méo mó, lệch pixel, vỡ hình.
   - **Bắt buộc 100%** sử dụng các file SVG vector chuẩn (Lucide / Feather / morphicons) lưu tại thư mục `src/main/resources/icons/svg/*.svg`.
   - Nạp icon qua `FlatSVGIcon` (`flatlaf-extras`) với `ColorFilter` đổi màu động, quản lý tập trung qua lớp `IconFactory`.
   - Cấm sử dụng các ký tự biểu tượng Unicode đặc biệt (như `✓`, `★`, emoji) trực tiếp trong chuỗi text hiển thị vì gây lỗi ô vuông (tofu glyph `▯`) trên font Windows mặc định. Dùng SVG icon tương ứng hoặc chữ thuần.

## 7. Quy trình sau mỗi thay đổi
1. Chạy `mvn test`; nghiệp vụ mới phải có test tương ứng trong bộ TC-01..TC-14.
2. Cập nhật `docs/PROJECT_STATUS.md` (trạng thái F-xx và Change Log).
3. Đổi schema, enum, công thức thì cập nhật file tài liệu tương ứng trong cùng commit.

## 8. Thuật ngữ đối chiếu (23 Bảng Chuẩn CSDL)
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
| Bồi thường mất/hỏng | Compensation | `Compensation` | `phieuphat` với `loaiphat = 'BOI_THUONG'` |
| Thanh toán | Payment | `Payment` | `thanhtoan` / `mathanhtoan` |
| Phiếu nhập sách | Stock Receipt | `StockReceipt` | `nhapsach` / `manhapsach` |
| Chi tiết nhập sách | Stock Receipt Detail | `StockReceiptDetail` | `chitietnhapsach` |
| Bình luận, đánh giá | Book Comment | `BookComment` | `binhluan` / `id` |
| Góp ý phản ánh | Feedback | `Feedback` | `gopy` / `id` |
| Cấu hình quy định | System Config | `SystemConfig` | `cauhinh` / `khoa` |
| Nhật ký kiểm toán | Audit Log | `AuditLog` | `auditlog` / `id` (Migration V5) |

## 10. Phong cách mã bắt buộc (Student Low-Code Style)
Tuân thủ toàn bộ các quy tắc bên dưới để mã nguồn đạt tính tự giải thích, trực quan, sinh viên năm hai có thể giải thích trôi chảy khi vấn đáp đồ án với giảng viên:

| ID | Quy tắc | Nội dung chi tiết |
|---|---|---|
| R-13 | **Không chú thích** | Không dùng `//`, `/* */`, Javadoc, mã bị comment-out, xóa các file `package-info.java`. Mã nguồn tự giải thích thông qua tên gọi tường minh; khi cần diễn giải phải tách hàm con có tên rõ nghĩa. (Ngoại lệ: file SQL, file cấu hình properties, tài liệu `.md`). |
| R-14 | **Định danh 100% tiếng Anh** | Tên class, method, biến, tham số, hằng số, enum 100% tiếng Anh không viết tắt (`bk`, `tmp`, `lst`). Hàm đặt theo `PascalCase` (`GetBookInfo()`, `FindUserByEmail()`, `CalculateOverdueFine()`, `IsCardExpired()`). Biến đặt theo `camelCase` (`bookTitle`, `overdueDays`). Hằng số `UPPER_SNAKE_CASE`. Giữ nguyên tên bắt buộc của JDK/Swing/JUnit khi override (`run`, `actionPerformed`, `paintComponent`). Toàn bộ chuỗi hiển thị tiếng Việt trên UI nằm tập trung trong lớp `UiText`. |
| R-15 | **Cấu trúc đơn giản (Low-Code)** | Chỉ dùng các cấu trúc căn bản: biến, `if`/`else`, `for`/`while`/`for-each`, `switch`, `try`/`catch`/`finally`, lớp thường, interface đơn giản, `ArrayList`, `HashMap`. **Cấm**: Stream API (`.stream()`, `Collectors`), Optional chuỗi, `var`, record, sealed class, pattern matching, builder/fluent API, reflection, annotation tự chế, ternary lồng, lambda phức tạp (chỉ cho phép lambda 1 dòng cho Swing listener). Hàm $\le$ ~25 dòng, lớp $\le$ ~300 dòng, $\le$ 3 cấp thụt lề, $\le$ 4 tham số. |
| R-16 | **Dễ giải thích hơn nhanh hoặc ngắn** | Ưu tiên giải pháp dễ hiểu, dễ trình bày khi bảo vệ đồ án; ưu tiên hàm có sẵn của JDK, Swing, FlatLaf và thư viện whitelist. Không thêm bất kỳ thư viện bên ngoài nào. Giảm thụt lề bằng cách return sớm (`guard clauses`). |
| R-17 | **Chuẩn hóa chữ và màu sắc** | Toàn bộ chữ hiển thị đi qua `UiText`, viết giọng tự nhiên theo phong cách humanizer (ngắn gọn, bỏ sáo rỗng, không emoji trang trí); giữ nguyên văn câu thông báo lỗi/thành công của SRS. Giao diện sử dụng 100% bảng màu tập trung `UITheme`, cấm hard-code mã màu hex trong View. |

---

## 11. Cách sửa đúng phong cách (10 ví dụ Trước / Sau)

#### Ví dụ 1: Thay thế Stream API / filter / map bằng vòng lặp for-each cơ bản
```java
// TRƯỚC (Phức tạp, khó debug từng bước):
List<String> titles = books.stream()
    .filter(b -> b.getAvailableCopies() > 0)
    .map(Book::getTitle)
    .collect(Collectors.toList());

// SAU (Tự giải thích, sinh viên dễ giải thích):
List<String> titles = new ArrayList<>();
for (Book book : books) {
    if (book.hasAvailableCopies()) {
        titles.add(book.getTitle());
    }
}
```

#### Ví dụ 2: Thay thế Optional lồng nhau bằng kiểm tra if-null truyền thống
```java
// TRƯỚC (Functional Optional):
return userRepository.findById(id)
    .map(User::getFullName)
    .orElse("Chưa xác định");

// SAU (Trực quan, dễ đặt breakpoint):
User user = userRepository.FindById(id);
if (user != null) {
    return user.getFullName();
}
return "Chưa xác định";
```

#### Ví dụ 3: Đổi tên hàm sang PascalCase và định danh tiếng Anh rõ nghĩa
```java
// TRƯỚC:
public int tinhTienPhat(PhieuMuon pm, int soNgay) { ... }

// SAU:
public BigDecimal CalculateOverdueFine(LoanTicket loanTicket, int overdueDays) { ... }
```

#### Ví dụ 4: Xóa bỏ Javadoc và chú thích rườm rà, dùng tên hàm tự giải thích
```java
// TRƯỚC:
/**
 * Hàm này dùng để kiểm tra độc giả có mượn được sách không
 * @param reader độc giả cần kiểm tra
 * @return true nếu được mượn
 */
// Kiểm tra độc giả có bị phạt không
if (reader.getTienPhat().compareTo(BigDecimal.ZERO) > 0) {
    return false; // Còn nợ phạt nên không cho mượn
}

// SAU:
if (HasUnpaidFine(reader)) {
    return false;
}
```

#### Ví dụ 5: Thay từ khóa `var` bằng kiểu dữ liệu tường minh
```java
// TRƯỚC:
var loans = loanRepository.findAllActive();
var total = BigDecimal.ZERO;

// SAU:
List<LoanTicket> loans = loanRepository.FindAllActive();
BigDecimal total = BigDecimal.ZERO;
```

#### Ví dụ 6: Thay thế toán tử 3 ngôi lồng nhau bằng if-else và return sớm
```java
// TRƯỚC:
String status = isOverdue ? (finePaid ? "Đã phạt" : "Chờ phạt") : (returned ? "Đã trả" : "Đang mượn");

// SAU:
if (isOverdue) {
    if (finePaid) {
        return UiText.STATUS_FINE_PAID;
    }
    return UiText.STATUS_WAITING_FINE;
}
if (returned) {
    return UiText.STATUS_RETURNED;
}
return UiText.STATUS_BORROWING;
```

#### Ví dụ 7: SQL với tham số PreparedStatement và hằng số SQL rõ nghĩa
```java
// TRƯỚC:
String sql = "SELECT * FROM sach WHERE tensach LIKE '%" + keyword + "%'";
ResultSet rs = stmt.executeQuery(sql);

// SAU:
private static final String SELECT_BOOKS_BY_TITLE = "SELECT * FROM sach WHERE IsDeleted = FALSE AND tensach LIKE ?";
PreparedStatement stmt = conn.prepareStatement(SELECT_BOOKS_BY_TITLE);
stmt.setString(1, "%" + keyword + "%");
ResultSet rs = stmt.executeQuery();
```

#### Ví dụ 8: Chuỗi hiển thị tiếng Việt tập trung trong `UiText`
```java
// TRƯỚC (Hard-code tiếng Việt rải rác trong View):
btnSave.setText("Lưu thông tin sách");
lblMessage.setText("Thêm sách thành công!");

// SAU:
btnSave.setText(UiText.BUTTON_SAVE_BOOK);
lblMessage.setText(UiText.MSG_BOOK_ADD_SUCCESS);
```

#### Ví dụ 9: Token màu sắc tập trung trong `UITheme`
```java
// TRƯỚC (Hard-code mã hex):
btnSubmit.setBackground(Color.decode("#DB2777"));
lblError.setForeground(new Color(185, 28, 28));

// SAU:
btnSubmit.setBackground(UITheme.PRIMARY);
lblError.setForeground(UITheme.DANGER);
```

#### Ví dụ 10: Quản lý Transaction tường minh thay vì callback lồng
```java
// TRƯỚC (Callback functional):
transactionCoordinator.executeInTransaction(ctx -> {
    loanRepo.create(loan);
    copyRepo.updateStatus(copyId, STATUS);
    return true;
});

// SAU (Tường minh Begin, Commit, Rollback trong Application Service):
try {
    transactionManager.BeginTransaction();
    loanRepository.CreateLoan(loan);
    copyRepository.UpdateStatus(copyId, BookCopyStatus.DANG_MUON);
    transactionManager.CommitTransaction();
} catch (Exception ex) {
    transactionManager.RollbackTransaction();
    throw new BorrowBookException(UiText.MSG_BORROW_FAILED, ex);
} finally {
    transactionManager.EndTransaction();
}
```
