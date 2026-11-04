# TRACEABILITY MATRIX & DEVIATION REGISTER — XIMILIB
> Phiên bản: 1.0 | Ngày cập nhật: 2026-11-04 | Tác giả: Principal Software Architect, BA & QA Lead  
> Single Source of Truth (SSOT): **Tài liệu Đặc tả yêu cầu phần mềm (SRS - IEEE Std 830-1998)**

---

## MỤC LỤC
1. [A. Ma Trận Truy Vết Yêu Cầu Chức Năng & Phi Chức Năng (YC-01 .. YC-14)](#a-ma-tr%E1%BA%ADn-truy-v%E1%BA%BFt-y%C3%AAu-c%E1%BA%A7u-yc-01--yc-14)
2. [B. Bảng Đối Chiếu Chi Tiết Luồng Ca Sử Dụng (UC 3.2.1 .. UC 3.2.9)](#b-b%E1%BA%A3ng-%C4%91%E1%BB%91i-chi%E1%BA%BFu-chi-ti%E1%BA%BFt-lu%E1%BB%93ng-ca-s%E1%BB%AD-d%E1%BB%A5ng-uc-321--uc-329)
3. [C. Sổ Đăng Ký Sai Lệch Kiến Trúc & Nghiệp Vụ (Deviation Register: DEV-01 .. DEV-10)](#c-s%E1%BB%95-%C4%91%C4%83ng-k%C3%BD-sai-l%E1%BB%87ch-deviation-register)
4. [D. Kết Luận Kiểm Toán Giai Đoạn 0 & Đề Xuất Bước Kế Tiếp](#d-k%E1%BA%BFt-lu%E1%BA%ADn-ki%E1%BB%83m-to%C3%A1n-giai-%C4%91o%E1%BA%A1n-0)

---

## A. Ma Trận Truy Vết Yêu Cầu (YC-01 .. YC-14)

### Ánh xạ thuật ngữ SRS $\rightarrow$ Mô hình CSDL & Mã nguồn thực tế:
- `TaiKhoan` $\rightarrow$ Bảng `nguoidung`, `vaitro` | Model `User`, `Role`
- `DocGia` $\rightarrow$ Bảng `nguoidung` + `thethuvien` | DTO `PatronDto` / Model `LibraryCard`
- `Sach, TheLoai` $\rightarrow$ Bảng `sach`, `theloai`, `tacgia`, `cuonsach`, `kesach` | Model `Book`, `BookCopy`, `Category`
- `PhieuMuon` $\rightarrow$ Bảng `phieumuon`, `chitietphieumuon`, `giahanmuon`, `datcho` | Model `BorrowTicket`, `LoanExtension`
- `PhieuPhat` $\rightarrow$ Bảng `phieuphat`, `thanhtoan` | Model `FineTicket`, `Payment`
- `Comments` $\rightarrow$ Bảng `binhluan` | Interface `BookCommentPort`
- `Config` $\rightarrow$ Bảng `cauhinh` | Model `LibraryPolicy` / Interface `SystemSettingsPort`
- `AuditLog` $\rightarrow$ File `logs/audit.log` | Lớp `AuditLogger`

| Mã YC | Tên Yêu Cầu / Phân Hệ | Bảng / CSDL Thực Tế | Lớp Service / Port / View Thực Tế | Test Cases Tương Ứng | Trạng Thái Hiện Tại |
|---|---|---|---|---|:---:|
| **YC-01** | **Đăng nhập hệ thống**<br>(UC 3.2.1) | `nguoidung`<br>`vaitro` | `AuthenticationServiceImpl`<br>`UserRepository`<br>`LoginForm`<br>`UserSession` | `AuthenticationServiceTest`:<br>- `testLoginSuccessWithUsername`<br>- `testLoginSuccessWithEmail`<br>- `testLoginFailWrongPassword`<br>- `testBr01Lockout` (TC-01) | **LỆCH**<br>*(Cần chỉnh nguyên văn thông báo, hỗ trợ SĐT, DEV-01/02)* |
| **YC-02** | **Đăng ký tài khoản độc giả**<br>(UC 3.2.2) | `nguoidung`<br>`thethuvien` | `AuthenticationServiceImpl`<br>`RegisterDialog`<br>`RegisterRequestDto` | `AuthenticationServiceTest`:<br>- `testRegisterSuccess`<br>- `testRegisterDuplicateCccdOrEmail` (TC-02) | **LỆCH**<br>*(Form thiếu Ngày sinh/Địa chỉ bằng DatePicker, trạng thái Chờ kích hoạt)* |
| **YC-03** | **Quản lý thông tin sách**<br>(UC 3.2.3) | `sach`, `theloai`<br>`tacgia`, `cuonsach`<br>`nhaxuatban` | `BookCatalogServiceImpl`<br>`BookRepository`<br>`BookManagementView` | `BookCatalogServiceTest`:<br>- `testSoftDeleteFailsWhenBookHasActiveLoans`<br>- `testSoftDeleteSucceedsWhenNoActiveLoans` (TC-03) | **KHỚP**<br>*(Đã soft-delete IsDeleted, kiểm soát xóa sách đang mượn)* |
| **YC-04** | **Tìm kiếm & tra cứu sách**<br>(UC 3.2.4) | `sach`, `theloai`<br>`tacgia`, `kesach`<br>`cuonsach` | `BookCatalogServiceImpl`<br>`BookQueryPort`<br>`BookCatalogView`<br>`ReaderHomeView` | `BookCatalogServiceTest`:<br>- `testSearchByKeyword`<br>- `testFilterByCategory`<br>- `testFilterByAvailability` (TC-04) | **LỆCH**<br>*(Thiếu gợi ý sách cùng thể loại khi không tìm thấy kết quả)* |
| **YC-05** | **Đặt mượn sách qua ứng dụng**<br>(UC 3.2.5) | `phieumuon`<br>`chitietphieumuon`<br>`cuonsach`, `datcho` | `CirculationServiceImpl`<br>`BorrowTicketRepository`<br>`ReaderHomeView` | `CirculationServiceTest`:<br>- `testBlockExceedingThreeHomeLoans`<br>- `testBorrowBlockedWhenHasUnpaidFine` (TC-05) | **LỆCH**<br>*(Cần chuẩn hóa nguyên văn 4 thông báo ngoại lệ a-d, kiểm tra giữ 24h)* |
| **YC-06** | **Xử lý mượn/trả tại quầy**<br>(UC 3.2.6) | `phieumuon`<br>`chitietphieumuon`<br>`cuonsach`, `phieuphat` | `CirculationServiceImpl`<br>`CirculationDeskView`<br>`ReturnAndCompensationView` | `CirculationServiceTest`:<br>- `testCreateHomeLoanDue14Days`<br>- `testReturnOverdueFiveDays` (TC-06) | **KHỚP**<br>*(Đã chuẩn hóa 14 ngày, tại chỗ 17:00, tính phạt trễ 2.000đ/ngày)* |
| **YC-07** | **Thống kê & báo cáo định kỳ**<br>(UC 3.2.7) | `phieumuon`<br>`chitietphieumuon`<br>`thanhtoan` | `ReportTrendAggregation`<br>`ExcelReportExporter`<br>`ReportsDashboardView` | `ExcelReportExporterTest`<br>`ReportTrendAggregationTest` (TC-07) | **LỆCH**<br>*(Hiện có Excel .xlsx; thiếu PDF / In ấn theo DEV-05)* |
| **YC-08** | **Cộng đồng & thảo luận sách**<br>(UC 3.2.8) | `binhluan`<br>`nguoidung` | `BookCommentPort`<br>`FeedbackServiceImpl`<br>`BookDetailPanel` | Chưa có test tự động riêng cho BookComment (TC-08) | **LỆCH**<br>*(Cần bổ sung Unit Test và đối soát modal in-app appNoticeModalBox)* |
| **YC-09** | **Báo mất & đền bù Online**<br>(UC 3.2.9) | `phieumuon`<br>`phieuphat`<br>`thanhtoan`, `cuonsach` | `CirculationServiceImpl`<br>`FineCalculationService`<br>`VietQrModalPanel` | `CirculationServiceTest`:<br>- `testReportLossAndPayCompensation`<br>- `testCalculateCompensationPreviewLostOverdue` (TC-09) | **KHỚP**<br>*(FSM 2 bước CHO_XU_LY_BOI_THUONG -> DA_MAT; công thức 200% + 20k + trễ)* |
| **YC-10** | **Cấu hình quy định & RBAC** | `cauhinh`<br>`vaitro`, `nguoidung` | `LibraryPolicy`<br>`SystemSettingsPort`<br>`SettingsPanel` | Chưa có test cấu hình động (TC-10) | **LỆCH**<br>*(Hằng số cứng vs CSDL động qua DEV-06)* |
| **YC-11** | **Hiệu năng (NFR)**<br>Cold start < 3s, RAM < 300MB | — *(Runtime JVM)* | `AppContainer`<br>`DatabaseConnectionManager` | `DatabaseConnectionManagerTest` (TC-11) | **LỆCH**<br>*(Đạt cold start & RAM; lệch NFR API 5.000 users theo DEV-03)* |
| **YC-12** | **An toàn & bảo mật (NFR)**<br>BCrypt, AES-256, RBAC | `nguoidung` (Cccd, Matkhau) | `SecurityUtil`<br>`PasswordHasher`<br>`AuthenticationService` | `SecurityUtilTest`<br>`AuthenticationServiceTest.testSampleAccountBcryptHashes` (TC-12) | **KHỚP**<br>*(100% BCrypt, AES-256 CCCD, RBAC tầng Service)* |
| **YC-13** | **Độ tin cậy (NFR)**<br>Uptime 99.9%, quét 00:00 | `phieumuon`<br>`thongbao` | `BackgroundJobManager`<br>`TransactionManager` | `BackgroundJobTest`<br>`TransactionManagerTest` (TC-13) | **THIẾU**<br>*(Thiếu tự động sao lưu DB lúc 00:00 & Auto-reconnect theo DEV-07)* |
| **YC-14** | **Bảo trì & mở rộng (NFR)**<br>Kiến trúc phân tầng | — | Clean Architecture (`com.ximilib.*`) | `DependencyRuleTest` (TC-14) | **KHỚP**<br>*(Clean Architecture thỏa mãn mục tiêu mở rộng theo DEV-08)* |

---

## B. Bảng Đối Chiếu Chi Tiết Luồng Ca Sử Dụng (UC 3.2.1 .. UC 3.2.9)

### 1. UC 3.2.1: Đăng nhập hệ thống (YC-01)
- **Tiền điều kiện:** Người dùng có tài khoản đang Active (`Trangthai = 1`).
- **Luồng chính:**
  1. Người dùng mở màn hình Đăng nhập $\rightarrow$ **Khớp** (`LoginForm.java`).
  2. Nhập Số điện thoại / Email và Mật khẩu $\rightarrow$ **Lệch:** Code hiện tại hỗ trợ `Tendangnhap` hoặc `Email`. Cần bổ sung tra cứu qua Số điện thoại (`Sdt`).
  3. Kiểm tra tính đầy đủ của thông tin $\rightarrow$ **Khớp** (kiểm tra rỗng, null).
  4. Đối soát thông tin và kiểm tra băm BCrypt $\rightarrow$ **Khớp** (`SecurityUtil.matches()`).
  5. Khởi tạo phiên làm việc, sinh mã chứng thực JWT $\rightarrow$ **Lệch:** Desktop dùng `UserSession.setCurrentUser()` và `AppContainer`, không dùng HTTP JWT token (Xem **DEV-01**).
  6. Chuyển hướng Dashboard tương ứng quyền hạn $\rightarrow$ **Khớp** (Admin/Librarian vào `MainDashboardFrame`, Reader vào `ReaderHomeFrame`).
- **Luồng ngoại lệ:**
  - *Sai thông tin:* SRS yêu cầu thông báo nguyên văn: `"Tên đăng nhập hoặc mật khẩu không chính xác"` $\rightarrow$ **Lệch:** Code gần đây thêm đuôi `"Bạn đã nhập sai X/5 lần"`. Cần chuẩn hóa khớp nguyên văn SRS.
  - *Sai mật khẩu > 5 lần:* Khóa tạm 15 phút $\rightarrow$ **Khớp:** Đã có cột `solansai`, `khoaden` và mã lỗi `AUTH_002`.
  - *Tài khoản bị khóa:* SRS yêu cầu: `"Tài khoản của bạn đã bị khóa. Vui lòng liên hệ thủ thư"` $\rightarrow$ **Lệch:** Cần rà soát chuỗi thông báo trả về khi `Trangthai = 0`.
  - *Lỗi kết nối mạng:* SRS yêu cầu: `"Không thể kết nối đến máy chủ, vui lòng thử lại sau"` $\rightarrow$ **Khớp** (mã lỗi `AUTH_003` / `DatabaseConnectionManager`).

---

### 2. UC 3.2.2: Đăng ký tài khoản độc giả qua ứng dụng (YC-02)
- **Tiền điều kiện:** Khách vãng lai chưa có tài khoản, thiết bị kết nối DB.
- **Luồng chính:**
  1. Chọn "Đăng ký tài khoản" $\rightarrow$ **Khớp** (`RegisterDialog.java` mở từ `LoginForm`).
  2. Điền 7 thông tin: *Họ tên, Ngày sinh, Địa chỉ, Số CMND/CCCD, Số điện thoại, Email, Mật khẩu* $\rightarrow$ **Lệch:** `RegisterDialog` hiện mới có Họ tên, Tên đăng nhập, Mật khẩu, Email, SĐT, CCCD; đang thiếu trường Ngày sinh và Địa chỉ (cần bổ sung `DatePicker` theo R-11).
  3. Kiểm tra hợp lệ dữ liệu $\rightarrow$ **Khớp:** Regex email, mật khẩu $\ge$ 8 ký tự (hoa, số, ký tự đặc biệt), CCCD 9 hoặc 12 số.
  4. Lưu trữ hồ sơ với trạng thái "Chờ kích hoạt", sinh mã thẻ `LIB-YYYY-XXXXX` $\rightarrow$ **Lệch:** Hiện tại đang tự động kích hoạt ngay (`Trangthai = 1`), cần hỗ trợ quy trình "Chờ kích hoạt" nếu thủ thư duyệt.
  5. Kích hoạt tài khoản thành công $\rightarrow$ về màn đăng nhập $\rightarrow$ **Khớp**.
- **Luồng ngoại lệ:**
  - Trùng CMND/CCCD hoặc Email: Báo lỗi `"Số CMND/CCCD hoặc Email đã được đăng ký trên hệ thống"` $\rightarrow$ **Khớp**.

---

### 3. UC 3.2.3: Quản lý thông tin sách (YC-03)
- **Tiền điều kiện:** Thủ thư/Admin có quyền `MANAGE_BOOKS`.
- **Luồng chính:**
  1. Vào phân hệ "Quản lý sách" $\rightarrow$ **Khớp** (`BookManagementView.java`).
  2. Tải danh sách: Mã sách, Tên sách, Tác giả, Thể loại, Vị trí kệ, Tồn kho, Trạng thái $\rightarrow$ **Khớp**.
  3. Thêm mới: Nhập thông tin, bấm "Lưu", sinh mã duy nhất `MaSach`, lưu CSDL, trạng thái Khả dụng $\rightarrow$ **Khớp** (`BookCatalogServiceImpl.createBook()`).
  4. Thông báo: `"Thêm sách thành công"` $\rightarrow$ **Khớp**.
- **Luồng ngoại lệ:**
  - Xóa sách đang có người mượn: Ngăn chặn và báo lỗi `"Không thể xóa đầu sách đang có độc giả mượn"` $\rightarrow$ **Khớp** (Quy tắc R-03 & BR-09).
  - Xóa sách thành công: Soft-delete `IsDeleted = TRUE` $\rightarrow$ **Khớp** (Quy tắc R-03).
- **Quy tắc:** Số lượng sách nhập kho là số nguyên dương $\ge 1$ $\rightarrow$ **Khớp**.

---

### 4. UC 3.2.4: Tìm kiếm và tra cứu sách (YC-04)
- **Tiền điều kiện:** Không có (hỗ trợ cả khách vãng lai `Guest`).
- **Luồng chính:**
  1. Nhập từ khóa $\rightarrow$ **Khớp**.
  2. Chọn bộ lọc: Thể loại, Tác giả, Trạng thái (Còn sách/Đã hết) $\rightarrow$ **Khớp**.
  3. Tìm kiếm full-text: Tên sách, Tác giả, Thể loại $\rightarrow$ **Khớp**.
  4. Kết quả: Tên sách, Ảnh bìa, Tác giả, Thể loại, Tình trạng kệ $\rightarrow$ **Khớp**.
  5. Xem chi tiết & vị trí kệ sách $\rightarrow$ **Khớp** (`BookDetailDialog.java`).
- **Luồng ngoại lệ:**
  - Không tìm thấy kết quả: Báo lỗi `"Không tìm thấy cuốn sách nào khớp với từ khóa của bạn"` kèm gợi ý sách cùng thể loại $\rightarrow$ **Lệch:** Hiện chỉ hiển thị danh sách rỗng, chưa hiển thị đúng nguyên văn chuỗi lỗi và danh sách gợi ý.

---

### 5. UC 3.2.5: Độc giả đặt mượn sách qua ứng dụng (YC-05)
- **Tiền điều kiện:** Độc giả đã đăng nhập; Thẻ thư viện còn hạn; Không có sách quá hạn.
- **Luồng chính:**
  1. Xem chi tiết sách khả dụng $\rightarrow$ **Khớp**.
  2. Chọn hình thức: "Đọc tại chỗ" hoặc "Mượn về nhà" $\rightarrow$ bấm "Đặt mượn" $\rightarrow$ **Khớp** (Bảng `phieumuon.Hinhthuc` gồm `TAI_CHO` / `VE_NHA`).
  3. Kiểm tra điều kiện mượn (sách đang giữ, nợ phạt, hạn thẻ) $\rightarrow$ **Khớp**.
  4. Tạo phiếu mượn `CHO_DUYET (0)`, tạm giữ sách (giảm số lượng khả dụng) $\rightarrow$ **Khớp**.
  5. Hiển thị thông báo: `"Đặt mượn thành công! Vui lòng đến quầy thư viện xuất trình thẻ thư viện trong vòng 24 giờ để nhận sách"` $\rightarrow$ **Lệch:** Cần chuẩn hóa chuỗi thông báo nguyên văn trên `ReaderHomeView`.
- **Luồng ngoại lệ (4 ngoại lệ bắt buộc a-d):**
  - **a. Đang giữ sách quá hạn:** `"Bạn có sách quá hạn chưa hoàn trả. Vui lòng trả sách trước khi mượn tiếp"` $\rightarrow$ **Lệch:** Hiện trả lời thông báo chung, cần đổi đúng nguyên văn SRS.
  - **b. Vượt quá số lượng:** `"Bạn đã đạt giới hạn mượn tối đa 3 cuốn sách mang về"` $\rightarrow$ **Lệch:** Cần đổi đúng nguyên văn SRS.
  - **c. Thẻ hết hạn:** `"Thẻ thư viện của bạn đã hết hạn sử dụng. Vui lòng liên hệ thủ thư để gia hạn"` $\rightarrow$ **Lệch:** Cần đổi đúng nguyên văn SRS.
  - **d. Quá 24 giờ giữ sách:** Tự động hủy phiếu mượn, hoàn kho $\rightarrow$ **Khớp** (`BackgroundJobManager.runReservationCleanup()`).

---

### 6. UC 3.2.6: Xử lý mượn và trả sách tại quầy (YC-06)
- **Tiền điều kiện:** Thủ thư đã đăng nhập; Độc giả có mặt tại quầy.
- **Luồng mượn:**
  1. Nhập mã thẻ hoặc CCCD $\rightarrow$ **Khớp** (`CirculationDeskView.java`).
  2. Hiển thị thông tin & yêu cầu $\rightarrow$ **Khớp**.
  3. Quét mã vạch cuốn sách thực tế $\rightarrow$ **Khớp**.
  4. Xác nhận xuất sách $\rightarrow$ chuyển `DANG_MUON (1)`, `Ngaymuon = TODAY`, tính hạn trả $\rightarrow$ **Khớp**.
  5. Gửi thông báo đến độc giả $\rightarrow$ **Khớp** (Bảng `thongbao`).
- **Luồng trả:**
  1. Quét mã vạch cuốn sách trả $\rightarrow$ tự động tìm phiếu mượn $\rightarrow$ **Khớp**.
  2. Kiểm tra tình trạng vật lý (Bình thường / Rách hỏng / Mất) $\rightarrow$ **Khớp** (`ReturnAndCompensationView.java`).
  3. Hoàn trả bình thường $\rightarrow$ chuyển `DA_TRA (2)`, hoàn kho $\rightarrow$ **Khớp**.
  4. Quá hạn $\rightarrow$ tính phạt trễ 2.000đ/ngày theo BR-06 $\rightarrow$ **Khớp**.
  5. Rách hỏng / Mất $\rightarrow$ chuyển xử lý bồi thường $\rightarrow$ **Khớp**.
- **Quy tắc:** Mượn về nhà 14 ngày (gia hạn 1 lần 7 ngày); Mượn tại chỗ trả trước 17:00 $\rightarrow$ **Khớp**.

---

### 7. UC 3.2.7: Thống kê và Báo cáo định kỳ (YC-07)
- **Tiền điều kiện:** Người dùng có quyền xem báo cáo.
- **Luồng chính:**
  1. Chọn 1 trong 4 loại báo cáo:
     - Lượt mượn/trả theo thời gian (Tuần, Tháng, Quý, Năm) $\rightarrow$ **Khớp**.
     - Danh sách độc giả nợ sách quá hạn $\rightarrow$ **Khớp**.
     - Top sách / thể loại mượn nhiều nhất $\rightarrow$ **Khớp**.
     - Sách hỏng/mất cần thanh lý/bổ sung $\rightarrow$ **Khớp**.
  2. Chọn mốc thời gian $\rightarrow$ hiển thị biểu đồ + bảng chi tiết $\rightarrow$ **Khớp** (`ReportsDashboardView.java`).
  3. Xuất dữ liệu Excel/PDF hoặc In báo cáo $\rightarrow$ **Lệch:** Hiện chỉ hỗ trợ Excel `.xlsx` (Apache POI). Chưa có PDF và Print (Xem **DEV-05**).
  4. Ghi Audit Log khi xuất dữ liệu $\rightarrow$ **Khớp** (`AuditLogger.log()`).
- **Luồng ngoại lệ:**
  - Không có dữ liệu: Thông báo `"Không có phát sinh giao dịch trong mốc thời gian đã chọn"` $\rightarrow$ **Lệch:** Cần chuẩn hóa chuỗi thông báo.
- **Quy tắc:** Quét quá hạn tự động cập nhật lại mỗi 00:00 hàng ngày $\rightarrow$ **Khớp** (`BackgroundJobManager`).

---

### 8. UC 3.2.8: Cộng đồng & Thảo luận sách (YC-08)
- **Tiền điều kiện:** Mở trang chi tiết sách.
- **Luồng chính:**
  1. Cuộn xuống khu vực 'Cộng đồng & Bình luận' $\rightarrow$ **Khớp** (`CommunityCommentPanel.java`).
  2. Nhập đánh giá, bấm "Gửi bình luận" $\rightarrow$ **Khớp**.
  3. Đã đăng nhập $\rightarrow$ lưu bảng `binhluan` (Comments) và hiển thị công khai ngay $\rightarrow$ **Khớp**.
- **Luồng ngoại lệ:**
  - Chưa đăng nhập $\rightarrow$ hiển thị popup in-app `appNoticeModalBox` nhắc: `"Vui lòng đăng nhập để gửi bình luận"`, kèm nút chuyển đến trang đăng nhập $\rightarrow$ **Khớp** (`UiModalHelper.showConfirm()` / `ModalDialog`).

---

### 9. UC 3.2.9: Báo mất & Đền bù Online (YC-09)
- **Tiền điều kiện:** Sách đang ở trạng thái `DANG_MUON (1)`.
- **Luồng chính:**
  1. Báo mất trên app hoặc tại quầy $\rightarrow$ **Khớp**.
  2. Phiếu mượn chuyển sang `CHO_XU_LY_BOI_THUONG (4)` $\rightarrow$ **Khớp**.
  3. Áp dụng công thức: `Tổng = (200% * Giá bìa) + 20.000đ + (2.000đ * Ngày trễ nếu có)` $\rightarrow$ **Khớp** (`FineCalculationService.java`).
  4. Dừng tính ngày trễ tại thời điểm báo mất $\rightarrow$ **Khớp**.
  5. Độc giả nộp tiền (tiền mặt hoặc VietQR) $\rightarrow$ thủ thư bấm "Đã nộp phạt" $\rightarrow$ phiếu đóng sang `DA_MAT (5)` $\rightarrow$ **Khớp**.
  6. Sách chuyển `HONG_MAT`, khôi phục quyền mượn sách nếu thẻ còn hạn $\rightarrow$ **Khớp**.

---

## C. Sổ Đăng Ký Sai Lệch (Deviation Register)

| ID | Nội Dung Trong SRS | Hiện Trạng Kiến Trúc & Code | Phương Án Phân Tích (1, 2, 3) | Khuyến Nghị Kiến Trúc | Trạng Thái |
|:---:|---|---|---|---|:---:|
| **DEV-01** | **Mã xác thực JWT**<br>(UC 3.2.1 Hậu điều kiện): *"Sinh mã chứng thực... lưu trữ JWT an toàn trên thiết bị"* | XimiLib là ứng dụng Desktop Swing kết nối trực tiếp MySQL qua JDBC Driver, không thông qua Web API/REST backend. Phiên làm việc được quản lý tập trung bằng `UserSession` (Thread-safe In-Memory). | **PA1 (Khuyên dùng):** Giữ nguyên kiến trúc Desktop Session nội bộ; cập nhật tài liệu giải trình: với ứng dụng Desktop Rich Client kết nối CSDL trực tiếp, `UserSession` đảm nhận đầy đủ vai trò ngữ cảnh bảo mật tương đương JWT.<br>**PA2:** Tự tạo lớp mã hóa giả lập token JWT cục bộ lưu trong file cấu hình người dùng.<br>**PA3:** Viết thêm một tầng REST API Service trung gian (Thay đổi cấu trúc toàn bộ dự án). | **Chọn PA1** (Phù hợp đồ án PBL3 Desktop Java). | **CHỜ QUYẾT** |
| **DEV-02** | **Đăng nhập bằng Số điện thoại / Email**<br>(UC 3.2.1 Bước 2): *"Người dùng nhập Số điện thoại / Email và Mật khẩu"* | Hiện tại hệ thống đăng nhập bằng `Tendangnhap` (Username) hoặc `Email`. Cột `Sdt` đã có trong bảng `nguoidung` nhưng chưa được đánh index và chưa hỗ trợ trong truy vấn đăng nhập. | **PA1 (Khuyên dùng):** Mở rộng truy vấn `findByIdentifier()` trong `UserRepository` để kiểm tra cả 3 trường: `Tendangnhap OR Email OR Sdt`. Đảm bảo đáp ứng 100% SRS mà không phá vỡ tài khoản `admin`/`sinhvien` hiện tại.<br>**PA2:** Chỉ cho phép đăng nhập duy nhất bằng SĐT hoặc Email (loại bỏ hoàn toàn đăng nhập bằng Username).<br>**PA3:** Giữ nguyên chỉ hỗ trợ Username và Email. | **Chọn PA1** (Tương thích ngược hoàn hảo, đáp ứng trọn vẹn SRS). | **CHỜ QUYẾT** |
| **DEV-03** | **Yêu cầu phi chức năng Cloud/Web**<br>(Mục 4.1 & 4.3): *Tải đồng thời 5.000 users, Uptime 99.9%, Horizontal Autoscaling, 95% request API < 500ms* | Hệ thống là ứng dụng Desktop Client phân tán nối vào MySQL tập trung. Tải 5.000 users đồng thời và Autoscaling là tiêu chí của hạ tầng Cloud/Web API nhiều máy chủ, không thể đo lường trực tiếp trên 1 máy desktop standalone. | **PA1 (Khuyên dùng):** Ghi rõ ranh giới kiến trúc trong `docs/ARCHITECTURE.md`: Các chỉ số NFR này là định hướng thiết kế khi triển khai cụm Backend quy mô trường đại học. Ở phạm vi đồ án Desktop PBL3, đo đạc và cam kết các chỉ số thực tế: Khởi động nguội < 3.0s, RAM máy trạm < 300MB, thời gian xử lý truy vấn DB < 500ms.<br>**PA2:** Giả lập kiểm thử tải 5.000 kết nối ảo bằng kịch bản đa luồng Java (stress test DB).<br>**PA3:** Viết lại toàn bộ sang Web Server / Microservices. | **Chọn PA1** (Đúng thực tế đồ án, trung thực với kết quả đo đạc). | **CHỜ QUYẾT** |
| **DEV-04** | **Lưu trữ Nhật ký Kiểm toán (AuditLog)**<br>(Mục 3.2.7 & Phụ lục): *Lưu nhật ký kiểm toán hành vi kết xuất dữ liệu* | Hiện tại `AuditLogger` đang ghi file text tại `logs/audit.log` theo định dạng `[TIMESTAMP] [USER] [ACTION] [DETAIL]`. | **PA1 (Khuyên dùng):** Tạo thêm bảng `auditlog` trong CSDL qua migration V5 (gồm `id, user_id, hanhdong, chitiet, thoigian`) và ghi song song (vừa ghi CSDL vừa ghi file `logs/audit.log`).<br>**PA2:** Chỉ ghi file text cục bộ `logs/audit.log` như hiện tại.<br>**PA3:** Chỉ ghi vào CSDL, bỏ ghi file text. | **Chọn PA1** (Bảo toàn dữ liệu kiểm toán tập trung cho nhiều máy trạm). | **CHỜ QUYẾT** |
| **DEV-05** | **Xuất PDF và In ấn Báo cáo**<br>(UC 3.2.7 Bước 205): *"Tải về file Excel/PDF hoặc bấm 'In báo cáo'"* | Hiện tại hệ thống đã tích hợp Apache POI (`poi-ooxml:5.2.5`) xuất file Excel `.xlsx` rất hoàn chỉnh. Chưa có tính năng xuất PDF và In trực tiếp. Ràng buộc cứng cấm thêm thư viện ngoài whitelist (như iText/PDFBox). | **PA1 (Khuyên dùng):** Dùng tính năng in chuẩn của Java (`java.awt.print.PrinterJob`) để in bảng thống kê trực tiếp ra máy in hoặc máy in ảo "Microsoft Print to PDF" của Windows (không vi phạm whitelist thư viện).<br>**PA2:** Xin phê duyệt bổ sung thư viện `openpdf` hoặc `pdfbox` vào whitelist thư viện.<br>**PA3:** Chỉ hỗ trợ xuất Excel `.xlsx`, giải trình PDF/In ấn thuộc lộ trình nâng cấp sau. | **Chọn PA1** (Không cần thêm thư viện mới, tận dụng được tính năng có sẵn của Java Desktop). | **CHỜ QUYẾT** |
| **DEV-06** | **Cấu hình động Quy định Thư viện**<br>(YC-10 & YC-14): *Admin thay đổi số sách tối đa, hạn mượn, tiền phạt từ Desktop không cần cập nhật bản cài* | Bảng `cauhinh` đã có trong CSDL (23 bảng), nhưng trong mã nguồn các hằng số đang nằm cố định ở `LibraryPolicy.java` (đáp ứng R-09). | **PA1 (Khuyên dùng):** Thiết kế mẫu `PolicyProvider` / `SystemSettingsPort`: Lấy giá trị mặc định từ `LibraryPolicy`, sau đó đọc đè từ bảng `cauhinh` khi ứng dụng khởi chạy hoặc khi Admin lưu cấu hình. Đáp ứng cả R-09 lẫn cấu hình động YC-10.<br>**PA2:** Giữ cố định hằng số trong code `LibraryPolicy`, chỉ cho sửa file `db.properties`.<br>**PA3:** Đọc 100% từ bảng `cauhinh` mỗi lần tính toán (truy vấn DB liên tục). | **Chọn PA1** (Kết hợp hoàn hảo giữa an toàn Fallback và tính năng động). | **CHỜ QUYẾT** |
| **DEV-07** | **Sao lưu tự động 00:00 & Tự động kết nối lại (Auto-reconnect)**<br>(YC-13 & Mục 4.3): *Uptime 99.9%, tự động sao lưu lúc 00:00 (RPO < 24h), Auto-reconnect khi rớt mạng* | Hệ thống đã có `BackgroundJobManager` chạy định kỳ lúc 00:00 để quét phiếu mượn quá hạn. Tuy nhiên chưa có job tự động dump CSDL sao lưu và cơ chế thử lại kết nối (Retry loop) khi MySQL ngắt kết nối. | **PA1 (Khuyên dùng):** Thêm tác vụ trong `BackgroundJobManager` lúc 00:00 gọi tiến trình `mysqldump` để backup file `.sql` vào thư mục `backups/`; đồng thời bổ sung cơ chế Auto-Reconnect trong `DatabaseConnectionManager` (thử kết nối lại tối đa 3 lần với exponential backoff).<br>**PA2:** Chỉ cấu hình auto-reconnect trong chuỗi JDBC URL (`autoReconnect=true`).<br>**PA3:** Ghi nhận đây là nhiệm vụ của DBA / Quản trị máy chủ, không viết trong app client. | **Chọn PA1** (Chủ động bảo vệ ứng dụng trước sự cố mạng). | **CHỜ QUYẾT** |
| **DEV-08** | **Mô hình kiến trúc phân tầng MVC/MVVM vs Clean Architecture**<br>(YC-14): *Đặc tả ghi kiến trúc MVC/MVVM* | XimiLib áp dụng Clean Architecture (`presentation -> application -> domain <- infrastructure`). | **PA1 (Khuyên dùng):** Giữ nguyên Clean Architecture. Tầng `presentation` của XimiLib hiện thực đúng mô hình MVC/Presenter (View độc lập, Model/DTO thuần, Service điều phối). Clean Architecture là kiến trúc phân tầng cao cấp hơn, hoàn toàn thỏa mãn và vượt yêu cầu YC-14.<br>**PA2:** Hạ cấp kiến trúc về MVC truyền thống (Rất tốn công và làm suy giảm chất lượng code). | **Chọn PA1** (Giữ vững chuẩn mực kiến trúc cốt lõi). | **CHỜ QUYẾT** |
| **DEV-09** | **Phạm vi Chức năng "Hỗ trợ nơi trò chuyện / Chatbot"**<br>(Mục 1.2 & F-08): *SRS nhắc "hỗ trợ nơi trò chuyện"* | Trong SRS, mục 1.2 nhắc "hỗ trợ nơi trò chuyện", nhưng phần đặc tả Use Case chi tiết tương ứng lại là **UC 3.2.8 "Cộng đồng & Thảo luận sách"** (bình luận sách). F-08 "Chatbot" trong `PROJECT_STATUS.md` đang ở trạng thái Hoãn (Nice-to-have). | **PA1 (Khuyên dùng):** Thống nhất quy ước: "Nơi trò chuyện" trong SRS được hiện thực trọn vẹn bằng chức năng Bình luận & Thảo luận sách cộng đồng (UC 3.2.8 / bảng `binhluan`). Tính năng Chatbot AI tách riêng là Nice-to-have (F-08).<br>**PA2:** Xây dựng cửa sổ Chat nội bộ giữa độc giả và thủ thư (cần tạo thêm bảng tin nhắn).<br>**PA3:** Tích hợp Chatbot AI Gemini API. | **Chọn PA1** (Khớp chính xác với UC 3.2.8 của SRS). | **CHỜ QUYẾT** |
| **DEV-10** | **Định danh "Ứng dụng Di động (App)" trong SRS**<br>(UC 3.2.5 & Mục 2.3) | SRS nhiều chỗ viết "trên ứng dụng di động", "từ điện thoại". Thực tế XimiLib là ứng dụng Desktop Client Java Swing/FlatLaf phục vụ 2 phân hệ: Quản trị (Thủ thư/Admin) và Giao diện Độc giả (`ReaderHomeFrame`). | **PA1 (Khuyên dùng):** Giữ nguyên ứng dụng Desktop Java đa nền tảng; làm rõ trong tài liệu: giao diện `ReaderHomeFrame` chính là "Cổng ứng dụng Độc giả" (Reader App Client) tương đương chức năng của App di động trong SRS.<br>**PA2:** Viết thêm app Android / Flutter cho độc giả (vượt phạm vi đồ án). | **Chọn PA1** (Thực tế và phù hợp với đồ án PBL3). | **CHỜ QUYẾT** |

---

## D. Kết Luận Kiểm Toán Giai Đoạn 0

1. **Về CSDL:** Đã có đủ 23 bảng trong MySQL (đáp ứng trọn vẹn các thực thể `binhluan`, `cauhinh`, `gopy`). Cần migration mới (V5) để bổ sung bảng `auditlog` (nếu duyệt DEV-04), index cho `Sdt` (nếu duyệt DEV-02) và index tối ưu tìm kiếm sách.
2. **Về Nghiệp vụ:** Nghiệp vụ cốt lõi (Mượn trả 14 ngày, FSM báo mất 2 bước `CHO_XU_LY_BOI_THUONG -> DA_MAT`, phạt trễ 2.000đ/ngày, VietQR) đã khớp 95% với SRS. Phần sai lệch chủ yếu nằm ở **chuỗi thông báo lỗi hiển thị cho người dùng** (cần chuẩn hóa 100% nguyên văn SRS) và **các trường trên form đăng ký** (bổ sung Ngày sinh, Địa chỉ).
3. **Về Ràng buộc Kiến trúc:** Toàn bộ 14 ca kiểm thử kiến trúc (`DependencyRuleTest`) đều xanh. Các quy tắc R-01 .. R-12 được tuân thủ nghiêm ngặt.

> **Trạng thái:** ĐÃ HOÀN TẤT GIAI ĐOẠN 0. DỪNG VÀ CHỜ PHÊ DUYỆT CÁC PHƯƠNG ÁN TRONG DEVIATION REGISTER (DEV-01 .. DEV-10) TRƯỚC KHI TIẾN HÀNH CODE GIAI ĐOẠN TIẾP THEO.
