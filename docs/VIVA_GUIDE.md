# CẨM NANG VẤN ĐÁP ĐỒ ÁN XIMILIB (VIVA GUIDE)
> Phiên bản: 1.1 | Ngày cập nhật: 2026-11-04 | Tác giả: Principal Software Architect & QA Lead

> Cẩm nang hướng dẫn sinh viên trả lời vấn đáp đồ án PBL trước hội đồng giảng viên.
> Trình bày trực quan, bám sát từng dòng mã nguồn, luồng dữ liệu 4 tầng (Clean Architecture) và 24 bảng CSDL MySQL.

---

## BẢNG TRA CỨU NHANH 8 CHỨC NĂNG NGHIỆP VỤ CỐT LÕI

| STT | Nghiệp Vụ | Màn Hình (View) | Service Xử Lý | Bảng CSDL | Quy Tắc Liên Quan |
|:---:|---|---|---|---|---|
| 1 | **Đăng nhập & Khóa TK** | `LoginFormPanel` | `AuthenticationService` | `nguoidung` | BR-01, DEV-02, R-08 |
| 2 | **Đăng ký bạn đọc** | `RegisterForm` | `AuthenticationService` | `nguoidung`, `thethuvien` | BR-10, BR-11, YC-02 |
| 3 | **Tra cứu sách** | `BookCatalogView` | `BookCatalogService` | `sach`, `theloai`, `tacgia` | UC 3.2.4, BR-13 |
| 4 | **Đăng ký mượn sách** | `CirculationDeskView` | `CirculationService` | `phieumuon`, `cuonsach` | BR-02..BR-05, R-05 |
| 5 | **Trả sách & Phạt quá hạn** | `ReturnAndCompensationView` | `CirculationService` | `phieumuon`, `phieuphat` | BR-06, BR-07 |
| 6 | **Báo mất & Bồi thường** | `ReaderMyLoansView` | `CirculationService` | `phieumuon`, `phieuphat` | BR-08, R-10, UC 3.2.9 |
| 7 | **Thanh toán VietQR** | `VietQrModalPanel` | `PaymentService` | `thanhtoan`, `phieuphat` | F-05, YC-06 |
| 8 | **Thống kê & Xuất Excel** | `ReportsDashboardView` | `DashboardService` | Toàn bộ 24 bảng | F-12, YC-07, DEV-05 |

---

## CHI TIẾT 8 LUỒNG NGHIỆP VỤ & CÂU HỎI VẤN ĐÁP

### 1. Chức Năng Đăng Nhập & Cơ Chế Khóa Tài Khoản (BR-01, DEV-02)

#### Luồng Thực Thi Từng Bước:
1. **View:** Người dùng nhập Tên đăng nhập / Email / SĐT và mật khẩu tại `LoginFormPanel.java`, bấm `ĐĂNG NHẬP` (hoặc nhấn `Enter`).
2. **Background Worker:** `LoginFormPanel` chuyển nút sang *"Đang xác thực..."*, khởi chạy `SwingWorker` gọi `AuthenticationService.login(identifier, password)`.
3. **Application Service (`AuthenticationServiceImpl.java`):**
   - Gọi `UserRepository.findByIdentifier(identifier)` (tìm khớp cả `tendangnhap`, `email` hoặc `sdt` theo DEV-02).
   - Kiểm tra `user.getIsLocked()`: Nếu tài khoản đang bị khóa và chưa qua 15 phút $\rightarrow$ ném `AccountLockedException` với thông báo *"Tài khoản bị tạm khóa 15 phút do nhập sai quá số lần quy định."*
   - Kiểm tra băm mật khẩu qua `BCrypt.checkpw(password, user.getPasswordHash())`.
   - Nếu sai: Tăng `failed_attempts` thêm 1. Nếu chạm 5 lần $\rightarrow$ cập nhật `locked_until = NOW() + INTERVAL 15 MINUTE` và khóa tài khoản theo BR-01.
   - Nếu đúng: Đặt lại `failed_attempts = 0`, ghi nhận `UserSession.setCurrentUser(user)` và ghi `AuditLogger.log(userId, "LOGIN", "Đăng nhập thành công")`.
