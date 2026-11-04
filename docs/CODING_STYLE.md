# Quy Chuẩn Lập Trình Sinh Viên Low-Code (Student Low-Code Coding Style)

> Tài liệu quy định phong cách viết mã chính thức cho dự án XimiLib.
> Mục tiêu cốt lõi: Code đơn giản nhất, không dùng kỹ thuật lập trình nâng cao thừa thãi, tên gọi tự giải thích, giúp sinh viên năm hai dễ đọc, dễ bảo trì và tự tin trả lời vấn đáp đồ án PBL trước hội đồng giảng viên.

---

## 1. Triết Lý Thiết Kế: "Sinh Viên Low-Code"

Khi sinh viên bảo vệ đồ án, giảng viên thường hỏi:
1. *"Em hãy chỉ ra luồng dữ liệu từ khi bấm nút Mượn sách trên màn hình cho đến khi lưu vào CSDL?"*
2. *"Hàm này làm nhiệm vụ gì, xử lý điều kiện ngoại lệ ra sao?"*
3. *"Tại sao em lại dùng cấu trúc phức tạp này thay vì vòng lặp thông thường?"*

Phong cách **Student Low-Code Style** giải quyết triệt để vấn đề trên bằng cách:
* **Không "vibe coding" hay phô diễn cú pháp phức tạp:** Loại bỏ Stream API, Optional chaining, Reflection hay Builder pattern lồng nhau — những thứ làm sinh viên bối rối khi cần đặt breakpoint debug từng dòng.
* **Cấu trúc phẳng, trực quan:** Ưu tiên `for-each`, `if-else` return sớm, biến trung gian có tên rõ nghĩa.
* **Tự giải thích 100% bằng tiếng Anh:** Đọc tên hàm là biết chức năng, không cần đọc chú thích.

---

## 2. Bốn Bộ Luật Cốt Lõi (Luật A - D)

### Luật A: Không Chú Thích Trong Mã Java
* **Quy định:** Tuyệt đối không dùng comment một dòng `//`, comment nhiều dòng `/* */`, Javadoc `/** */`, mã nguồn cũ bị comment-out và xóa toàn bộ các file `package-info.java`.
* **Nguyên nhân:** Chú thích thường nhanh chóng bị lạc hậu so với code, gây nhiễu thị giác. Khi một đoạn code cần chú thích để hiểu, nghĩa là đoạn code đó đang quá phức tạp hoặc đặt tên chưa tốt $\rightarrow$ Hãy tách thành một hàm con nhỏ có tên động từ + danh từ rõ nghĩa.
* **Ngoại lệ:** File SQL script (`db/`), file cấu hình `.properties`, và các file tài liệu hướng dẫn `.md`.

### Luật B: Định Danh 100% Tiếng Anh, Nhìn Là Hiểu
* **Quy định chung:** Tên class, method, biến, tham số, hằng số, enum đều 100% bằng tiếng Anh, không viết tắt khó hiểu (`bk`, `tmp`, `lst`, `arr`).
* **Quy tắc đặt tên:**
  - **Lớp / Interface:** `PascalCase` danh từ (`BookService`, `BookRepositoryImpl`, `UserSession`, `MainDashboardFrame`).
  - **Hàm (Method):** `PascalCase` động từ + danh từ (`GetBookInfo()`, `FindUserByEmail()`, `CalculateOverdueFine()`, `IsCardExpired()`).
  - **Biến & Thuộc tính:** `camelCase` danh từ mô tả rõ đối tượng (`bookTitle`, `overdueDays`, `finePerDay`, `readerCardId`). Biến boolean bắt đầu bằng `is/has/can` (`isDeleted`, `hasAvailableCopies`). Danh sách dùng số nhiều (`books`, `activeLoans`).
  - **Hằng số:** `UPPER_SNAKE_CASE` (`MAX_BORROW_COUNT`, `BORROW_DAYS`, `FINE_PER_DAY`).
  - **Ngoại lệ bắt buộc:** Giữ nguyên các hàm chuẩn khi override của JDK, Swing, FlatLaf, JUnit (`run`, `actionPerformed`, `paintComponent`, `equals`, `toString`, `hashCode`, `setUp`).
  - **Database Mapping:** Giữ nguyên tên bảng/cột MySQL trong câu SQL; ánh xạ sang tiếng Anh ở Java (`Masach` $\rightarrow$ `bookId`, `Maphieumuon` $\rightarrow$ `loanId`, `Tienphat` $\rightarrow$ `fineAmount`, `Tensach` $\rightarrow$ `title`). Chuỗi SQL đặt trong hằng số tên rõ nghĩa.
  - **Chuỗi UI:** Toàn bộ chuỗi hiển thị tiếng Việt có dấu đặt tập trung trong lớp `UiText`. Mọi màu sắc đặt trong `UITheme`.

