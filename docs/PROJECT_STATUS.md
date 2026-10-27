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
2. **Kiểm thử tự động:** **54/54 test cases JUnit 5 đạt 100% PASS** (Unit tests + Integration tests MySQL thật).
3. **An toàn dữ liệu:** 100% `PreparedStatement`, mật khẩu băm BCrypt, tiền tệ dùng `BigDecimal`, khóa bi quan `SELECT ... FOR UPDATE`.
4. **Không chặn giao diện:** Mọi truy vấn DB và I/O đều chạy trên `SwingWorker` ngoài EDT; giao diện cập nhật an toàn trên EDT. Khởi động nguội < 1 giây.
5. **Đóng gói & Chạy thử:** Lệnh `mvn clean package` xuất ra file `dist/XimiLib.jar` độc lập (16.1 MB) chạy trực tiếp bằng `run.bat`.
6. **Tài liệu:** Đầy đủ bộ tài liệu SSOT trong `/docs` (`AGENTS.md`, `ARCHITECTURE.md`, `DATABASE_SCHEMA.md`, `BUSINESS_RULES.md`, `PROJECT_STATUS.md`, `VAN_DAP.md`).

---

## 3. Nhật ký Thay đổi Dự án (Changelog)

### [Sprint 16 — Hoàn thiện Bố cục Responsive Dashboard, Tối ưu Tốc độ Tải Bìa & Khắc phục Lỗi Giao diện] - 2026-10-27
#### Added
- `db/seed/seed_demo_loans.sql`: Script nạp dữ liệu mượn trả thật (phiếu quá hạn, phiếu đến hạn hôm nay, phiếu mượn nhiều cuốn, và 2 phiếu chờ nhận sách trực tuyến) giúp giao diện vận hành trực quan và sinh động.
- Nút "+ Thêm Sách Mới" nổi bật trên thanh tiêu đề `BookCatalogView`, mở modal tạo sách mới `BookFormModalPanel` (chỉ hiển thị cho Quản trị viên và Thủ thư).
- Cơ chế đệm ảnh bìa thu nhỏ trong bộ nhớ (`ConcurrentHashMap<String, BufferedImage> THUMBNAIL_CACHE`) tại `BookCoverTile` và `COVER_CACHE` tại `ReaderBorrowRegistrationView`, giảm 100% việc decode ảnh lặp lại từ đĩa, tốc độ hiển thị đạt 0ms và giải phóng bộ nhớ ảnh gốc ngay lập tức.
#### Fixed
- **Trạng thái Thẻ bạn đọc (`thethuvien.Trangthai`)**: Khắc phục lỗi hiển thị "Bị khóa" do ánh xạ chuỗi so với số nguyên CSDL (`1` = Hoạt động, `0` = Khóa). Bổ sung `StatusBadgeRenderer` với màu xanh (Hoạt động), vàng cam (Hết hạn), đỏ (Bị khóa).
- **Thẻ Quản trị hệ thống & Phím phân quyền**: Bỏ bẫy nested JScrollPane khiến tiêu đề bị cuộn mất khi bảng nhận tiêu điểm. Tách rõ `BorderLayout.NORTH` (Tiêu đề, Breadcrumb, 2 nút hành động, 4 thẻ KPI) và `BorderLayout.CENTER` (Bảng cuộn độc lập). Mở rộng độ rộng cột Thao tác lên 260px (min 250px) để 3 nút "Phân quyền", "Khóa/Mở", "Đặt lại MK" nằm trên một hàng không bị tràn.
- **Thùng rác & Khôi phục (`TrashAndRestoreView`)**: Tích hợp `AncestorListener` và đăng ký lắng nghe sự kiện `BookStockChangedEvent` (phát sinh khi xóa/khôi phục sách tại `BookCatalogServiceImpl`), giúp Thùng rác tự động cập nhật ngay lập tức khi chuyển tab hoặc sau khi thao tác xóa sách.
- **Bố cục Responsive thẻ Dashboard**: Cải tiến `StaffHomePanel` với tỷ lệ co giãn linh hoạt (`setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS)`), bọc dải sách mới nhập trong `JScrollPane` cuộn ngang mượt mà, cắt tỉa văn bản dài với tooltip và thêm lề cho nút "Xem tất cả >" ở `SectionHeader`.
- **Logo ứng dụng hiển thị sắc nét, đúng màu hồng pastel**: Khắc phục lỗi file logo cũ chỉ chứa mặt nạ màu trắng thuần (white-on-white tàng hình trên nền form trắng). Đã tổng hợp đồ họa màu gốc từ `assets/Logo.svg` kèm kênh alpha trong suốt (RGBA 32-bit), tạo đầy đủ bộ icon đa kích thước (`logo.png`, `logo.svg`, `logo_16..512.png`) và nạp trực tiếp qua `AppLogo.getIcon()`.
- **Tối giản BrandPanel màn Đăng nhập**: Lược bỏ cơ chế slideshow đa ảnh, timer chuyển cảnh và nút điều hướng; cố định hiển thị 1 hình ảnh chất lượng cao duy nhất `plushy-woman-work.jpg` kèm gradient phủ đạt chuẩn WCAG >= 4.5:1 và thông điệp thương hiệu tinh tế.
- **Tổng số bài test tự động**: 56/56 bài test JUnit 5 đạt **100% PASS**.