4. **View Response:** Trên EDT (`done()`), `LoginFormPanel` đóng form đăng nhập và mở `MainDashboardFrame`.

#### 3 Câu Hỏi Giảng Viên Hay Hỏi:
* **Hỏi 1: Tại sao hệ thống cho phép đăng nhập bằng cả Email và Số điện thoại mà không làm chậm câu truy vấn SQL?**
  * *Trả lời:* CSDL đã tạo index đa cột `idx_nguoidung_login (tendangnhap, email, sdt)` và câu lệnh SQL dùng `WHERE tendangnhap = ? OR email = ? OR sdt = ?` kết hợp tham số PreparedStatement, tận dụng chỉ mục B-Tree nên tốc độ phản hồi luôn $< 10\text{ms}$.
* **Hỏi 2: Cơ chế khóa 15 phút được lưu ở đâu? Nếu tắt ứng dụng mở lại có bị mất đếm khóa không?**
  * *Trả lời:* Dữ liệu số lần sai (`solansai`) và thời điểm mở khóa (`khoadenluc`) được lưu trực tiếp trong bảng CSDL `nguoidung`. Vì vậy dù tắt ứng dụng hoặc chuyển sang máy tính khác thì tài khoản vẫn bị khóa đúng 15 phút theo chuẩn an toàn BR-01.
* **Hỏi 3: Mật khẩu lưu trong CSDL là gì? Có giải mã ngược được không?**
  * *Trả lời:* Mật khẩu được băm một chiều bằng thuật toán BCrypt với salt ngẫu nhiên (`jbcrypt:0.4`), chi phí băm cost factor = 10 (R-08). BCrypt là hàm băm một chiều chống tấn công Rainbow Table, về mặt toán học không thể giải mã ngược mà chỉ có thể so khớp bằng `BCrypt.checkpw()`.

---

### 2. Chức Năng Đăng Ký Bạn Đọc & Cấp Thẻ Thư Viện (BR-10, BR-11, YC-02)

#### Luồng Thực Thi Từng Bước:
1. **View:** Độc giả điền 7 trường tại `RegisterForm.java` (Họ tên, Ngày sinh qua `DatePicker`, CCCD, Địa chỉ, Email, SĐT, Mật khẩu có `PasswordPolicyChecklist` kiểm tra trực tiếp 4 tiêu chuẩn).
2. **Validation:** Khi bấm `Đăng Ký`, form kiểm tra inline: CCCD đúng 9 hoặc 12 số (BR-11), mật khẩu $\ge 8$ ký tự gồm chữ hoa, số, ký tự đặc biệt (BR-10).
3. **Application Service (`AuthenticationServiceImpl.java`):**
   - Kiểm tra trùng lặp CCCD/Email trong CSDL. Nếu trùng $\rightarrow$ ném `BusinessRuleException("Số CMND/CCCD hoặc Email đã được đăng ký trên hệ thống")`.
   - Băm mật khẩu BCrypt.
   - Bọc trong Transaction:
     * Chèn bản ghi `nguoidung` mới với `vaitro_id = 3` (Độc giả).
     * Tự động sinh mã thẻ `mathe = "LIB-" + Năm + "-" + SốThứTự5ChữSố` (ví dụ: `LIB-2026-00125`).
     * Chèn bản ghi `thethuvien` với thời hạn 365 ngày (`ngayhethan = NOW() + 1 YEAR`) và trạng thái `1` (Hoạt động).
4. **View Response:** Hiển thị toast kèm mã thẻ thư viện và chuyển về form Đăng nhập.

