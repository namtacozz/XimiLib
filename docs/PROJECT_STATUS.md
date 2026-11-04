# Version: 1.4 | Last updated: 2026-11-04 | Owner: Architecture Team | Related files: ../AGENTS.md, ARCHITECTURE.md, DATABASE_SCHEMA.md, BUSINESS_RULES.md, VAN_DAP.md, TRACEABILITY.md

# PROJECT_STATUS.md — Trạng thái Dự án & Lộ trình Phát triển XimiLib

## 1. Ma trận Tính năng Dự án (Feature Matrix F-01 đến F-12 & SRS YC-01..YC-14)

Dự án phân cấp tính năng theo chuẩn độ ưu tiên MoSCoW (Must / Should / Nice) bám sát 100% file Đặc tả Yêu cầu:

| Mã | Tên Tính năng / Module | Ưu tiên | Trạng thái | Phụ trách chính | Mô tả tóm tắt kỹ thuật & Phân quyền RBAC |
|---|---|---|---|---|---|
| **F-01** | **Quản lý Đầu sách & Bản sao (YC-03, YC-04)** | **Must** | **Hoàn thành (100%)** | Phan Vũ Xuân Kiên (FE/DB) | Giao diện Kho sách (`BookCatalogView`), Quản lý Thể loại (`CategoryManagementView`), Thùng rác & Khôi phục (`TrashAndRestoreView`). 221 đầu sách, 663 cuốn copy (`cuonsach`), phân trang server-side, bộ lọc Thể loại/Tình trạng, soft-delete BR-09 và cơ chế Observer. Phân quyền: Admin + Thủ thư được CRUD; Độc giả chỉ tra cứu & bấm mượn. |
| **F-02** | **Quản lý Mượn - Trả sách (YC-05, YC-06, YC-09)** | **Must** | **Hoàn thành (100%)** | Lê Võ Thành Nam (BE) | Bàn lưu hành quầy (`CirculationDeskView`), Xử lý trả & bồi hoàn (`ReturnAndCompensationView`), Đăng ký mượn sách App (`ReaderBorrowRegistrationView`), Phiếu mượn của tôi (`ReaderMyLoansView`). Khóa bi quan `SELECT ... FOR UPDATE` (R-05), hạn mức 3 cuốn/14 ngày (BR-02), mượn tại chỗ trả 17:00 (BR-04), phạt trễ 2.000đ/ngày (BR-06), chặn nợ phạt (BR-07), bồi thường 200% (BR-08), FSM báo mất 2 bước (R-10). |
| **F-03** | **Tài khoản & Phân quyền RBAC (YC-01, YC-02, YC-10)** | **Must** | **Hoàn thành (100%)** | Nguyễn Việt Nhật (Lead) | Đăng nhập/Đăng ký phân đôi 50/50 chuẩn FlatLaf, băm mật khẩu BCrypt (R-08), khóa 15 phút sau 5 lần sai (BR-01). Màn hình `UserRoleManagementView` dành riêng cho Admin quản lý tài khoản nhân viên (Admin, Thủ thư), phân quyền vai trò RBAC và khóa/mở tài khoản. Hỗ trợ đăng nhập bằng Tendangnhap/Email/Sdt (DEV-02). |
| **F-04** | **Quản lý Thẻ Thư viện (YC-02, F-04)** | **Must** | **Hoàn thành (100%)** | Phan Vũ Xuân Kiên (FE/DB) | Cấp thẻ tự động khi đăng ký (`LIB-YYYY-XXXXX`, hạn 365 ngày), màn hình Hồ sơ độc giả & Quản lý thẻ (`PatronManagementView`), khóa/mở thẻ, gia hạn thẻ, Digital Card Popup QR. |
| **F-05** | **Thanh toán Trực tuyến VietQR (YC-06, YC-09)** | **Should** | **Hoàn thành (100%)** | Lê Võ Thành Nam (BE) | Sinh mã VietQR động theo chuẩn Napas247 (`VietQrModalPanel`), hiển thị thông tin MB Bank và mã nội dung `XML{Maphieuphat}`, đối soát chuyển khoản với regex ranh giới, lưu lịch sử vào bảng `thanhtoan`. |
| **F-06** | **Ghi Log Kiểm toán Hệ thống (F-06, YC-07)** | **Should** | **Hoàn thành (100%)** | Lê Võ Thành Nam (BE) | Triển khai `AuditLogger` an toàn luồng, ghi nhật ký truy cập, biến động mượn/trả, nộp phạt, báo mất và kết xuất báo cáo vào cả file `logs/audit.log` và CSDL bảng `auditlog` (DEV-04, R-08). |
| **F-07** | **Gợi ý Sách theo Sở thích** | **Nice** | **Hoàn thành (80%)** | Phan Vũ Xuân Kiên (FE/DB) | Khối sách mới nhập và sách nổi bật trên Dashboard hiển thị theo danh mục; gợi ý sách cùng thể loại khi không tìm thấy kết quả tra cứu. |
| **F-08** | **Chatbot Hỗ trợ Độc giả** | **Nice** | **Tạm hoãn (Scope)** | Nguyễn Việt Nhật (Lead) | Không triển khai chatbot riêng; hiện thực nơi trao đổi qua chức năng Bình luận & Thảo luận sách cộng đồng (UC 3.2.8, bảng `binhluan`) theo DEV-09. |
| **F-09** | **Bảng Xếp hạng Sách Mượn nhiều** | **Nice** | **Hoàn thành (100%)** | Phan Vũ Xuân Kiên (FE/DB) | Báo cáo Top 10 đầu sách mượn nhiều nhất tích hợp trong màn hình Thống kê. |
| **F-10** | **Góp ý & Đánh giá Thư viện** | **Nice** | **Hoàn thành (90%)** | Phan Vũ Xuân Kiên (FE/DB) | Tích hợp bảng `gopy` và form phản hồi trực tiếp. |
| **F-11** | **Đánh giá & Bình luận Sách (YC-08)** | **Nice** | **Hoàn thành (100%)** | Lê Võ Thành Nam (BE) | Màn hình `BookDetailModalPanel`: hiển thị vị trí kệ sách (`khuvuc`, `kesach`), điểm sao trung bình (1-5 ⭐) và nhận xét của bạn đọc từ bảng `binhluan`, cho phép gửi đánh giá mới và nút mượn sách nhanh. |
| **F-12** | **Thống kê & Báo cáo Định kỳ (YC-07)** | **Should** | **Hoàn thành (100%)** | Phan Vũ Xuân Kiên (FE/DB) | Màn hình `ReportsDashboardView`: 4 KPI tổng quan, 4 loại báo cáo nghiệp vụ (lượt mượn trả, độc giả quá hạn, sách hư hỏng, top sách), xuất file Excel định dạng `.xlsx` qua Apache POI (`poi-ooxml:5.2.5`) và in ấn báo cáo / PDF qua Java Print API (DEV-05) kèm Audit log. |

