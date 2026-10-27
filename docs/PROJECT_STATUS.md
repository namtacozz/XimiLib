# Version: 1.3 | Last updated: 2026-10-27 | Owner: Architecture Team | Related files: ../AGENTS.md, ARCHITECTURE.md, DATABASE_SCHEMA.md, BUSINESS_RULES.md, VAN_DAP.md

# PROJECT_STATUS.md — Trạng thái Dự án & Lộ trình Phát triển XimiLib

## 1. Ma trận Tính năng Dự án (Feature Matrix F-01 đến F-12 & SRS YC-01..YC-14)

Dự án phân cấp tính năng theo chuẩn độ ưu tiên MoSCoW (Must / Should / Nice) bám sát 100% file Đặc tả Yêu cầu:

| Mã | Tên Tính năng / Module | Ưu tiên | Trạng thái | Phụ trách chính | Mô tả tóm tắt kỹ thuật & Phân quyền RBAC |
|---|---|---|---|---|---|
| **F-01** | **Quản lý Đầu sách & Bản sao (YC-03, YC-04)** | **Must** | **Hoàn thành (100%)** | Phan Vũ Xuân Kiên (FE/DB) | Giao diện Kho sách (`BookCatalogView`), Quản lý Thể loại (`CategoryManagementView`), Thùng rác & Khôi phục (`TrashAndRestoreView`). 221 đầu sách, 663 cuốn copy (`cuonsach`), phân trang server-side, bộ lọc Thể loại/Tình trạng, soft-delete BR-09 và cơ chế Observer. Phân quyền: Admin + Thủ thư được CRUD; Độc giả chỉ tra cứu & bấm mượn. |
| **F-02** | **Quản lý Mượn - Trả sách (YC-05, YC-06, YC-09)** | **Must** | **Hoàn thành (100%)** | Lê Võ Thành Nam (BE) | Bàn lưu hành quầy (`CirculationDeskView`), Xử lý trả & bồi hoàn (`ReturnAndCompensationView`), Đăng ký mượn sách App (`ReaderBorrowRegistrationView`), Phiếu mượn của tôi (`ReaderMyLoansView`). Khóa bi quan `SELECT ... FOR UPDATE` (R-05), hạn mức 3 cuốn/14 ngày (BR-02), mượn tại chỗ trả 17:00 (BR-04), phạt trễ 2.000đ/ngày (BR-06), chặn nợ phạt (BR-07), bồi thường 200% (BR-08), FSM báo mất 2 bước (R-10). |
| **F-03** | **Tài khoản & Phân quyền RBAC (YC-01, YC-02, YC-10)** | **Must** | **Hoàn thành (100%)** | Nguyễn Việt Nhật (Lead) | Đăng nhập/Đăng ký phân đôi 50/50 chuẩn FlatLaf, băm mật khẩu BCrypt (R-08), khóa 15 phút sau 5 lần sai (BR-01). Màn hình `UserRoleManagementView` dành riêng cho Admin quản lý tài khoản nhân viên (Admin, Thủ thư), phân quyền vai trò RBAC và khóa/mở tài khoản. |
| **F-04** | **Quản lý Thẻ Thư viện (YC-02, F-04)** | **Must** | **Hoàn thành (100%)** | Phan Vũ Xuân Kiên (FE/DB) | Cấp thẻ tự động khi đăng ký (`LIB-YYYY-XXXXX`, hạn 365 ngày), màn hình Hồ sơ độc giả & Quản lý thẻ (`PatronManagementView`), khóa/mở thẻ, gia hạn thẻ, Digital Card Popup QR. |
| **F-05** | **Thanh toán Trực tuyến VietQR (YC-06, YC-09)** | **Should** | **Hoàn thành (100%)** | Lê Võ Thành Nam (BE) | Sinh mã VietQR động theo chuẩn Napas247 (`VietQrModalPanel`), hiển thị thông tin MB Bank và mã nội dung `XML{Maphieuphat}`, đối soát chuyển khoản, lưu lịch sử vào bảng `thanhtoan`. |
| **F-06** | **Ghi Log Kiểm toán Hệ thống (F-06, YC-07)** | **Should** | **Hoàn thành (100%)** | Lê Võ Thành Nam (BE) | Triển khai `AuditLogger` an toàn luồng, ghi nhật ký truy cập, biến động mượn/trả, nộp phạt, báo mất và kết xuất báo cáo vào `logs/audit.log` (R-08). |
| **F-07** | **Gợi ý Sách theo Sở thích** | **Nice** | **Hoàn thành (80%)** | Phan Vũ Xuân Kiên (FE/DB) | Khối sách mới nhập và sách nổi bật trên Dashboard hiển thị theo danh mục. |
| **F-08** | **Chatbot Hỗ trợ Độc giả** | **Nice** | **Tạm hoãn (Scope)** | Nguyễn Việt Nhật (Lead) | Không triển khai; hệ thống sử dụng hộp thông báo nội bộ qua bảng `thongbao` theo AGENTS.md §2. |
| **F-09** | **Bảng Xếp hạng Sách Mượn nhiều** | **Nice** | **Hoàn thành (100%)** | Phan Vũ Xuân Kiên (FE/DB) | Báo cáo Top 10 đầu sách mượn nhiều nhất tích hợp trong màn hình Thống kê. |
| **F-10** | **Góp ý & Đánh giá Thư viện** | **Nice** | **Hoàn thành (90%)** | Phan Vũ Xuân Kiên (FE/DB) | Tích hợp bảng `gopy` và form phản hồi trực tiếp. |
| **F-11** | **Đánh giá & Bình luận Sách (YC-08)** | **Nice** | **Hoàn thành (100%)** | Lê Võ Thành Nam (BE) | Màn hình `BookDetailModalPanel`: hiển thị vị trí kệ sách (`khuvuc`, `kesach`), điểm sao trung bình (1-5 ⭐) và nhận xét của bạn đọc từ bảng `binhluan`, cho phép gửi đánh giá mới và nút mượn sách nhanh. |
| **F-12** | **Thống kê & Báo cáo Định kỳ (YC-07)** | **Should** | **Hoàn thành (100%)** | Phan Vũ Xuân Kiên (FE/DB) | Màn hình `ReportsDashboardView`: 4 KPI tổng quan, 4 loại báo cáo nghiệp vụ (lượt mượn trả, độc giả quá hạn, sách hư hỏng, top sách), xuất file Excel (.csv UTF-8 BOM) và in báo cáo kèm Audit log. |