#### 3 Câu Hỏi Giảng Viên Hay Hỏi:
* **Hỏi 1: Tại sao phải bọc thao tác tạo Người dùng và tạo Thẻ thư viện trong một Transaction?**
  * *Trả lời:* Để đảm bảo tính toàn vẹn dữ liệu (Atomicity). Nếu tạo xong người dùng nhưng bước cấp thẻ thư viện bị lỗi (ví dụ nghẽn mạng DB), hệ thống sẽ tự động `RollbackTransaction()`, tránh tình trạng tạo ra tài khoản "mồ côi" không có thẻ thư viện.
* **Hỏi 2: Mã thẻ `LIB-YYYY-XXXXX` sinh ra như thế nào để đảm bảo không bị trùng khi nhiều máy cùng đăng ký?**
  * *Trả lời:* Hệ thống truy vấn số lượng thẻ lớn nhất trong năm hiện tại bằng lệnh `SELECT MAX(mathe) FROM thethuvien WHERE mathe LIKE 'LIB-2026-%' FOR UPDATE` trong transaction, sau đó cộng 1 và format `%05d`.
* **Hỏi 3: Tại sao không cho người dùng tự gõ ngày sinh mà bắt buộc dùng DatePicker?**
  * *Trả lời:* Để triệt tiêu hoàn toàn lỗi parse ngày tháng do khác biệt định dạng giữa các hệ điều hành (`dd/MM/yyyy` vs `MM/dd/yyyy`), tuân thủ quy tắc R-11. `DatePicker` trả về trực tiếp đối tượng `java.time.LocalDate` an toàn tuyệt đối.

---

### 3. Chức Năng Tra Cứu Kho Sách & Kiểm Soát Tồn Kho (UC 3.2.4)

#### Luồng Thực Thi Từng Bước:
1. **View:** Độc giả/Thủ thư gõ từ khóa vào ô tìm kiếm trên `BookCatalogView.java` (hỗ trợ phím tắt toàn cục `Ctrl + F`).
2. **Debounce & Loading:** Hệ thống debounce 300ms chống spam truy vấn, hiển thị 3 dòng Skeleton Loading nhấp nháy màu `TINT_LIGHT`.
3. **Application Service (`BookCatalogServiceImpl.java`):**
   - Gọi `BookRepository.searchBooks(criteria, page, pageSize)`.
4. **Infrastructure SQL (`BookRepositoryImpl.java`):**
   - Thực thi câu lệnh có phân trang:
     ```sql
     SELECT s.masach, s.tensach, s.isbn, s.giabia, tl.tentheloai, tg.tentacgia,
            (SELECT COUNT(*) FROM cuonsach c WHERE c.masach = s.masach AND c.trangthai = 1) AS so_luong_kha_dung
     FROM sach s
     LEFT JOIN theloai tl ON s.matheloai = tl.matheloai
     LEFT JOIN sach_tacgia stg ON s.masach = stg.masach
     LEFT JOIN tacgia tg ON stg.matacgia = tg.matacgia
     WHERE s.IsDeleted = FALSE AND (s.tensach LIKE ? OR tg.tentacgia LIKE ? OR tl.tentheloai LIKE ?)
     LIMIT ? OFFSET ?;
     ```
5. **View Response:** Hiển thị kết quả dưới dạng Bảng (`JTable`) hoặc Thẻ lưới (`Grid View`) tùy theo lựa chọn lưu trong `UserSession`. Nếu không có kết quả $\rightarrow$ hiển thị `EmptyStatePanel` với thông điệp chuẩn SRS: *"Không tìm thấy cuốn sách nào khớp với từ khóa của bạn"*.

#### 3 Câu Hỏi Giảng Viên Hay Hỏi:
* **Hỏi 1: Cơ chế phân trang (Pagination) ở đây là Server-side hay Client-side? Tại sao?**
  * *Trả lời:* Hệ thống dùng phân trang Server-side với `LIMIT` và `OFFSET` trong MySQL. Vì thư viện có thể có hàng chục ngàn đầu sách, nếu kéo toàn bộ về Client sẽ làm tràn RAM (OOM) và vi phạm chỉ tiêu Cold Start < 3 giây.