---

## 2. Tiêu chuẩn Hoàn thành (Definition of Done - DoD)
Đã đạt **100% 6 tiêu chí DoD**:
1. **Kiến trúc:** Tuân thủ Clean Architecture 4 tầng, Pure DI tại `AppContainer`, không có bất kỳ import ngược nào giữa các tầng (`DependencyRuleTest` xanh 100%).
2. **Kiểm thử tự động:** **106 test cases JUnit 5** bao gồm Unit tests các tầng và Integration tests kết nối, giao dịch, leak detection.
3. **An toàn dữ liệu:** 100% `PreparedStatement`, mật khẩu băm BCrypt, tiền tệ dùng `BigDecimal`, khóa bi quan `SELECT ... FOR UPDATE`, quản lý transaction tập trung ở tầng Application.
4. **Không chặn giao diện:** Mọi truy vấn DB và I/O đều chạy trên `SwingWorker` ngoài EDT; giao diện cập nhật an toàn trên EDT. Khởi động nguội < 1 giây.
5. **Đóng gói & Chạy thử:** Lệnh `mvn clean package` xuất ra file `dist/XimiLib.jar` độc lập chạy trực tiếp bằng `run.bat` / `run.sh` và hỗ trợ cờ headless `--init-db`.
6. **Tài liệu:** Đầy đủ bộ tài liệu SSOT trong `/docs` (`AGENTS.md`, `ARCHITECTURE.md`, `DATABASE_SCHEMA.md`, `BUSINESS_RULES.md`, `PROJECT_STATUS.md`, `VAN_DAP.md`, `TRACEABILITY.md`).

---

## 3. Nhật ký Thay đổi Dự án (Changelog)

