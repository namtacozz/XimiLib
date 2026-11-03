# BÁO CÁO CHI TIẾT CÁC ĐÓNG GÓP CỦA THÀNH VIÊN NHẬT TRÊN NHÁNH `dev`
**Dự án:** XimiLib – Hệ Thống Quản Lý Thư Viện Hiện Đại (PBL3 - ĐHBK Đà Nẵng)  
**Nhánh kiểm tra:** [`dev`](https://github.com/namtacozz/XimiLib/tree/dev)  
**Tác giả thực hiện:** Nguyễn Việt Nhật (`Nhatgreen <nguyenvietnhat30@gmail.com>`)  
**Thời gian thực hiện:** Từ 28/10/2026 đến 03/11/2026  
**Người lập báo cáo:** Antigravity Pairing Assistant  
**Ngày lập báo cáo:** 04/11/2026  

---

## 1. TỔNG QUAN ĐÓNG GÓP (EXECUTIVE SUMMARY)

Trong giai đoạn từ ngày 28/10/2026 đến 03/11/2026, thành viên **Nguyễn Việt Nhật** đã thực hiện một khối lượng công việc tái cấu trúc và mở rộng hệ thống rất lớn trên nhánh `dev`. Nhánh này tập trung nâng cấp toàn diện tầng **Dữ liệu (DAL / Persistence)**, **Hạ tầng (Infrastructure)**, **Bảo mật (Security)**, **Kiến trúc Clean Architecture**, **Tự động hóa CSDL & Migrations**, và **Công cụ thiết lập tự động (DevOps)**.

### Thống kê định lượng:
* **Tổng số files thay đổi:** 184 files.
* **Số dòng thêm mới:** +6.938 dòng.
* **Số dòng xóa bỏ / tái cấu trúc:** -1.187 dòng.
* **Số lượng Pull Requests:** 17 PRs (từ PR #1 đến PR #17) đã được merge hoàn tất.
* **Số lượng Commits:** 28 commits liên quan trực tiếp đến các tính năng cốt lõi.
* **Độ bao phủ kiểm thử (Testing):** Bổ sung hơn 12 test suites mới (Unit tests, Integration tests, Architecture tests), nâng tổng số bài test chạy tự động lên mức bao phủ toàn bộ các use case nghiệp vụ.

```
+-----------------------------------------------------------------------------------+
|                        SƠ ĐỒ TỔNG THỂ CÁC ĐÓNG GÓP                                |
+-----------------------------------------------------------------------------------+
| 1. CLEAN ARCHITECTURE  | Triệt tiêu SQL khỏi UI, tạo Ports, tách Model khỏi DTO    |
| 2. PURE JDBC & DAL     | Chuyển sang DriverManager, xử lý mã lỗi MySQL tiếng Việt  |
| 3. TRANSACTION MGMT    | TransactionCoordinator & TransactionManager độc lập       |
| 4. DATABASE & MIGRATION| 4 Migrations (V1->V4), DatabaseInitializer, cờ --init-db  |
| 5. STORAGE SUBSYSTEM   | Quản lý Khu vực (Zone) & Kệ sách (Shelf) chuẩn nghiệp vụ  |
| 6. REPORT & EXCEL POI  | Xuất .xlsx bằng Apache POI, Biểu đồ phẳng Java2D          |
| 7. BẢO MẬT HỆ THỐNG    | Ép băm BCrypt 100%, bảo mật khóa AES qua biến môi trường |
| 8. DEVOPS & SCRIPTS    | Script setup 1-click tự động hóa (setup.bat / setup.sh)   |
+-----------------------------------------------------------------------------------+
```

---

## 2. CHI TIẾT CÁC HẠNG MỤC CÔNG VIỆC CỐT LÕI

### 2.1. Chuẩn hóa Kiến trúc Clean Architecture & Phân tầng nghiêm ngặt (R-01, R-02)
* **Quy chuẩn lại Package Structure (PR #4):**
  - Chuyển toàn bộ các lớp giao diện rời rạc sang package chuẩn: `com.ximilib.presentation.view`, `com.ximilib.presentation.component`, `com.ximilib.presentation.util`.
  - Chuyển `User` về đúng `com.ximilib.domain.model` và loại bỏ package `models` cũ vi phạm phân tầng.
* **Triệt tiêu 100% câu lệnh SQL nhúng trực tiếp trong UI (PR #7):**
  - Trước đây, một số màn hình giao diện Swing vẫn gọi trực tiếp câu lệnh `SELECT ...` vào CSDL qua JDBC kết nối tĩnh. Nhật đã tái cấu trúc, đưa toàn bộ qua **Application Ports**:
    - `BookCommentPort`: Xử lý đánh giá và bình luận sách cho `BookDetailModalPanel`.
    - `FeedbackPort`: Xử lý góp ý của bạn đọc cho `FeedbackManagementView`.
    - `ReportPort`: Phục vụ truy vấn dữ liệu báo cáo thống kê cho `ReportsDashboardView`.
    - `SystemSettingsPort`: Quản lý cấu hình quy định hệ thống cho `SystemSettingsView`.
* **Phân tách hoàn toàn Domain Model khỏi DTO (PR #5):**
  - Tạo các cổng truy vấn nghiệp vụ: `BookQueryPort`, `CirculationQueryPort`, `UserQueryPort`, `DashboardQueryPort`.
  - Repository ở tầng Domain chỉ trả về Entity Domain nguyên thủy, hoàn toàn không phụ thuộc vào các DTO hiển thị của tầng Presentation.
* **Bộ kiểm thử quy tắc kiến trúc tự động (`DependencyRuleTest.java` - PR #1):**
  - Viết 465 dòng kiểm thử tự động bằng JUnit 5 để rà soát toàn bộ cấu trúc dự án:
    - Presentation không được gọi DAO hoặc `java.sql`.
    - Domain không được phụ thuộc vào Swing, FlatLaf hoặc Persistence.
    - Chặn đứng mọi vi phạm kiến trúc ngay khi chạy lệnh `mvn test`.

---

### 2.2. Quản lý Kết nối CSDL & Ranh giới Transaction (DAL / Persistence)
* **Chuyển đổi sang Pure JDBC `java.sql.DriverManager` (Commit `7a8068f`, `84c6253`):**
  - Ban đầu Nhật tích hợp thư viện connection pool HikariCP (PR #10), nhưng sau khi cân nhắc kỹ quy mô ứng dụng desktop PBL3, Nhật đã chuyển sang **Pure JDBC (`java.sql.DriverManager`)**.
  - **Lý do kỹ thuật xác đáng:** Với phần mềm desktop chạy nội bộ trên một số máy trạm, Pure JDBC mở và đóng kết nối độc lập trên từng truy vấn giúp loại bỏ hoàn toàn các lỗi tiềm ẩn của pool (deadlock thread pool, rò rỉ tài nguyên khi tắt JVM đột ngột, cấu hình shutdown hook phức tạp).
* **Bắt mã lỗi MySQL thông minh & che giấu thông tin nhạy cảm (`DatabaseConnectionManager.java` - Commit `e79111e`):**
  - Hàm `wrapConnectionException()` phân tích chi tiết mã lỗi MySQL (`errorCode`) để chuyển thành thông báo tiếng Việt trực quan cho người dùng, **tuyệt đối không để lộ chuỗi kết nối hay mật khẩu**:
    - `Mã 1045`: *"Xác thực cơ sở dữ liệu thất bại: Tên người dùng hoặc mật khẩu CSDL không chính xác."*
    - `Mã 1044`: *"Tài khoản CSDL không có quyền truy cập cơ sở dữ liệu thư viện. Vui lòng cấp quyền (GRANT) cho tài khoản."*
    - `Mã 1049`: *"Không tìm thấy cơ sở dữ liệu trên máy chủ MySQL. Vui lòng kiểm tra lại cấu hình tên CSDL hoặc khởi tạo database."*
    - `Mã 1040` / Communications link failure: Thông báo lỗi mạng hoặc dịch vụ MySQL chưa bật.
* **Phân định ranh giới Transaction (PR #11, PR #15):**
  - Tách hoàn toàn các lệnh `setAutoCommit(false)`, `commit()`, `rollback()` ra khỏi các Repository (`CirculationRepositoryImpl`, `BookRepositoryImpl`, `UserRepositoryImpl`).
  - Đưa việc điều phối transaction lên tầng Application thông qua `TransactionCoordinator` và `TransactionManager` (với `TransactionCallback` và `VoidTransactionCallback`), đảm bảo tính trọn vẹn dữ liệu (ACID) khi mượn trả nhiều cuốn sách đồng thời.
* **Kiểm thử chống rò rỉ kết nối (`DBConnectionLeakIT.java` - PR #13, Commit `e21a075`):**
  - Viết test kiểm tra tích hợp đo lường trực tiếp biến trạng thái `Threads_connected` của MySQL server qua 100 lần truy vấn liên tiếp, đảm bảo 100% tài nguyên kết nối đều được thu hồi qua `try-with-resources`.

---

### 2.3. Quản lý Cơ sở Dữ liệu & Hệ thống Migrations Tự Động
* **Hoàn thiện chuỗi 4 bản Migration (`db/migrations/`):**
  1. `V1__initial_schema.sql`: Khởi tạo 20 bảng cơ bản chuẩn `utf8mb4`, loại bỏ lệnh cố định `USE ximilib_db`.
  2. `V2__srs_schema_updates.sql`: Cập nhật cấu trúc bảng bám sát đặc tả SRS.
  3. `V3__enforce_bcrypt_passwords.sql`: Cập nhật mật khẩu mẫu của các tài khoản (`ND001` -> `ND004` và độc giả) sang chuẩn băm BCrypt.
  4. `V4__report_and_search_indexes.sql`: Bổ sung 4 chỉ mục hot tối ưu hiệu năng:
     - `idx_cuonsach_status_book`: Tăng tốc lọc bản sao theo trạng thái và mã sách.
     - `idx_phieumuon_overdue`: Tăng tốc phát hiện các phiếu mượn quá hạn.
     - `idx_phieumuon_date`: Tăng tốc báo cáo số lượt mượn theo ngày.
     - `idx_thanhtoan_date`: Tăng tốc thống kê doanh thu tiền phạt theo ngày.
* **Module `DatabaseInitializer.java` & Cờ `--init-db` (PR #12):**
  - Tự động phát hiện và thực thi chuỗi migration V1 -> V4 khi khởi động.
  - Tích hợp cờ dòng lệnh `java -jar dist/XimiLib.jar --init-db` tiện lợi cho môi trường CI/CD và triển khai trên máy tính mới mà không cần mở MySQL Workbench import thủ công.

---

### 2.4. Phân hệ Quản lý Phân khu & Kệ sách (Storage Subsystem - PR #14)
* **Domain Models:** Tạo mới thực thể `Zone` (Phân khu lưu trữ) và `Shelf` (Kệ sách), các ngoại lệ nghiệp vụ `BookException`, `CopyException`.
* **Application Services:** Xây dựng `StorageService`, `StorageServiceImpl`, `ZoneDto`, `ShelfDto`, `StorageQueryPort`.
* **Ràng buộc toàn vẹn dữ liệu nghiêm ngặt:**
  - Không cho phép xóa Phân khu nếu bên trong vẫn còn Kệ sách.
  - Không cho phép xóa Kệ sách nếu trên kệ vẫn còn Cuốn sách vật lý.
* **Tầng Hạ tầng:** Hiện thực `ZoneRepositoryImpl`, `ShelfRepositoryImpl` qua PreparedStatement và tự động tiêm phụ thuộc tại `AppContainer`.
* **Kiểm thử đầy đủ:** Bộ unit test `StorageServiceTest` (8 tests) và kiểm thử tích hợp `StorageRepositoryIT`.

---

### 2.5. Biểu đồ Xu hướng Thống kê & Xuất Báo cáo Excel POI (PR #16)
* **Xuất file Excel chuyên nghiệp với Apache POI (`ExcelReportExporter.java`):**
  - Tích hợp thư viện `org.apache.poi:poi-ooxml:5.2.5`.
  - Xuất báo cáo ra định dạng chuẩn `.xlsx` với giao diện thẩm mỹ: Dòng tiêu đề có màu nền thương hiệu, căn lề tự động theo loại dữ liệu, định dạng số tiền VND (`#,##0 "VND"`), định dạng ngày tháng `dd/MM/yyyy`, tự động co giãn độ rộng cột.
* **Biểu đồ xu hướng Java2D thuần (`TrendChartPanel.java`):**
  - Tự hiện thực biểu đồ vẽ trực tiếp bằng `Graphics2D` với phong cách phẳng bo góc:
    - Biểu đồ cột: Thể hiện số lượt mượn sách theo từng ngày trong kỳ thống kê.
    - Biểu đồ đường (Line Chart màu cam hổ phách): Thể hiện xu hướng thu tiền phạt.
  - Tích hợp trực tiếp trên `ReportsDashboardView`, sử dụng `SwingWorker` để xử lý tính toán ngầm ngoài luồng EDT, giúp giao diện không bị giật lag.

---

### 2.6. Tăng cường Bảo mật Hệ thống (Security & Cryptography)
* **Loại bỏ Backdoor mật khẩu thuần (PR #2):**
  - Triệt tiêu hoàn toàn logic cho phép so sánh mật khẩu dạng chuỗi trần (`plaintext`).
  - Ép buộc 100% tài khoản phải xác thực qua băm mật khẩu `BCrypt`.
  - Định nghĩa Application Port `PasswordHasher` và xây dựng công cụ `PasswordHashGenerator.java`.
* **Bảo mật khóa mã hóa AES-256 (Commit `1497627`):**
  - Trong `SecurityUtil`, khóa bí mật AES được đọc linh hoạt từ biến môi trường `XIMILIB_AES_KEY` hoặc từ file `config/db.properties`.
  - Loại bỏ hoàn toàn fallback chuỗi cứng mặc định trong mã nguồn.

---

### 2.7. Tự động hóa Thiết lập Môi trường & DevOps (PR #17)
* **Bộ Script cài đặt tự động 1-Click (`scripts/setup.bat` và `scripts/setup.sh`):**
  - Tự động kiểm tra phiên bản Java (yêu cầu Java 17+) và công cụ dòng lệnh MySQL.
  - Nhập mật khẩu MySQL `root` an toàn bằng PowerShell `Read-Host -AsSecureString` (không hiển thị ký tự ra màn hình, không ghi log ra đĩa).
  - Tự động tạo database `ximilib_db`, user `ximilib_app`, gán quyền đầy đủ.
  - Tự động thực thi Migrations (V1 -> V4) và Seed dữ liệu (221 đầu sách, 663 cuốn copy, bạn đọc mẫu).
  - Tự động tạo file cấu hình kết nối `config/db.properties` từ mẫu `config/db.properties.demo`.
  - Hỗ trợ tính năng Idempotent: Chạy lại nhiều lần mà không gây lỗi hoặc trùng lặp dữ liệu.
* **Nâng cấp Script chạy ứng dụng (`run.bat`, `run.sh`, `scripts/run.bat`, `scripts/run.sh`):**
  - Tự động kiểm tra sự tồn tại của file cấu hình và thông báo chạy `setup.bat` nếu chưa có.
  - Tự động kích hoạt `mvn package` nếu chưa tìm thấy file JAR trong thư mục `dist/`.
* **Soạn thảo tài liệu `README.md` mới:**
  - Viết lại toàn bộ hướng dẫn cài đặt từ A-Z, bảng thông số cấu hình và các lệnh kiểm thử dành cho thành viên mới tham gia dự án.

---

## 3. DANH SÁCH CHI TIẾT 17 PULL REQUESTS TRÊN NHÁNH `dev`

| PR # | Nhánh nguồn / Tên Task | Ngày Merge | Nội dung chi tiết |
|---|---|---|---|
| **#1** | `test/T01-arch-rule-baseline` | 28/10/2026 | Bổ sung bài kiểm tra kiến trúc `DependencyRuleTest` và danh sách ngoại lệ `arch-baseline.txt`. |
| **#2** | `fix/T01-auth-remove-plaintext-backdoor` | 29/10/2026 | Loại bỏ cơ chế xác thực mật khẩu trần, ép kiểm tra mã băm BCrypt qua `PasswordHasher`. |
| **#3** | `chore/T01-maven-pom-fatjar` | 29/10/2026 | Cấu hình Maven Shade Plugin để xuất thẳng file Fat JAR ra thư mục `dist/`. |
| **#4** | `refactor/T01-unify-presentation-package` | 29/10/2026 | Chuẩn hóa cấu trúc thư mục UI sang `com.ximilib.presentation.*`, đưa `User` về Domain. |
| **#5** | `refactor/T01-domain-decouple-dto` | 31/10/2026 | Tách biệt Domain Model và DTO qua các Query Ports: Book, Circulation, User, Dashboard. |
| **#6** | `refactor/T01-move-fine-calc-to-service` | 01/11/2026 | Đưa công thức phạt quá hạn và bồi thường mất sách về `FineCalculationService` (BR-06, BR-08). |
| **#7** | `refactor/T01-remove-sql-from-ui` | 01/11/2026 | Triệt tiêu toàn bộ câu lệnh SQL trong UI, bổ sung các Port: BookComment, Feedback, Report, Settings. |
| **#8** | `docs/T01-package-info-javadoc` | 01/11/2026 | Bổ sung file `package-info.java` cho toàn bộ các gói, chuẩn hóa Javadoc tiếng Việt. |
| **#9** | `chore/T02-db-schema-seed-sql` | 01/11/2026 | Dọn dẹp các script SQL thừa, chuẩn hóa tài khoản mẫu `ND001` - `ND004`. |
| **#10**| `feature/T02-hikari-dbconnection` | 01/11/2026 | Hiện thực ban đầu của `DatabaseConnectionManager` và file mẫu `config/db.properties.example`. |
| **#11**| `feature/T02-transaction-manager` | 02/11/2026 | Xây dựng `TransactionManager` với `TransactionCallback` cho tầng hạ tầng. |
| **#12**| `feature/T02-database-initializer` | 02/11/2026 | Xây dựng `DatabaseInitializer` tự động nạp migrations và hỗ trợ cờ `--init-db`. |
| **#13**| `test/T02-dbconnection-leak-it` | 02/11/2026 | Bài kiểm thử chống rò rỉ kết nối `DBConnectionLeakIT` (23 bảng, 221 đầu sách). |
| **#14**| `feature/T03-book-reader-shelf-repository` | 02/11/2026 | Xây dựng phân hệ Quản lý Phân khu & Kệ sách (`Zone`, `Shelf`, `StorageService`). |
| **#15**| `refactor/T01-transaction-boundary` | 02/11/2026 | Tách việc quản lý transaction khỏi Repository, chuyển giao cho `TransactionCoordinator`. |
| **#16**| `feature/T03-dashboard-charts-excel-export` | 02/11/2026 | Tích hợp biểu đồ xu hướng Java2D, xuất Excel `.xlsx` POI và Migration V4 tối ưu chỉ mục. |
| **#17**| `chore/T04-clone-and-run` | 03/11/2026 | Tích hợp bộ script cài đặt tự động 1-click `setup.bat`/`setup.sh` và cập nhật `README.md`. |

---

## 4. BẢNG SO SÁNH VÀ KHUYẾN NGHỊ KHI KẾT HỢP VỚI NHÁNH `main` CỦA BẠN (KIÊN)

| Hạng mục so sánh | Nhánh `main` (Bạn đang làm) | Nhánh `dev` (Nhật đã làm) | Hướng tích hợp đề xuất khi Merge / Rebase |
|---|---|---|---|
| **Cấu trúc Package** | Giao diện còn nằm ở package `com.ximilib.gui.*` | Đã chuẩn hóa toàn bộ về `com.ximilib.presentation.*` | **Ưu tiên áp dụng chuẩn package của `dev`** để tuân thủ 100% nguyên tắc kiến trúc Clean Architecture. |
| **Kết nối CSDL** | Sử dụng HikariCP | Sử dụng Pure JDBC `java.sql.DriverManager` | **Sử dụng `DatabaseConnectionManager` của `dev`** vì cơ chế đóng mở kết nối độc lập nhẹ hơn, không lỗi pool và thông báo lỗi tiếng Việt rất chi tiết. |
| **UI Bàn lưu hành & Trả sách** | Bạn đã sửa triệt để lỗi nhập mã tự do (`MS007`, `MS7`), tránh đè chữ giỏ sách, tối ưu modal cập nhật trạng thái phiếu mượn | Nhật tập trung vào logic backend, transaction và tầng DAL | **Bắt buộc giữ lại toàn bộ mã nguồn xử lý giao diện mới nhất của bạn** (`CirculationDeskView`, `ReturnAndCompensationView`, `ReportsDashboardView`) kết hợp với backend của Nhật. |
| **Tính năng mới bổ sung** | Tập trung vào luồng Mượn - Trả, Danh mục sách, Độc giả | Thêm Quản lý Kệ/Khu vực, Biểu đồ xu hướng, Xuất báo cáo Excel `.xlsx` POI | **Tích hợp thêm các màn hình và nút bấm mới này** vào hệ thống menu của bạn. |
| **Cài đặt & Triển khai** | Cài đặt thủ công bằng tay | Có sẵn `scripts/setup.bat` tự động 100% | **Sử dụng bộ script của `dev`** để tất cả thành viên trong nhóm cài đặt máy trạm chỉ với 1 cú click. |

---

## 5. KẾT LUẬN
Nhánh `dev` do **Nhật** phụ trách đã hoàn thành xuất sắc vai trò làm nền tảng kỹ thuật vững chắc:
1. **Kiến trúc sạch sẽ (Clean Architecture):** Tách bạch rõ ranh giới giữa các tầng, loại bỏ hoàn toàn vi phạm gọi CSDL trực tiếp từ giao diện.
2. **Độ ổn định dữ liệu:** Cơ chế Transaction phối hợp tại tầng Service và hệ thống Migration tự động giúp CSDL luôn nhất quán.
3. **Tính năng nâng cao:** Bổ sung quản lý Kệ/Khu vực, Xuất file Excel POI chuẩn và Biểu đồ thống kê phẳng Java2D.
4. **Trải nghiệm phát triển (DevOps):** Bộ script `setup.bat` và `run.bat` tự động hóa giúp giảm thiểu 100% lỗi cài đặt môi trường cho cả nhóm.

File báo cáo này được lưu trữ chính thức tại đường dẫn: `docs/BAO_CAO_DEV_NHAT.md` trong mã nguồn dự án.