* **Hỏi 2: Chức năng xóa sách hoạt động thế nào? Có xóa mất dữ liệu trong DB không?**
  * *Trả lời:* Tuân thủ quy tắc R-03 và BR-09, hệ thống dùng Soft-delete (`IsDeleted = TRUE`). Đặc biệt, trước khi xóa, hệ thống kiểm tra nếu có bản sao đang được mượn (`trangthai = DANG_MUON`) thì lập tức chặn lại và thông báo: *"Không thể xóa đầu sách đang có độc giả mượn"*.
* **Hỏi 3: Ảnh bìa sách 221 cuốn được quản lý như thế nào để không bị giật lag khi cuộn chuột?**
  * *Trả lời:* Ảnh bìa được lưu cục bộ trong `resources/assets/covers/` và nạp bất đồng bộ thông qua `THUMBNAIL_CACHE` (bộ nhớ đệm LRU Cache). Khi cuộn bảng, hệ thống chỉ hiển thị ảnh đã được scale sẵn kích thước nhỏ (60x90px), không đọc lại file từ ổ đĩa.

---

### 4. Chức Năng Mượn Sách & Khóa Bi Quan Chống Tranh Chấp (BR-02..BR-05, R-05)

#### Luồng Thực Thi Từng Bước:
1. **View:** Thủ thư quét mã thẻ độc giả $\rightarrow$ quét mã vạch cuốn sách tại `CirculationDeskView.java`.
2. **Kiểm Tra Điều Kiện (Guard Clauses tại `CirculationServiceImpl.java`):**
   - Kiểm tra hạn thẻ: Nếu hết hạn $\rightarrow$ chặn *"Thẻ thư viện của bạn đã hết hạn sử dụng. Vui lòng liên hệ thủ thư để gia hạn"* (BR-07).
   - Kiểm tra nợ phạt: Nếu độc giả còn phiếu phạt chưa nộp $\rightarrow$ chặn *"Độc giả còn nợ phạt chưa thanh toán"* (BR-07).
   - Kiểm tra sách quá hạn: Nếu có cuốn chưa trả quá hạn $\rightarrow$ chặn *"Bạn có sách quá hạn chưa hoàn trả"* (BR-07).
   - Kiểm tra hạn mức: Nếu mượn về nhà và đã có 3 cuốn $\rightarrow$ chặn *"Bạn đã đạt giới hạn mượn tối đa 3 cuốn sách mang về"* (BR-02).
3. **Transaction Điều Phối & Khóa Bi Quan (R-05):**
   ```sql
   -- Khóa dòng cuốn sách để tránh 2 thủ thư duyệt cùng 1 lúc
   SELECT * FROM cuonsach WHERE macuonsach = ? FOR UPDATE;
   -- Đổi trạng thái sang ĐANG_MUON (2)
   UPDATE cuonsach SET trangthai = 2, updated_at = NOW() WHERE macuonsach = ?;
   -- Thêm phiếu mượn và chi tiết phiếu mượn
   INSERT INTO phieumuon (mathe, hantranhan, loaiphieu, trangthai) VALUES (?, ?, ?, 1);
   ```
4. **Đồng bộ đa máy:** Kích hoạt `BookStockChangedEvent` cập nhật ngay trên giao diện các máy khác thông qua Background Polling Task.

#### 3 Câu Hỏi Giảng Viên Hay Hỏi:
* **Hỏi 1: Tại sao phải dùng `SELECT ... FOR UPDATE` khi mượn sách?**
  * *Trả lời:* Để giải quyết bài toán tương tranh (Concurrency). Giả sử chỉ còn đúng 1 cuốn sách duy nhất, nếu 2 độc giả cùng bấm mượn tại 2 máy khác nhau ở cùng một phần nghìn giây, `FOR UPDATE` sẽ khóa dòng dữ liệu trong DB, buộc giao dịch thứ hai phải chờ cho đến khi giao dịch thứ nhất hoàn tất và phát hiện sách đã hết, tránh lỗi mượn quá số lượng thực tế.