### [Sprint 19 — PR 1: Khắc Phục Lỗi Hiển Thị & Hành Vi Sai Giao Diện Đăng Nhập, Đăng Ký, Khách] - 2026-11-04
#### Fixed & Enhanced (UI/UX)
- **`DividerLabel` thuần Graphics2D**: Thay thế chuỗi có ký tự gạch Unicode (`─── hoặc ───`) gây lỗi ô vuông (tofu glyph) trên Windows bằng component `DividerLabel` tự vẽ hai vạch kẻ `UITheme.CARD_BORDER` và chữ "hoặc" `UITheme.TEXT_MUTED`.
- **`VectorCheckBoxIcon` & Focus Ring**: Vẽ rõ 2 trạng thái cho checkbox "Ghi nhớ đăng nhập" (viền bo góc khi chưa chọn, nền primary bo góc kèm dấu tick vector trắng khi đã chọn), hỗ trợ focus ring khi duyệt phím Tab và rollover effect.
- **Tự động ẩn banner lỗi**: Lắng nghe `DocumentListener` trên cả ô tài khoản và mật khẩu, tự động ẩn banner lỗi đỏ ngay khi người dùng bắt đầu gõ lại hoặc khi bấm đăng nhập.
- **Khớp DEV-02 & Đổi nhãn thương hiệu**: Cập nhật nhãn và placeholder ô đăng nhập thành `"Tên đăng nhập, email hoặc số điện thoại"`; đổi huy hiệu `"Library OS"` thành `"Quản lý Thư viện"`.
- **Avatar Khách dạng vector**: Thay chữ `"T"` (sinh từ họ tên mặc định "Khách Viếng Thăm") bằng icon người vẽ vector `IconFactory.createUserIcon()` cho người dùng vai trò Khách.
- **Ẩn cột Checkbox theo quyền**: Ẩn cột chọn checkbox (cột 0) trên bảng kho sách `BookCatalogView` đối với người dùng vai trò Khách và Độc giả; chỉ hiển thị cho Admin và Thủ thư.
- **Đăng ký (`RegisterForm`)**: Sửa placeholder CCCD thành `"9 hoặc 12 chữ số"`, bổ sung kiểm tra định dạng đúng 9 hoặc 12 số nguyên, áp dụng icon cửa sổ đồng bộ logo thương hiệu (`AppLogo.applyWindowIcons()`).
- **Kiểm thử giao diện**: Bổ sung `DividerLabelAndCheckBoxIconTest` (2 tests mới), toàn bộ bộ test đạt **108 test cases xanh 100%**.