### [Sprint 15 — Tích hợp Bộ tài nguyên Hình ảnh Logo & Minh họa] - 2026-10-27
#### Added
- `com.ximilib.presentation.util.AppLogo`: Cung cấp logo vector sắc nét từ `images/logo/logo.svg` và bộ icon PNG đa kích thước (16, 24, 32, 48, 64, 128, 256) cho Titlebar và Taskbar OS. Tự động đệm (cache) bộ nhớ.
- `com.ximilib.domain.model.ImageSlideshow`: Domain model quản lý trạng thái trình chiếu ảnh màn Đăng nhập (Next, Prev, GoTo, Auto-timer 5s, Hover Pause) hoàn toàn độc lập với Swing.
- `com.ximilib.presentation.component.EmptyStatePanel`: Khối giao diện chuẩn hóa hiển thị trạng thái rỗng kèm minh họa vector FlatSVG sắc nét và nút kêu gọi hành động (CTA).
- `com.ximilib.presentation.util.Illustrations`: Bộ hằng số đường dẫn minh họa vector chuẩn (11 file SVG chuẩn hóa).
#### Changed
- `BrandPanel`: Tích hợp slideshow 2 ảnh 3D thư viện Pixar chuẩn (`girl-reading-book.jpg`, `plushy-woman-work.jpg`), chuyển cảnh cross-fade 60 FPS, overlay gradient WCAG >= 4.5:1, nút điều hướng `<` `>` và phím mũi tên.
- `SidebarPanel`, `LoginFormPanel`, `DigitalCardPopupPanel`: Thay thế toàn bộ mã vẽ logo thủ công bằng `AppLogo.getIcon()`.
- `MainDashboardFrame`, `LoginForm`: Gán bộ ảnh icon cửa sổ ứng dụng qua `AppLogo.applyWindowIcons()`.
- `BookCatalogView`, `TrashAndRestoreView`, `NotificationPopupPanel`, `ReaderHomePanel`, `StaffHomePanel`, `HelpGuideModalPanel`: Tích hợp hình ảnh minh họa vector và trạng thái rỗng `EmptyStatePanel`.
#### Fixed & Cleaned
- Khắc phục lỗi JSVG render mã `Logo.svg` của Canva bằng cách chuyển nền đen thành kênh alpha trong suốt (RGBA 32-bit), tương thích hoàn hảo 100% với `FlatSVGIcon`.
- Tối ưu hóa kích thước ảnh đăng nhập từ 4.1 MB xuống ~240 KB cho 2 ảnh, giúp khởi động nguội ứng dụng < 1s.
- Loại bỏ hoàn toàn `usecase.jpg` và các tệp mock cũ khỏi tài nguyên và file `dist/XimiLib.jar`.
- Nâng tổng số test tự động lên **56 bài test (100% PASS)**.

### [Sprint 14 — Tái thiết kế Trang chủ, Header và Sidebar chuẩn Dashboard Vận hành] - 2026-10-27
#### Added
- **Trang chủ Vận hành Thủ thư / Quản trị viên (`StaffHomePanel`)**:
  * Thay thế toàn bộ banner tĩnh và dữ liệu mẫu bằng dashboard vận hành trực tiếp từ MySQL.
  * 4 thẻ KPI chỉ số thực (`sach`, `cuonsach`, `nguoidung`, `phieumuon`), loại bỏ nhãn xu hướng ảo (+12%, +8%).
  * Bảng "Cần xử lý hôm nay" phân loại hạn trả thuần domain (`LoanDueClassification`): Quá hạn (đỏ), Hạn hôm nay (vàng), Còn hạn (xanh), kèm nút "Gia hạn" và "Nhận trả sách" tại quầy.
  * Danh sách "Chờ nhận tại quầy" và lưới 6 sách mới nhập bìa 2:3 (`BookCoverTile`).
  * Chip giờ mở cửa tự động kiểm tra đóng cửa lúc 17:00 theo quy định SRS.
- **Trang chủ Độc giả phong cách Libby / Goodreads (`ReaderHomePanel`)**:
  * Lời chào cá nhân hóa, mã thẻ và trạng thái thẻ thư viện.
  * Banner cảnh báo nợ phạt / quá hạn nổi bật (chỉ hiển thị khi có vi phạm/nợ đọng) kèm nút điều hướng nộp phạt trực tuyến.
  * Danh sách sách đang mượn với bìa 2:3, hạn trả và nút gia hạn trực tuyến (+14 ngày) theo quy định BR-03.
  * Lưới sách mới nhập có thể bấm xem chi tiết hoặc đăng ký mượn ngay.