* **Hỏi 2: Mượn tại chỗ và mượn về nhà khác nhau như thế nào trong mã nguồn?**
  * *Trả lời:* Được phân biệt bởi enum `LoanType`:
    - Mượn về nhà (`VE_NHA`): Hạn trả tối đa 14 ngày (BR-02).
    - Mượn tại chỗ (`TAI_CHO`): Hạn trả cố định vào 17:00 cùng ngày (BR-04), không tính vào hạn mức 3 cuốn mang về nhà.
* **Hỏi 3: Nếu giữa chừng mất mạng DB thì giao dịch xử lý ra sao?**
  * *Trả lời:* Tầng Application Service quản lý khối `try/catch/finally` gọi `RollbackTransaction()`. MySQL tự động giải phóng toàn bộ khóa `FOR UPDATE` và khôi phục trạng thái cuốn sách như ban đầu, hoàn toàn không sinh rác dữ liệu.

---

### 5. Chức Năng Trả Sách & Tính Phạt Quá Hạn (BR-06, BR-07)

#### Luồng Thực Thi Từng Bước:
1. **View:** Thủ thư quét mã vạch cuốn sách cần trả tại `ReturnAndCompensationView.java`.
2. **Application Service (`CirculationServiceImpl.java`):**
   - Lấy thông tin phiếu mượn và ngày hẹn trả `Hantranhan`.
   - Tính số ngày quá hạn: `overdueDays = max(0, ChronoUnit.DAYS.between(dueDate, LocalDate.now()))`.
   - Nếu `overdueDays > 0`: Gọi `FineCalculationService.CalculateOverdueFine(overdueDays)`.
   - Tiền phạt = $2.000\text{đ} \times \text{số ngày trễ}$ (BR-06).
   - Tự động lập bản ghi `phieuphat` với `loaiphat = 'QUA_HAN'`, `sotien = fineAmount`, `trangthai = 0` (Chưa nộp).
   - Cập nhật cuốn sách về `trangthai = 1` (Khả dụng).
   - Đóng phiếu mượn sang `trangthai = 2` (Đã hoàn trả).

#### 3 Câu Hỏi Giảng Viên Hay Hỏi:
* **Hỏi 1: Công thức tính phạt trễ hạn được đặt ở đâu trong kiến trúc Clean Architecture?**
  * *Trả lời:* Tuân thủ quy tắc R-02, toàn bộ công thức phạt được đặt tại Domain Service `FineCalculationService.java`, tuyệt đối không viết trong View, Controller hay câu lệnh SQL để đảm bảo tính độc lập và dễ viết Unit Test (`FineCalculationServiceTest`).
* **Hỏi 2: Tại sao tiền phạt lại dùng kiểu `BigDecimal` mà không dùng `double` hay `float`?**
  * *Trả lời:* Tuân thủ quy tắc R-04. Kiểu số thực `double` và `float` trong máy tính sử dụng chuẩn IEEE 754 biểu diễn dấu chấm động nhị phân, dễ gây ra sai số làm tròn số thập phân (ví dụ $0.1 + 0.2 = 0.30000000000000004$). Trong ứng dụng quản lý tài chính và tiền tệ, bắt buộc phải dùng `BigDecimal` với scale 2 và `RoundingMode.HALF_UP` để đảm bảo độ chính xác tuyệt đối.
* **Hỏi 3: Độc giả bị phạt trễ hạn có được mượn tiếp cuốn sách khác không?**
  * *Trả lời:* Theo quy tắc BR-07, hệ thống lập tức chặn quyền mượn sách mới của độc giả cho đến khi độc giả thanh toán hết toàn bộ tiền phạt (tiền mặt hoặc quét mã VietQR).

---

### 6. Chức Năng Báo Mất Sách & Bồi Thường 2 Bước (BR-08, R-10, UC 3.2.9)