#### Vì sao chọn hàm PascalCase cho đồ án?
Trong giáo trình đại học và khi vấn đáp đồ án:
1. **Phân biệt rạch ròi Thuộc tính và Hành vi:** Cú pháp `reader.cardId` (thuộc tính dữ liệu - camelCase) phân biệt rõ ràng với `reader.CanBorrowBooks()` (hành vi nghiệp vụ - PascalCase).
2. **Gần gũi với mã giả (Pseudo-code) và C#/.NET:** Rất quen thuộc với sinh viên trong các môn Lập trình Hướng đối tượng và Thiết kế Hệ thống, giúp slide trình bày đồ án sáng sủa, mạch lạc.
3. **Dễ tra cứu và diễn giải:** Từng hàm nghiệp vụ nổi bật ngay lập tức khi cuộn màn hình hoặc trình chiếu trước hội đồng.

### Luật C: Cấu Trúc Đơn Giản (Low-Code)
* **Chỉ sử dụng:** Biến, `if`/`else`, `for`/`while`/`for-each`, `switch` truyền thống, `try`/`catch`/`finally`, lớp thường, interface đơn giản, `ArrayList`, `HashMap`.
* **Tuyệt đối cấm:**
  - Không Stream API (`.stream()`, `.filter()`, `.map()`, `Collectors`).
  - Không chuỗi Optional (`.map().flatMap().orElseGet()`).
  - Không từ khóa `var` (luôn ghi rõ kiểu dữ liệu để dễ hiểu kiểu trả về).
  - Không Java records, sealed classes, pattern matching phức tạp.
  - Không Builder pattern, Fluent API phức tạp, Reflection, Annotation tự chế.
  - Không toán tử 3 ngôi lồng nhau (`a ? b : c ? d : e`).
  - Không lambda phức tạp nhiều dòng (chỉ cho phép lambda 1 dòng cho listener Swing).
* **Giới hạn số lượng:**
  - Mỗi hàm chỉ làm 1 việc, tối đa $\le$ ~25 dòng.
  - Mỗi hàm tối đa $\le$ 3 cấp thụt lề (áp dụng return sớm / Guard Clauses).
  - Mỗi hàm tối đa $\le$ 4 tham số (nếu nhiều hơn gom vào một DTO request đơn giản).
  - Mỗi lớp tối đa $\le$ ~300 dòng (nếu vượt quá, tách trách nhiệm).
* **Quản lý Transaction:** Application Service quản lý giao dịch tường minh bằng 3 thao tác rõ ràng `BeginTransaction()`, `CommitTransaction()`, `RollbackTransaction()` trong khối `try/catch/finally`. Repository tuyệt đối không can thiệp commit/rollback.
* **Tác vụ nền Swing:** Tạo lớp kế thừa `SwingWorker` có tên rõ ràng (ví dụ: `LoadBooksWorker`), xử lý dữ liệu nặng trong `doInBackground()` và cập nhật UI trong `done()`.

### Luật D: Giữ Vững Kiến Trúc 4 Tầng & Pure DI
Tuân thủ Clean Architecture chuẩn mực:
```text
com.ximilib.domain          # Nghiệp vụ lõi, POJO thuần túy, không import Swing, JDBC, SQL
com.ximilib.application     # Use Case Services, Quản lý giao dịch, Query Ports, DTOs
com.ximilib.infrastructure  # CSDL MySQL (JDBC thuần PreparedStatement), SePay API, AppContainer
com.ximilib.presentation    # Giao diện Swing FlatLaf, UiModalHelper, UiText, UITheme
```
Khởi tạo và kết nối phụ thuộc bằng Pure DI (Dependency Injection thủ công) tại `AppContainer`, không dùng framework DI phức tạp (Spring/Guice) để sinh viên nắm chắc vòng đời đối tượng.

---

## 3. Bảng Ánh Xạ Thuật Ngữ CSDL và Mã Nguồn (Việt → Anh)

