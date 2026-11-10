# Version: 1.5 | Last updated: 2026-11-04 | Related files: ARCHITECTURE.md, DATABASE_SCHEMA.md, BUSINESS_RULES.md, DEMO_AND_DEFENSE_GUIDE.md, TRACEABILITY.md

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
1. **Kiến trúc:** Tuân thủ Clean Architecture 4 tầng, Pure DI tại `AppContainer`, không có bất kỳ import ngược nào giữa các tầng.
2. **Kiểm thử tự động:** **45 test cases JUnit 5** tinh gọn tập trung vào toàn bộ các công thức nghiệp vụ cốt lõi (Kho sách, Mượn trả, Tiền phạt, Xác thực bảo mật).
3. **An toàn dữ liệu:** 100% `PreparedStatement`, mật khẩu băm BCrypt, tiền tệ dùng `BigDecimal`, khóa bi quan `SELECT ... FOR UPDATE`, quản lý transaction tập trung ở tầng Application.
4. **Không chặn giao diện:** Mọi truy vấn DB và I/O đều chạy trên `SwingWorker` ngoài EDT; giao diện cập nhật an toàn trên EDT. Khởi động nguội < 1.6 giây.
5. **Đóng gói & Chạy thử:** Lệnh `mvn clean package` xuất ra file `dist/XimiLib.jar` độc lập chạy trực tiếp bằng `run.bat` / `run.sh` và hỗ trợ cờ headless `--init-db`.
6. **Tài liệu:** Đầy đủ bộ tài liệu SSOT trong `/docs` (`ARCHITECTURE.md`, `DATABASE_SCHEMA.md`, `BUSINESS_RULES.md`, `PROJECT_STATUS.md`, `DEMO_AND_DEFENSE_GUIDE.md`, `TRACEABILITY.md`, `CODING_STYLE.md`).

---

## 3. Các Mốc Hoàn Thành Dự Án (Project Milestones)

Dự án được xây dựng và hoàn thiện qua 4 cột mốc chính bám sát tiến độ học tập:

### Cột mốc 1: Khảo sát & Thiết kế Cơ sở Dữ liệu (Hoàn thành)
- Phân tích yêu cầu bài toán quản lý thư viện, chuẩn hóa 23 bảng CSDL MySQL đạt chuẩn BCNF / 3NF.
- Xây dựng các tệp kịch bản nạp schema và seed dữ liệu mẫu phong phú (221 đầu sách, 663 bản sao vật lý, 62 tác giả, 11 thể loại).

### Cột mốc 2: Hiện thực Kiến trúc Clean Architecture & Nghiệp vụ Lõi (Hoàn thành)
- Thiết lập phân tầng `Domain -> Application -> Infrastructure -> Presentation` và Pure Dependency Injection tại `AppContainer`.
- Triển khai toàn bộ quy tắc nghiệp vụ cốt lõi: Mượn tối đa 3 cuốn/14 ngày, phạt trễ hạn 2.000đ/ngày, quy trình bồi thường mất sách 200%, mã hóa mật khẩu BCrypt và cơ chế khóa tài khoản khi nhập sai 5 lần.
- Áp dụng khóa bi quan `SELECT ... FOR UPDATE` trong Transaction để chống tranh chấp mượn trùng sách đồng thời.

### Cột mốc 3: Thiết kế Giao diện FlatLaf & Tích hợp Tính năng Mở rộng (Hoàn thành)
- Hoàn thiện giao diện đồ họa phong cách FlatLaf Pastel hiện đại, kết hợp bộ thư viện DJ-Raven Suite (`modal-dialog`, `swing-datetime-picker`, `swing-glasspane-popup`).
- Tích hợp cổng thanh toán trực tuyến VietQR Napas247 tự động sinh mã QR đối soát qua SePay.
- Hiện thực báo cáo thống kê chuyên sâu và kết xuất file Excel định dạng `.xlsx` qua Apache POI.

### Cột mốc 4: Tinh gọn Hệ thống, Tối ưu Trải nghiệm & Hoàn thiện Đồ án (Hoàn thành)
- Tối ưu luồng giao sách đặt trước online: tự động nạp thẻ bạn đọc và sách đặt trước tại Bàn lưu hành.
- Tinh giản bộ kiểm thử tự động về 4 lớp Unit Test nghiệp vụ cốt lõi (45 tests in-memory), loại bỏ toàn bộ các bài test phức tạp không phù hợp với phạm vi đồ án sinh viên.
- Đóng gói file thực thi độc lập `dist/XimiLib.jar` và hoàn thiện cẩm nang hướng dẫn demo, vấn đáp bảo vệ đồ án.