#### Luồng Thực Thi Từng Bước:
1. **Bước 1 (Báo Mất):**
   - Độc giả/Thủ thư chọn cuốn sách bị mất và bấm "Báo mất sách".
   - Hệ thống tính phí bồi thường theo công thức BR-08:
     $$\text{Tổng bồi thường} = 200\% \times \text{giá bìa sách} + 20.000\text{đ (phí xử lý)} + \text{tiền phạt trễ hạn (nếu có)}$$
   - Phiếu mượn chuyển sang trạng thái chờ: `CHO_XU_LY_BOI_THUONG (4)`.
   - Lập phiếu phạt loại `BOI_THUONG`.
2. **Bước 2 (Hoàn Tất Nộp Tiền Đền Bù):**
   - Độc giả thanh toán tiền đền bù (tiền mặt hoặc VietQR).
   - Phiếu phạt chuyển sang `DA_THANH_TOAN (1)`.
   - Chỉ sau khi nhận đủ tiền, phiếu mượn mới chính thức được đóng sang trạng thái `DA_MAT (5)`. Cuốn sách vật lý được cập nhật trạng thái `DA_MAT (4)` và loại khỏi lưu hành.

#### 3 Câu Hỏi Giảng Viên Hay Hỏi:
* **Hỏi 1: Tại sao hệ thống không đóng phiếu mượn sang "Đã mất" ngay khi độc giả báo mất?**
  * *Trả lời:* Đây là quy tắc FSM 2 bước theo yêu cầu SRS UC-9 và R-10. Nếu đóng sang "Đã mất" ngay lập tức, độc giả có thể lợi dụng kẽ hở để xóa phiếu mượn mà không chịu nộp tiền đền bù. Trạng thái `CHO_XU_LY_BOI_THUONG` giữ cho độc giả vẫn bị ghi nhận nghĩa vụ tài chính và chặn mượn sách mới cho đến khi hoàn tất thanh toán.
* **Hỏi 2: Nếu độc giả tìm lại được cuốn sách sau khi đã báo mất thì sao?**
  * *Trả lời:* Khi ở trạng thái `CHO_XU_LY_BOI_THUONG` và chưa nộp phạt, thủ thư có quyền hủy yêu cầu báo mất, chuyển cuốn sách trở lại quy trình trả sách thông thường và chỉ tính tiền phạt trễ hạn nếu có.
* **Hỏi 3: Giá bìa sách để tính $200\%$ được lấy từ đâu?**
  * *Trả lời:* Được lấy trực tiếp từ cột `giabia` (kiểu `DECIMAL(12,2)`) của bảng `sach`, đảm bảo bất biến và công bằng.

---

### 7. Chức Năng Thanh Toán VietQR Napas247 (F-05, YC-06)

#### Luồng Thực Thi Từng Bước:
1. **View:** Độc giả bấm thanh toán tiền phạt / bồi hoàn $\rightarrow$ Mở `VietQrModalPanel.java`.
2. **Sinh Mã QR Động:** Hệ thống tạo `paymentCode = "XML" + maphieuphat`, sinh URL ảnh VietQR chuẩn Napas247 từ SePay API.
3. **Đối Soát Tự Động (Long Polling & Boundary Regex):**
   - Bật timer bất đồng bộ quét lịch sử giao dịch mỗi 3 giây.
   - Khi nhận giao dịch chuyển khoản từ ngân hàng, kiểm tra nội dung chuyển tiền bằng Regex ranh giới:
     `Pattern.compile("(?<![A-Za-z0-9])XML" + maphieuphat + "(?![A-Za-z0-9])")`.
   - Kiểm tra số tiền chuyển $\ge$ số tiền phạt.
   - Ghi nhận bản ghi `thanhtoan`, cập nhật `phieuphat.trangthai = 1`, hiển thị toast thành công và tự động đóng modal.

#### 3 Câu Hỏi Giảng Viên Hay Hỏi:
* **Hỏi 1: Ứng dụng Desktop không có IP công khai, làm sao nhận được Webhook từ ngân hàng?**
  * *Trả lời:* Vì Client Desktop nằm sau mạng LAN/NAT không mở cổng mạng ra ngoài Internet, hệ thống sử dụng cơ chế **Long Polling** gọi đến API SePay theo chu kỳ 3 giây/lần trong vòng tối đa 3 phút để kiểm tra danh sách biến động số dư.