---

## 2. Tiêu chuẩn Hoàn thành (Definition of Done - DoD)
Đã đạt **100% 6 tiêu chí DoD**:
1. **Kiến trúc:** Tuân thủ Clean Architecture 4 tầng, Pure DI tại `AppContainer`, không có bất kỳ import ngược nào giữa các tầng.
2. **Kiểm thử tự động:** **45/45 test cases JUnit 5 đạt 100% PASS** (Unit tests + Integration tests MySQL thật).
3. **An toàn dữ liệu:** 100% `PreparedStatement`, mật khẩu băm BCrypt, tiền tệ dùng `BigDecimal`, khóa bi quan `SELECT ... FOR UPDATE`.
4. **Không chặn giao diện:** Mọi truy vấn DB và I/O đều chạy trên `SwingWorker` ngoài EDT; giao diện cập nhật an toàn trên EDT. Khởi động nguội < 1 giây.
5. **Đóng gói & Chạy thử:** Lệnh `mvn clean package` xuất ra file `dist/XimiLib.jar` độc lập (16.0 MB) chạy trực tiếp bằng `run.bat`.
6. **Tài liệu:** Đầy đủ bộ tài liệu SSOT trong `/docs` (`AGENTS.md`, `ARCHITECTURE.md`, `DATABASE_SCHEMA.md`, `BUSINESS_RULES.md`, `PROJECT_STATUS.md`, `VAN_DAP.md`).

---

## 3. Nhật ký Thay đổi Dự án (Changelog)

### [Sprint 13 — Hoàn thiện Trọn bộ Tính năng Đặc tả & Phân quyền RBAC] - 2026-10-27
#### Added
- **YC-10 (Quản lý Tài khoản & Phân quyền RBAC)**:
  * Xây dựng màn hình `UserRoleManagementView` dành riêng cho Quản trị viên (Admin).
  * Hỗ trợ tìm kiếm, lọc theo vai trò (ADMIN, LIBRARIAN, READER), cấp tài khoản nhân viên mới (mật khẩu băm BCrypt), chuyển đổi vai trò RBAC và khóa/mở tài khoản (`Trangthai = 0/1`).
- **YC-05 (Độc giả Đặt mượn sách qua App & Đặt trước khi hết sách)**:
  * Xây dựng `ReaderBorrowRegistrationView` bám sát luồng nghiệp vụ 3.2.5.
  * Tích hợp tính năng Đặt chỗ / Giữ sách (Reservation) khi hết bản sao trên kệ (chuyển nút thành "ĐẶT TRƯỚC SÁCH", lưu vào bảng `datcho`).
  * Bổ sung nút "Đăng ký mượn cuốn này (App)" ngay trong `BookDetailModalPanel`.
- **YC-09 (Báo mất sách & Đền bù Online)**:
  * Bổ sung nút "Báo mất" vào từng phiếu mượn đang mượn trên `ReaderMyLoansView`.
  * Hiển thị bảng kê chi tiết bồi hoàn (200% giá gốc + 20.000đ phí kỹ thuật + phạt quá hạn theo BR-08).
  * Tích hợp popup thanh toán VietQR động (`VietQrModalPanel`) để độc giả thanh toán trực tuyến ngay trên ứng dụng.