### [Sprint 18 — Đồng Bộ Toàn Diện Tài Liệu, Schema V5, Nguồn Mã & Quy Tắc Nghiệp Vụ SRS] - 2026-11-04
#### Added & Updated
- **Thực thi toàn bộ Use Cases SRS (UC 3.2.1 đến UC 3.2.9) và DEV-01..DEV-10**:
  * **UC 3.2.1 (Đăng nhập)**: Hỗ trợ đăng nhập linh hoạt bằng Tên đăng nhập, Email hoặc Số điện thoại (DEV-02); thông báo tài khoản bị khóa đúng nguyên văn `"Tài khoản của bạn đã bị khóa. Vui lòng liên hệ thủ thư"`; chuẩn hóa thông báo sai mật khẩu/tài khoản chống liệt kê.
  * **UC 3.2.2 (Đăng ký bạn đọc)**: Chuẩn hóa 7 trường thông tin (Họ tên, Ngày sinh qua DatePicker, CCCD, Địa chỉ, Email, SĐT, Mật khẩu); ràng buộc mật khẩu $\ge 8$ ký tự chữ hoa/số/ký tự đặc biệt (BR-10); kiểm tra CCCD 9 hoặc 12 chữ số (BR-11); thông báo trùng lặp chuẩn `"Số CMND/CCCD hoặc Email đã được đăng ký trên hệ thống"`.
  * **UC 3.2.3 (Quản lý sách)**: Sinh mã sách tự động `MSxxxx` (BR-13); số lượng sách nhập kho $\ge 1$ (BR-12); chặn xóa sách đang có người mượn với thông báo chuẩn `"Không thể xóa đầu sách đang có độc giả mượn"` (BR-09); soft-delete `IsDeleted = TRUE`.
  * **UC 3.2.4 (Tra cứu sách)**: Tận dụng index tìm kiếm đa trường `idx_sach_search`; trạng thái rỗng hiển thị đúng thông điệp chuẩn `"Không tìm thấy cuốn sách nào khớp với từ khóa của bạn"` và hướng dẫn độc giả.
  * **UC 3.2.5 (Đặt mượn trực tuyến)**: Phân định mượn về nhà (14 ngày) và tại chỗ (17:00 trong ngày); chặn quá hạn `"Bạn có sách quá hạn chưa hoàn trả. Vui lòng trả sách trước khi mượn tiếp"`; chặn quá hạn mức 3 cuốn `"Bạn đã đạt giới hạn mượn tối đa 3 cuốn sách mang về"`; chặn thẻ hết hạn `"Thẻ thư viện của bạn đã hết hạn sử dụng. Vui lòng liên hệ thủ thư để gia hạn"`; thông báo thành công nhận sách 24h.
  * **UC 3.2.6 (Bàn lưu hành quầy)**: Quét thẻ độc giả/CCCD $\rightarrow$ quét mã vạch $\rightarrow$ lập phiếu `DANG_MUON`; trả sách kiểm tra quá hạn và hư hại.
  * **UC 3.2.7 (Báo cáo & Kiểm toán)**: Ghi log kiểm toán song song file `logs/audit.log` và CSDL bảng `auditlog` (DEV-04); hỗ trợ In báo cáo trực tiếp / Print to PDF qua Java Print API (DEV-05); thông báo rỗng chuẩn `"Không có phát sinh giao dịch trong mốc thời gian đã chọn"`.
  * **UC 3.2.8 (Cộng đồng & Nhận xét)**: Khi khách vãng lai gửi bình luận, hiển thị in-app modal prompt `"Vui lòng đăng nhập để gửi bình luận"` kèm nút điều hướng đến trang Đăng nhập.
  * **UC 3.2.9 (Báo mất & Bồi hoàn)**: Chuẩn hóa FSM 2 bước (`CHO_XU_LY_BOI_THUONG` $\rightarrow$ thanh toán $\rightarrow$ `DA_MAT`); công thức bồi thường $200\% \times \text{giá bìa} + 20.000\text{đ} + \text{phạt trễ}$; ghi nhận `thanhtoan.magiaodich` duy nhất.
  * **YC-10 & YC-13**: Cung cấp `SystemSettingsPort.getInt()` / `getBigDecimal()` đọc cấu hình động từ `cauhinh` (DEV-06); vòng lặp tự động kết nối lại (auto-reconnect 3 lần backoff) trong `DatabaseConnectionManager`; tác vụ bảo trì nền `BackgroundJobManager` định kỳ quét quá hạn và duy trì thư mục sao lưu CSDL (DEV-07).
  * **Bộ kiểm thử toàn diện**: Nâng tổng số test cases lên **106 tests** (100% PASS, 0 failure, 0 error).