- **Header & Sidebar Tối giản Hiện đại**:
  * Sidebar thu gọn tối đa 6-7 mục tùy vai trò, loại bỏ hoàn toàn thanh cuộn ở độ phân giải 768px và các nhóm tiêu đề thừa.
  * Phân nhóm tiểu màn hình bằng thanh chuyển tab phẳng `TabBar` (`TabbedViewPanel`) cho Kho sách, Mượn – Trả, Quản trị, Sách của tôi.
  * Chuông thông báo động (`NotificationPopupPanel`) phản ánh số lượng thông báo chưa đọc từ MySQL.
  * Header hành động theo vai trò: "+ Lập phiếu mượn" cho Thủ thư/Admin, "Thẻ Thư Viện" cho Độc giả.
  * Tiêu đề cửa sổ chuẩn hóa: `XimiLib – Quản lý Thư viện`.
#### Removed
- Xóa bỏ các lớp mockup cũ: `HeroBannerPanel`, `ActiveLoanCard`, `BookArrivalCard`.

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


### [Sprint 14 — Tinh chỉnh Layout, Hiệu ứng Chuyển cảnh 60 FPS, Khóa Tương tác & Nâng cấp Bàn Desk] - 2026-10-27
#### Added & Refined
- **Tinh chỉnh Bố cục Form Đăng ký (`RegisterForm`)**:
  * Chuyển layout từ đơn cột dọc sang `GridBagLayout` dạng bảng 2 cột cân đối, kích thước chuẩn 650x700px. Nhãn (Label) và ô nhập nằm trên cùng 1 hàng.
  * Bổ sung placeholder gợi ý định dạng (CCCD 9-12 số, email, số điện thoại, mật khẩu).
  * Nút "TẠO TÀI KHOẢN" bo góc ôm trọn chiều ngang form, căn giữa chỉn chu.
- **Nâng cấp Animation 60 FPS Panel Nhận Diện (`BrandPanel`)**:
  * Tích hợp cơ chế chuyển cảnh Carousel mượt mà với đường cong nội suy Cubic Ease-Out (`TIMER_FPS_DELAY = 16ms ~ 60 FPS`).
  * Hiệu ứng chuyển động kép: văn bản Slide & Fade kết hợp các chấm chỉ báo Morphing Dots co giãn linh hoạt khi chuyển slide hoặc click chuyển trang.
  * Tự động tạm dừng chuyển động khi người dùng rê chuột vào panel để đọc nội dung.
- **Khóa Tương tác Chống Spam Khi Đăng Nhập (`LoginFormPanel`)**:
  * Bổ sung hàm `setFormInteractive(false)` tự động vô hiệu hóa toàn bộ ô nhập văn bản, nút bấm và liên kết khi hệ thống đang xử lý xác thực tài khoản.
  * Ẩn con trỏ soạn thảo nhấp nháy (`requestFocusInWindow()`) để tạo trải nghiệm người dùng chuyên nghiệp và mượt mà.
- **Thiết kế Nút Đóng Vector 'X' Chống Lỗi Font (`UserProfileModalPanel` & `NotificationPopupPanel`)**:
  * Loại bỏ hoàn toàn ký tự Unicode `✕` (vốn dễ bị lỗi ô vuông font trên một số phiên bản Windows), thay thế bằng nút đóng vector thuần vẽ bằng `Graphics2D` với viền tròn mờ hover tinh tế.
  * Form đổi mật khẩu trong `UserProfileModalPanel` được căn chỉnh theo `GridBagLayout` nhãn và ô nhập cùng hàng, tiêu đề căn giữa.
  * Triệt tiêu hoàn toàn thanh cuộn ngang không mong muốn trong `NotificationPopupPanel` bằng `ScrollableListPanel` ép cố định theo chiều ngang viewport.
- **Bàn Lưu Hành Quầy Đầy Đủ Tính Năng (`CirculationDeskView`)**:
  * Bố cục 2 cột chuyên nghiệp: bên trái là thẻ thông tin bạn đọc trực quan (hiển thị trạng thái thẻ HOẠT ĐỘNG / KHÓA / HẾT HẠN) kèm ô quét mã vạch và tóm tắt hạn trả; bên phải là danh sách giỏ sách mượn.
  * Bổ sung nút xóa cuốn sách đang chọn, nút xóa toàn bộ giỏ mượn, cùng nhãn đếm số lượng "X / 3 cuốn" tuân thủ nghiêm ngặt quy tắc BR-02.
  * Tích hợp kiểm tra thẻ bị khóa hoặc hết hạn trước khi xuất mượn theo BR-07.
- **Chuẩn hóa Cột Trạng Thái Sách Thùng Rác (`BookRepositoryImpl`)**:
  * Đồng bộ câu truy vấn thùng rác với schema CSDL thực tế: đổi `c.Trangthai` sang `c.Trangthaisach` trên bảng `cuonsach`.
- **Kiểm thử**:
  * Toàn bộ 45 bài test tự động chạy qua 100% (`BUILD SUCCESS`).