* **Hỏi 2: Tại sao phải dùng Regex ranh giới `(?<!...)` và `(?!...)` khi kiểm tra nội dung chuyển khoản?**
  * *Trả lời:* Để chống lỗi so khớp nhầm chuỗi con. Nếu không dùng boundary regex, một giao dịch có nội dung `XML1` sẽ bị khớp nhầm vào người chuyển khoản với nội dung `XML10` hoặc `XML105`. Regex ranh giới đảm bảo mã giao dịch phải đứng độc lập chính xác $100\%$.
* **Hỏi 3: Làm thế nào chống việc người dùng dùng 1 giao dịch ngân hàng để thanh toán cho 2 phiếu phạt khác nhau?**
  * *Trả lời:* Bảng CSDL `thanhtoan` có ràng buộc duy nhất `UNIQUE (magiaodich)`. Khi cố tình ghi nhận lại một mã giao dịch đã từng được dùng, MySQL sẽ báo lỗi vi phạm khóa duy nhất và transaction bị hủy ngay lập tức.

---

### 8. Chức Năng Báo Cáo Thống Kê & Xuất Excel / In Ấn (F-12, YC-07, DEV-05)

#### Luồng Thực Thi Từng Bước:
1. **View:** Thủ thư/Admin chọn khoảng thời gian tại `ReportsDashboardView.java`.
2. **Thống Kê 4 Khối KPI:**
   - Tổng đầu sách, Lượt mượn đang kích hoạt, Lượt quá hạn, Doanh thu tiền phạt đã thu.
3. **Xuất File Excel POI (`ExcelReportExporter.java`):**
   - Sử dụng thư viện whitelist `org.apache.poi:poi-ooxml:5.2.5`.
   - Tạo workbook `.xlsx`, định dạng header màu hồng pastel `#DB2777`, tự động căn chỉnh độ rộng cột (`autoSizeColumn`), xuất bảng dữ liệu chi tiết kèm ngày giờ trích xuất.
4. **In Ấn Báo Cáo / In PDF:** Sử dụng `java.awt.print.PrinterJob` của JDK chuẩn để in ấn trực tiếp ra máy in hoặc lưu thành file PDF không cần thư viện ngoài.

#### 3 Câu Hỏi Giảng Viên Hay Hỏi:
* **Hỏi 1: Tại sao chọn định dạng `.xlsx` qua Apache POI mà không xuất file `.csv` cho đơn giản?**
  * *Trả lời:* File CSV không hỗ trợ định dạng cột, màu sắc bảng biểu, ô hợp nhất tiêu đề và rất dễ bị lỗi font chữ tiếng Việt có dấu trên Microsoft Excel nếu không cấu hình đúng UTF-8 BOM. Apache POI xuất file chuẩn Excel OpenXML `.xlsx`, chuyên nghiệp và giữ nguyên 100% tiếng Việt có dấu.
* **Hỏi 2: Khi khoảng thời gian chọn không có dữ liệu giao dịch nào thì ứng dụng xử lý thế nào?**
  * *Trả lời:* Hệ thống tuân thủ nguyên văn thông báo SRS: *"Không có phát sinh giao dịch trong mốc thời gian đã chọn"*, vô hiệu hóa nút xuất Excel để tránh sinh file rỗng.
* **Hỏi 3: Việc xuất file Excel báo cáo lớn có làm đơ giao diện đồ họa không?**
  * *Trả lời:* Tuân thủ quy tắc R-06, toàn bộ quá trình đọc dữ liệu DB và ghi file Excel POI đều chạy ngầm trên `SwingWorker`. Giao diện chính hiển thị trạng thái đang xử lý và chỉ kích hoạt thông báo thành công trên EDT sau khi file đã được ghi hoàn tất xuống ổ đĩa.

---