| Tiếng Việt | Tên Bảng MySQL | Tên Cột Khóa CSDL | Java Model / Class | Java Identifier / Field |
|---|---|---|---|---|
| Người dùng, tài khoản | `nguoidung` | `id`, `tendangnhap` | `User` | `userId`, `username`, `passwordHash` |
| Vai trò | `vaitro` | `mavaitro` | `Role` | `roleId`, `roleName` |
| Thẻ thư viện | `thethuvien` | `mathe` | `LibraryCard` | `cardId`, `issueDate`, `expiryDate` |
| Đầu sách | `sach` | `masach` | `Book` | `bookId`, `title`, `isbn`, `coverPrice` |
| Cuốn sách vật lý | `cuonsach` | `macuonsach`, `mavach` | `BookCopy` | `copyId`, `barcode`, `status` |
| Tác giả | `tacgia` | `matacgia` | `Author` | `authorId`, `authorName` |
| Thể loại | `theloai` | `matheloai` | `Category` | `categoryId`, `categoryName` |
| Nhà xuất bản | `nhaxuatban` | `manxb` | `Publisher` | `publisherId`, `publisherName` |
| Khu vực | `khuvuc` | `makhuvuc` | `Zone` | `zoneId`, `zoneName` |
| Kệ sách | `kesach` | `makesach` | `Shelf` | `shelfId`, `shelfName` |
| Phiếu mượn | `phieumuon` | `maphieumuon` | `LoanTicket` | `loanId`, `borrowDate`, `dueDate`, `returnDate` |
| Chi tiết phiếu mượn | `chitietphieumuon` | `machitiet` | `LoanDetail` | `detailId`, `loanId`, `copyId` |
| Gia hạn mượn | `giahanmuon` | `magiahan` | `LoanExtension` | `extensionId`, `newDueDate` |
| Đặt chỗ, giữ sách | `datcho` | `madatcho` | `Reservation` | `reservationId`, `reservedAt` |
| Phiếu phạt | `phieuphat` | `maphieuphat` | `FineTicket` | `fineId`, `fineAmount`, `fineReason` |
| Thanh toán | `thanhtoan` | `mathanhtoan` | `Payment` | `paymentId`, `amount`, `paymentMethod` |
| Nhập sách | `nhapsach` | `manhapsach` | `StockReceipt` | `receiptId`, `importDate`, `totalAmount` |
| Bình luận, đánh giá | `binhluan` | `id` | `BookComment` | `commentId`, `rating`, `content` |
| Góp ý phản ánh | `gopy` | `id` | `Feedback` | `feedbackId`, `content`, `status` |
| Cấu hình quy định | `cauhinh` | `khoa` | `SystemConfig` | `configKey`, `configValue` |
| Nhật ký kiểm toán | `auditlog` | `id` | `AuditLog` | `auditId`, `action`, `actorUsername` |

---

## 4. Mười Ví Dụ Trước / Sau Chuẩn Mực

### Ví dụ 1: Thay Stream API bằng vòng lặp for-each cơ bản
```java
// TRƯỚC (Functional Stream):
List<String> titles = books.stream()
    .filter(b -> b.getAvailableCopies() > 0)
    .map(Book::getTitle)
    .collect(Collectors.toList());

// SAU (Student Low-Code Style):
List<String> titles = new ArrayList<>();
for (Book book : books) {
    if (book.hasAvailableCopies()) {
        titles.add(book.getTitle());
    }
}
```

### Ví dụ 2: Thay Optional lồng nhau bằng kiểm tra null truyền thống
```java
// TRƯỚC:
return userRepository.findById(id)
    .map(User::getFullName)
    .orElse("Chưa xác định");

// SAU:
User user = userRepository.FindById(id);
if (user != null) {
    return user.getFullName();
}
return "Chưa xác định";
```

### Ví dụ 3: Đổi tên hàm sang PascalCase và định danh tiếng Anh tường minh
```java
// TRƯỚC:
public int tinhTienPhat(PhieuMuon pm, int soNgay) { ... }

// SAU:
public BigDecimal CalculateOverdueFine(LoanTicket loanTicket, int overdueDays) { ... }
```

### Ví dụ 4: Xóa bỏ chú thích rườm rà, tách hàm con tự giải thích
```java
// TRƯỚC:
/**
 * Kiểm tra độc giả có mượn được sách không
 */
// Kiểm tra nợ phạt
if (reader.getTienPhat().compareTo(BigDecimal.ZERO) > 0) {
    return false; // Còn nợ phạt nên không cho mượn
}

// SAU:
if (HasUnpaidFine(reader)) {
    return false;
}
```

### Ví dụ 5: Thay từ khóa `var` bằng kiểu dữ liệu tường minh
```java
// TRƯỚC:
var loans = loanRepository.findAllActive();
var total = BigDecimal.ZERO;

// SAU:
List<LoanTicket> loans = loanRepository.FindAllActive();
BigDecimal total = BigDecimal.ZERO;
```

### Ví dụ 6: Thay thế toán tử 3 ngôi lồng nhau bằng if-else và return sớm
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

### Ví dụ 7: SQL với PreparedStatement và hằng số SQL rõ nghĩa
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

### Ví dụ 8: Chuỗi hiển thị tiếng Việt tập trung trong `UiText`
```java
// TRƯỚC (Hard-code tiếng Việt rải rác):
btnSave.setText("Lưu thông tin sách");
lblMessage.setText("Thêm sách thành công!");

// SAU:
btnSave.setText(UiText.BUTTON_SAVE_BOOK);
lblMessage.setText(UiText.MSG_BOOK_ADD_SUCCESS);
```

### Ví dụ 9: Token màu sắc tập trung trong `UITheme`
```java
// TRƯỚC (Hard-code mã hex):
btnSubmit.setBackground(Color.decode("#DB2777"));
lblError.setForeground(new Color(185, 28, 28));

// SAU:
btnSubmit.setBackground(UITheme.PRIMARY);
lblError.setForeground(UITheme.DANGER);
```

### Ví dụ 10: Quản lý Transaction tường minh ở Application Service
```java
// TRƯỚC (Callback functional):
transactionCoordinator.executeInTransaction(ctx -> {
    loanRepo.create(loan);
    copyRepo.updateStatus(copyId, STATUS);
    return true;
});

// SAU (Tường minh Begin, Commit, Rollback):
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