- **Đồng bộ hóa Tài liệu Đặc tả (SSOT SRS)**:
  * Lập [docs/TRACEABILITY.md](file:///f:/AppMaking/XimiLib/docs/TRACEABILITY.md) làm ma trận truy vết chuẩn xác cho toàn bộ 14 yêu cầu YC-01..YC-14, Use Case 3.2.1..3.2.9 và Sổ đăng ký sai lệch DEV-01..DEV-10.
  * Cập nhật `DATABASE_SCHEMA.md` lên 24 bảng (thêm `binhluan`, `gopy`, `cauhinh`, `auditlog`), chuẩn hóa 7 trạng thái FSM của phiếu mượn và hình thức `TAI_CHO` / `VE_NHA`.
  * Chuẩn hóa `BUSINESS_RULES.md`: FSM 2 bước cho Báo mất sách (`CHO_XU_LY_BOI_THUONG` -> `DA_MAT`), bổ sung BR-10..BR-14 (mật khẩu 8 ký tự, CCCD 9/12 số, SL nhập >= 1, quét quá hạn 00:00).
  * Chuẩn hóa `ARCHITECTURE.md`: sửa mẫu `BookRepositoryImpl` khớp schema thật, viết lại §7.3 theo `TransactionCoordinator`/`TransactionManager`, bổ sung cây package các Query Ports mới và cơ chế so khớp regex SePay.

### [Sprint 17 — Hoàn thiện Kiến trúc Kết nối JDBC thuần (DriverManager), Transaction Coordinator, Quản lý Kệ/Khu vực, Biểu đồ Xu hướng & Xuất Báo cáo Excel POI] - 2026-11-01
#### Added
- **Task T02 (Kết nối JDBC thuần DriverManager & Quản lý Cấu hình DB)**:
  * Khởi tạo `DatabaseConnectionManager` với `java.sql.DriverManager` trực tiếp (không pool, không shutdown hook, không double-checked locking). URL nạp UTF-8 và múi giờ Asia/Ho_Chi_Minh.
  * `DatabaseConfig`: Ưu tiên nạp biến môi trường `XIMILIB_DB_*`, sau đó đọc từ `config/db.properties`. Cung cấp file mẫu `config/db.properties.example` và thông báo lỗi tiếng Việt tường minh khi thiếu thông tin kết nối. Loại bỏ 100% mật khẩu cứng mặc định.
  * Cấu hình khóa AES (`SecurityUtil.DEFAULT_AES_KEY`) qua biến `XIMILIB_AES_KEY` hoặc `security.aes.key`, không fallback chuỗi cứng.
  * `TransactionManager`: Đóng gói cơ chế thực thi giao dịch an toàn với `TransactionCallback` và `VoidTransactionCallback`.
  * `DatabaseInitializer`: Tự động nạp schema và thực thi chuỗi migration V1 -> V2 -> V3 -> V4 khi khởi tạo; hỗ trợ cờ dòng lệnh headless `--init-db` trong `Main.java` tiện lợi cho môi trường CI/CD và máy dev mới.
  * `DBConnectionLeakIT`: Kiểm thử tích hợp xác nhận 23 bảng, 221 cuốn sách và đảm bảo giải phóng connection 100% sau N=100 lần truy vấn (kiểm tra `Threads_connected`).
- **Task T03 (Quản lý Khu vực & Kệ sách - Storage Subsystem)**:
  * Domain: Mô hình `Zone`, `Shelf`, ngoại lệ miền `BookException`, `CopyException`, repository interfaces `ZoneRepository`, `ShelfRepository`.
  * Application: Cung cấp `ZoneDto`, `ShelfDto`, `StorageQueryPort`, `StorageService`, `StorageServiceImpl` với các kiểm tra nghiệp vụ chặt chẽ (chặn xóa khu vực nếu còn kệ, chặn xóa kệ nếu còn cuốn sách).
  * Infrastructure: Hiện thực `ZoneRepositoryImpl`, `ShelfRepositoryImpl` qua JDBC PreparedStatement và đăng ký DI tại `AppContainer`.
  * Tests: Bộ unit tests `StorageServiceTest` (8 tests) và IT `StorageRepositoryIT`.
- **Task T01 (Phân định Ranh giới Transaction Boundary)**:
  * Định nghĩa `TransactionContext` (Domain marker interface) và `TransactionCoordinator` (Application port), triển khai `JdbcTransactionCoordinator` tại Infrastructure persistence connection.
  * Loại bỏ hoàn toàn `setAutoCommit`, `commit`, `rollback` khỏi các Repository (`CirculationRepositoryImpl`, `BookRepositoryImpl`, `UserRepositoryImpl`). Service chịu trách nhiệm điều phối transaction theo đúng Clean Architecture.
  * Bổ sung kiểm thử ca biên phạt `FineCalculationServiceTest` (ngày trễ = 0, trễ qua nửa đêm 00:00:01, giá lẻ làm tròn `HALF_UP`) và IT tranh chấp mượn đồng thời `CirculationConcurrencyIT`.
- **Task T03 (Dashboard Charts & Xuất Báo cáo Excel POI)**:
  * **Migration V4 (`db/migrations/V4__report_and_search_indexes.sql`)**: Thiết lập 4 chỉ mục hot queries: `idx_cuonsach_status_book`, `idx_phieumuon_overdue`, `idx_phieumuon_date`, `idx_thanhtoan_date`.
  * **Application Port `ReportExporter` & DTO `DailyMetric`**: Hỗ trợ trích xuất số liệu xu hướng mượn và doanh thu phạt theo thời gian (`getBorrowAndFineTrend`).
  * **`ExcelReportExporter`**: Sử dụng thư viện `org.apache.poi:poi-ooxml:5.2.5` xuất báo cáo ra file Excel `.xlsx` chuẩn giao diện chuyên nghiệp (header nền màu thương hiệu, định dạng số tiền VND và ngày tháng, tự động căn chỉnh độ rộng cột).
  * **`TrendChartPanel`**: Vẽ biểu đồ kết hợp trực tiếp bằng Java2D phẳng bo góc (cột thể hiện lượt mượn sách, đường cam hổ phách thể hiện xu hướng tiền phạt) tích hợp trên `ReportsDashboardView` với nút "Xuất Excel (.xlsx)" chạy ngoài EDT bằng `SwingWorker`.
  * Tests: `ExcelReportExporterTest` (2 tests POI kiểm tra định dạng và dữ liệu đọc lại), `ReportTrendAggregationTest` (1 test).

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

### [Sprint 14 — Tái thiết kế Trang chủ, Header và Sidebar chuẩn Dashboard Vận hành] - 2026-10-27
#### Added
- **Trang chủ Vận hành Thủ thư / Quản trị viên (`StaffHomePanel`)**:
  * Thay thế toàn bộ banner tĩnh và dữ liệu mẫu bằng dashboard vận hành trực tiếp từ MySQL.
  * 4 thẻ KPI chỉ số thực (`sach`, `cuonsach`, `nguoidung`, `phieumuon`), loại bỏ nhãn xu hướng ảo.
  * Bảng "Cần xử lý hôm nay" phân loại hạn trả thuần domain (`LoanDueClassification`): Quá hạn (đỏ), Hạn hôm nay (vàng), Còn hạn (xanh), kèm nút "Gia hạn" và "Nhận trả sách" tại quầy.
  * Danh sách "Chờ nhận tại quầy" và lưới 6 sách mới nhập bìa 2:3 (`BookCoverTile`).
  * Chip giờ mở cửa tự động kiểm tra đóng cửa lúc 17:00 theo quy định SRS.
- **Trang chủ Độc giả phong cách Libby / Goodreads (`ReaderHomePanel`)**:
  * Lời chào cá nhân hóa, mã thẻ và trạng thái thẻ thư viện.
  * Banner cảnh báo nợ phạt / quá hạn nổi bật kèm nút điều hướng nộp phạt trực tuyến.
  * Danh sách sách đang mượn với bìa 2:3, hạn trả và nút gia hạn trực tuyến (+7 ngày) theo quy định BR-03.
  * Lưới sách mới nhập có thể bấm xem chi tiết hoặc đăng ký mượn ngay.
- **Header & Sidebar Tối giản Hiện đại**:
  * Sidebar thu gọn tối đa 6-7 mục tùy vai trò, loại bỏ hoàn toàn thanh cuộn ở độ phân giải 768px.
  * Phân nhóm tiểu màn hình bằng thanh chuyển tab phẳng `TabBar` (`TabbedViewPanel`).
  * Chuông thông báo động (`NotificationPopupPanel`) phản ánh số lượng thông báo chưa đọc từ MySQL.
  * Header hành động theo vai trò: "+ Lập phiếu mượn" cho Thủ thư/Admin, "Thẻ Thư Viện" cho Độc giả.

### [Sprint 13 — Hoàn thiện Trọn bộ Tính năng Đặc tả & Phân quyền RBAC] - 2026-10-27
#### Added
- **YC-10 (Quản lý Tài khoản & Phân quyền RBAC)**: Màn hình `UserRoleManagementView` dành riêng cho Admin.
- **YC-05 (Độc giả Đặt mượn sách qua App & Đặt trước khi hết sách)**: `ReaderBorrowRegistrationView`, tích hợp đặt chỗ `datcho`.
- **YC-09 (Báo mất sách & Đền bù Online)**: Tích hợp nút Báo mất, tính đền bù 200% theo BR-08, popup VietQR `VietQrModalPanel`.

### [Sprint 12 — Tinh chỉnh Layout, Hiệu ứng Chuyển cảnh 60 FPS, Khóa Tương tác & Nâng cấp Bàn Desk] - 2026-10-26
#### Added & Refined
- **Tinh chỉnh Bố cục Form Đăng ký (`RegisterForm`)**: GridBagLayout 2 cột, placeholder gợi ý định dạng.
- **Nâng cấp Animation 60 FPS Panel Nhận Diện (`BrandPanel`)**: Carousel mượt mà, đường cong nội suy Cubic Ease-Out.
- **Khóa Tương tác Chống Spam Khi Đăng Nhập (`LoginFormPanel`)**: `setFormInteractive(false)`.
- **Thiết kế Nút Đóng Vector 'X' Chống Lỗi Font (`UserProfileModalPanel` & `NotificationPopupPanel`)**.
- **Bàn Lưu Hành Quầy Đầy Đủ Tính Năng (`CirculationDeskView`)**: Quét mã vạch, đếm "X / 3 cuốn", kiểm tra thẻ.