- **Rà soát Phân quyền RBAC Giao diện (Strict RBAC UI)**:
  * Rà soát `SidebarPanel` và HeaderBar: Độc giả không nhìn thấy các nút quản trị (nút "+ Thêm Sách" được thay bằng "💳 Thẻ Số" trên header; các mục Desk, Thống kê, Cài đặt quy định chỉ hiển thị cho Thủ thư và Admin).
  * `Tài khoản & Phân quyền (RBAC)` và `Cài đặt quy định (SRS)` chỉ dành riêng cho Quản trị viên.
- **Sửa Triệt để Lỗi Popup Tài khoản Flash/Ẩn Tức thì**:
  * Chuyển đổi `UserProfileModalPanel` kế thừa `GlassPaneChild` và tích hợp In-App Overlay chuẩn DJ-Raven (`UiModalHelper.showQuickPopup`).
  * Khắc phục hoàn toàn xung đột focus/event giữa ModalDialog và GlassPanePopup; popup mở êm ái, giữ vững trên màn hình, hỗ trợ xem thẻ số, đổi mật khẩu băm BCrypt cập nhật trực tiếp vào MySQL và nút Đóng/Đăng xuất chuẩn xác.
- **Chuẩn hóa Phân quyền Vai trò (RBAC Normalization)**:
  * Bổ sung helper `isAdmin()`, `isLibrarian()`, `isReader()` tại `User.java`.
  * Sửa lỗi lệch chữ hoa/thường (`"Thủ Thư"` vs CSDL `"Thủ thư"` / `"LIBRARIAN"`), đảm bảo tài khoản Thủ thư (`kien.phan`) nhận diện chính xác 100% vai trò "Thủ Thư".
  * Thủ thư được mở toàn bộ bộ công cụ nghiệp vụ: Bàn lưu hành quầy (Desk), Xử lý trả & bồi hoàn, Lịch sử phiếu mượn, Hồ sơ độc giả & Thẻ, Quản lý thể loại, Thống kê thư viện, Thùng rác & Khôi phục, Hộp thư góp ý; không còn bị gán nhầm vào giao diện Độc Giả.
- **Sửa Lỗi Truy Vấn Thùng Rác (Unknown column 's.Anhbia')**:
  * Chuẩn hóa tên cột trong truy vấn thùng rác tại `BookRepositoryImpl.java` từ `s.Anhbia` sang đúng tên `s.Hinhanh` theo `DATABASE_SCHEMA.md` và CSDL MySQL thực tế. Màn hình Thùng rác & Khôi phục tải trơn tru không còn lỗi.
- **Sửa Bắt Sự Kiện Click Mở Popup Tài Khoản Trên Header**:
  * Đăng ký bộ lắng nghe sự kiện chuột `openProfileListener` trên cả 5 thành phần: `userPanel`, `avatar`, `greetingBox`, `lblGreeting`, và `lblName`.
  * Chuyển sang bắt sự kiện `mousePressed` kết hợp debounce chống click đúp, đảm bảo người dùng click vào bất cứ đâu (hình tròn avatar, lời chào hay tên người dùng) đều lập tức mở popup thông tin tài khoản.
- **Nạp Dữ Liệu 25 Độc Giả Mẫu Chuẩn Người Thật (Seed Readers)**:
  * Khởi tạo thành công 25 tài khoản độc giả thực tế (`ND005` đến `ND029`) với họ tên Việt chuẩn dấu, ngày sinh, địa chỉ tại Đà Nẵng, số điện thoại, email sinh viên `@sv.dut.udn.vn`, CCCD 12 số mã hóa AES-256, mật khẩu băm BCrypt (`123456`) và thẻ thư viện điện tử tương ứng (`LIB-2026-00005` đến `LIB-2026-00029`).
- **Khắc Phục Triệt Để Lỗi NPE GlassPanePopup & Modal Hiện Rồi Ẩn Liền**:
  * **NPE VolatileImage:** `raven.popup` mặc định bật `useSnapshot() == true` để chụp ảnh nền cửa sổ; trên môi trường Windows FlatLaf/High-DPI hàm `createVolatileImage()` trả về `null` gây `NullPointerException` tại `ComponentImageUtils.createGraphics()`. Đã ghi đè `useSnapshot() == false` an toàn trong `UiModalHelper.initMainFrame` và `showQuickPopup`.
  * **Modal hiện rồi ẩn ngay:** Thư viện `raven.modal` mặc định đặt `BackgroundClickType.CLOSE`. Khi người dùng click nút (Thêm/Sửa/Xóa sách), sự kiện mouse release từ chuột rơi trúng lớp phủ backdrop vừa hiện ra khiến modal tự đóng ngay lập tức. Đã cấu hình `Option.BackgroundClickType.BLOCK` làm mặc định toàn hệ thống qua `ModalDialog.setDefaultOption()` và áp dụng cho toàn bộ form CRUD. Modal giờ đây đứng vững 100%, chỉ đóng khi người dùng chủ động bấm Lưu, Hủy, Đóng hoặc Xác nhận.
- **Kiểm thử & Đóng gói**:
  * Nâng tổng số test tự động lên **45 bài test (100% PASS)**.
  * Đóng gói lại `dist/XimiLib.jar` (16.0 MB).