### 9. Kỹ Thuật Lập Trình Giao Diện Swing & Khắc Phục Lỗi UI (DJ-Raven Suite & JTable)

#### 4 Câu Hỏi Giảng Viên Hay Hỏi Về Kỹ Thuật UI:
* **Hỏi 1: Tại sao trong JTable cần phải tách riêng `TableCellRenderer` và `TableCellEditor` thành 2 class độc lập? Nếu gộp chung thì phát sinh lỗi gì?**
  * *Trả lời:* Trong cơ chế hoạt động của Swing `JTable`, Renderer dùng chung 1 component duy nhất để vẽ lần lượt từ trên xuống dưới cho tất cả các dòng trên màn hình (Flyweight Pattern). Nếu dùng chung 1 instance cho cả Editor và lưu biến độc giả trong instance đó, biến này sẽ luôn mang giá trị của dòng cuối cùng được vẽ. Khi người dùng click chuột vào nút bấm ở một dòng phía trên, sự kiện `actionPerformed` sẽ lấy nhầm dữ liệu của dòng cuối cùng dẫn đến thao tác sai mục tiêu (ví dụ: bấm Khóa thẻ dòng 1 nhưng lại gửi lệnh khóa thẻ dòng 15). Việc tách riêng Renderer (chỉ vẽ) và Editor (quản lý sự kiện cho đúng dòng đang active) đảm bảo tính toàn vẹn trạng thái 100%.
* **Hỏi 2: Làm thế nào để ngăn chặn hiện tượng cửa sổ popup modal bị mất chớp nhoáng ngay khi vừa mở?**
  * *Trả lời:* Trong thư viện `io.github.dj-raven:modal-dialog`, khi một modal mở lên, component `SimpleModalBorder` luôn tự động phát sự kiện khởi tạo ban đầu `modalOpened()` với mã hành động `action = 20` (`OPENED`). Nếu callback điều khiển viết theo dạng `if (action == OK) ... else controller.close()`, nhánh `else` sẽ bắt nhầm sự kiện `OPENED` và đóng cửa sổ ngay trong frame đầu tiên. Để khắc phục triệt để, callback bắt buộc phải chỉ định rõ chỉ đóng khi người dùng chủ động nhấn nút hủy: `else if (action == CANCEL_OPTION || action == CLOSE_OPTION) controller.close()`.
* **Hỏi 3: Làm thế nào để đảm bảo cửa sổ popup tùy biến (Custom Modal) không bị mép dưới cắt cụt các nút bấm Ok/Cancel?**
  * *Trả lời:* Trong `UiModalHelper.showCustomModal`, kích thước cửa sổ ModalDialog được tính toán bao gồm kích thước nội dung form (`content.getPreferredSize()`) cộng thêm khoảng đệm an toàn tối thiểu `150px` chiều cao (`content.height + 150`) và `56px` chiều rộng. Khoảng đệm này bù đắp chính xác cho chiều cao của tiêu đề header (~48px), thanh nút bấm footer (~56px) và khoảng cách insets viền ngoài (~40px), đảm bảo giao diện luôn hiển thị trọn vẹn và thoáng đãng trên mọi độ phân giải màn hình.
* **Hỏi 4: Tại sao dự án cấm dùng ký tự Unicode đặc biệt (như `✓`, `★`, `✕`) trực tiếp trong chuỗi text và giải pháp thay thế là gì?**
  * *Trả lời:* Các ký tự biểu tượng Unicode đặc biệt không được hỗ trợ đầy đủ trên bộ font chữ mặc định của hệ điều hành Windows (`Segoe UI` / `Tahoma`), dẫn đến lỗi hiển thị ô vuông rỗng (tofu glyph `▯`). Giải pháp chuẩn mực của dự án là loại bỏ toàn bộ ký tự này, thay thế bằng file vector SVG chuẩn (bộ Lucide Icons) lưu trong `resources/icons/svg/` và nạp động thông qua `FlatSVGIcon` kết hợp `ColorFilter` trong lớp `IconFactory`.
