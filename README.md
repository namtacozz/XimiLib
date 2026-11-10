# XimiLib — Hệ Thống Quản Lý Thư Viện (Library Management System)

> **Thư viện làng Xì Mi:** Hệ thống Quản lý Thư viện Desktop đa nền tảng (Windows / macOS / Linux)  
> **Đơn vị vận hành: Thư viện làng Xì Mi**  
> **Phiên bản:** `1.1.0` | **Nền tảng:** `Java 25 LTS (Swing FlatLaf)` + `MySQL 8.0+` (JDBC thuần `DriverManager`)

---

## 👥 Nhóm Tác Giả & Phân Công Nhiệm Vụ

| STT | Thành viên | Vai trò | Trách nhiệm chính |
|:---:|---|:---:|---|
| 1 | **Nguyễn Việt Nhật** | **Tech Lead** | Kiến trúc hệ thống Clean Architecture, Phân quyền RBAC, Code Review & Điều phối dự án. |
| 2 | **Lê Võ Thành Nam** | **Backend (BE)** | Xử lý nghiệp vụ Mượn - Trả, FSM, Tích hợp cổng thanh toán VietQR (SePay API). |
| 3 | **Phan Vũ Xuân Kiên** | **Frontend & DB (FE/DB)** | Thiết kế CSDL 23 bảng MySQL, Xây dựng giao diện Desktop FlatLaf, Digital Card. |

---

## 🚀 Chạy trên máy mới (Dành cho thành viên clone lần đầu)

> [!NOTE] Lưu ý quan trọng
> Bản đóng gói `dist/XimiLib.jar` không nằm trong Git (được loại trừ bởi `.gitignore`). Khi clone dự án về máy tính mới, bạn chỉ cần thực hiện thiết lập tự động hoặc thủ công theo hướng dẫn dưới đây để sẵn sàng chạy.

### 1. Yêu cầu Tiên quyết
- **Java Development Kit (JDK):** Phiên bản **Java 25 LTS** (hoặc Java 21+).
- **Apache Maven:** Phiên bản **3.8+**.
- **Hệ quản trị CSDL:** **MySQL 8.0+** đang hoạt động (cổng mặc định `3306`).

### 2. Thiết lập & Khởi chạy tự động (Khuyên dùng)
Hệ thống cung cấp script thiết lập toàn diện, an toàn và có tính chất **idempotent** (chạy lại nhiều lần không hỏng dữ liệu):

```bash
# Bước 1: Clone kho mã nguồn về máy
git clone <URL_REPO>
cd XimiLib

# Bước 2: Chạy script thiết lập môi trường CSDL tự động
# Trên Linux / macOS:
./scripts/setup.sh

# Trên Windows (cmd / powershell):
scripts\setup.bat

# Bước 3: Khởi chạy ứng dụng (tự động biên dịch và tạo dist/XimiLib.jar nếu chưa có)
# Trên Linux / macOS:
./run.sh

# Trên Windows:
run.bat
```

*Script `setup` sẽ tự động kiểm tra JDK, MySQL client, hỏi mật khẩu root MySQL ẩn danh, tạo cơ sở dữ liệu `ximilib_db`, cấp quyền tài khoản `ximilib_app`, nạp chuỗi migrations V1 -> V2 -> V3 -> V4 -> V5, nạp 221 đầu sách mẫu và tạo sẵn file `config/db.properties`.*

### 3. Danh sách tài khoản thử nghiệm (Demo Accounts)
Mật khẩu đã được mã hóa bằng chuẩn BCrypt trong cơ sở dữ liệu mẫu, tuân thủ chính sách mật khẩu SRS (tối thiểu 8 ký tự, gồm ít nhất 1 chữ hoa, 1 số và 1 ký tự đặc biệt theo BR-10):

| Tài khoản | Mật khẩu chuẩn SRS | Mật khẩu cũ (tương thích) | Vai trò | Quyền hạn chính |
|---|---|---|---|---|
| `admin` | `Admin@123` | `admin123` | **Admin** (Quản trị viên) | Toàn quyền hệ thống, cấu hình tham số, quản lý người dùng, xem báo cáo |
| `kien.phan` | `Kien@123456` | `123456` | **Thủ thư** (Librarian) | Quản lý kho sách, lập phiếu mượn/trả, gia hạn, xử lý bồi thường phạt |
| `nam.le` | `Nam@123456` | `123456` | **Thủ thư** (Librarian) | Quản lý kho sách, lập phiếu mượn/trả, gia hạn, xử lý bồi thường phạt |
| `sinhvien` | `Sinhvien@123` | `123456` | **Độc giả** (Reader) | Tra cứu sách, xem lịch sử mượn trả cá nhân, xuất thẻ thư viện QR |

### 4. Quy trình thiết lập thủ công thay thế (Manual Setup)
Nếu bạn muốn tự thiết lập từng bước thay vì dùng script:
1. **Tạo CSDL và cấp quyền:**
   ```sql
   CREATE DATABASE IF NOT EXISTS ximilib_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER IF NOT EXISTS 'ximilib_app'@'localhost' IDENTIFIED BY 'ximilib_demo_123';
   GRANT ALL PRIVILEGES ON ximilib_db.* TO 'ximilib_app'@'localhost';
   FLUSH PRIVILEGES;
   ```
2. **Nạp migrations và dữ liệu mẫu theo thứ tự:**
   ```bash
   mysql -u ximilib_app -p ximilib_db < db/migrations/V1__initial_schema.sql
   mysql -u ximilib_app -p ximilib_db < db/migrations/V2__srs_schema_updates.sql
   mysql -u ximilib_app -p ximilib_db < db/migrations/V3__enforce_bcrypt_passwords.sql
   mysql -u ximilib_app -p ximilib_db < db/migrations/V4__report_and_search_indexes.sql
   mysql -u ximilib_app -p ximilib_db < db/migrations/V5__add_audit_log.sql
   mysql -u ximilib_app -p ximilib_db < db/seed/seed_books.sql
   ```
3. **Cấu hình kết nối CSDL:**
   Tạo file `config/db.properties` dựa trên mẫu `config/db.properties.example` và điền thông tin tài khoản MySQL của bạn.
4. **Đóng gói và khởi chạy:**
   ```bash
   mvn clean package -f sources/pom.xml -DskipTests
   java -jar dist/XimiLib.jar
   ```

### 5. Xử lý các sự cố thường gặp (Troubleshooting)
- **Lỗi xác thực CSDL (Mã 1045):** Tên người dùng hoặc mật khẩu không chính xác trong `config/db.properties`. Kiểm tra lại khóa `db.user` và `db.password`.
- **Lỗi thiếu quyền truy cập CSDL (Mã 1044):** Tài khoản người dùng chưa được cấp quyền thao tác trên CSDL `ximilib_db`. Chạy lệnh `GRANT ALL PRIVILEGES ON ximilib_db.* TO 'ximilib_app'@'localhost'; FLUSH PRIVILEGES;` bằng tài khoản root.
- **Lỗi không tìm thấy CSDL (Mã 1049):** CSDL `ximilib_db` chưa được tạo. Hãy chạy script `./scripts/setup.sh` hoặc `CREATE DATABASE ximilib_db;`.
- **Lỗi máy chủ MySQL chưa chạy / Connection refused:** Dịch vụ MySQL chưa hoạt động trên máy hoặc sai cổng (mặc định 3306).
- **Lỗi chạy sai thư mục làm việc:** Luôn thực hiện chạy lệnh từ thư mục gốc của repository `XimiLib/`, tránh chạy từ thư mục con để ứng dụng tìm đúng file cấu hình `config/db.properties`.

---

## 📁 Cấu Trúc Cây Thư Mục Dự Án

```text
XimiLib/                                        # [ROOT] Thư mục gốc dự án
├── .git/                                       # Git repository
├── .vscode/                                    # Cấu hình IDE VS Code (Java runtime, extensions)
├── .gitattributes                              # Chuẩn hóa kết thúc dòng (CRLF cho .bat, LF cho code/md)
├── .gitignore                                  # Danh sách loại trừ file build, log và backup
├── README.md                                   # Tài liệu giới thiệu và hướng dẫn tổng quan dự án
├── run.sh / run.bat                            # Shortcut khởi chạy ứng dụng nhanh tại Root
│
├── config/                                     # CẤU HÌNH HỆ THỐNG
│   ├── db.properties.example                   # File mẫu cấu hình rỗng
│   └── db.properties                           # File cấu hình thực tế tại máy (bị .gitignore bỏ qua)
│
├── docs/                                       # TÀI LIỆU KỸ THUẬT DỰ ÁN
│   ├── ARCHITECTURE.md                         # Clean Architecture, DI Wiring, Observer đa máy, SePay
│   ├── DATABASE_SCHEMA.md                      # Lược đồ 23 bảng, Mermaid ERD, Pessimistic Lock, Indexes
│   ├── BUSINESS_RULES.md                       # Máy trạng thái FSM, Công thức phạt & bồi thường (BR-01..BR-14)
│   ├── PROJECT_STATUS.md                       # Ma trận F-01..F-12 MoSCoW, DoD, Các cột mốc dự án
│   ├── TRACEABILITY.md                         # Ma trận truy vết YC-01..YC-14, UC 3.2.1..3.2.9, Deviation Register
│   ├── CODING_STYLE.md                         # Chuẩn phong cách mã nguồn sinh viên low-code (R-13..R-17)
│   ├── DEMO_AND_DEFENSE_GUIDE.md               # Cẩm nang 8 luồng nghiệp vụ & 24 câu hỏi vấn đáp đồ án PBL
│   └── academic/                               # Báo cáo và tài liệu học thuật đồ án PBL
│       ├── BaoCao_TienDo_GiuaKi_PBL_Nhom1.docx # Báo cáo tiến độ giữa kỳ
│       ├── Dac_Ta_SRS_XimiLib.docx             # Đặc tả yêu cầu phần mềm chuẩn IEEE 830
│       ├── Tong_Quan_Kien_Truc_Patterns.docx   # Tài liệu phân tích kiến trúc và Design Patterns
│       └── pbl_workspace.html                  # Công cụ quản lý tiến độ & ERD tương tác của nhóm
│
├── db/                                         # QUẢN TRỊ CƠ SỞ DỮ LIỆU MYSQL 8.0+
│   ├── schema.sql                              # Script DDL 20 bảng, khóa ngoại và chỉ mục chính thức
│   ├── migrations/                             # Các bản migration phiên bản hóa (V1 -> V5)
│   └── seed/                                   # Dữ liệu mẫu (221 đầu sách, bản sao, phiếu mượn demo)
│
├── sources/                                    # MÃ NGUỒN DỰ ÁN MAVEN (Java 25 LTS)
│   ├── pom.xml                                 # Cấu hình Maven dependencies & Assembly plugin
│   └── src/
│       ├── main/java/com/ximilib/              # Mã nguồn các tầng (domain, application, infra, presentation)
│       ├── main/resources/                     # Cấu hình runtime và tài nguyên đóng gói trong JAR
│       └── test/java/com/ximilib/              # Unit tests nghiệp vụ cốt lõi (JUnit 5)
│
├── scripts/                                    # CÔNG CỤ TỰ ĐỘNG HÓA BUILD VÀ SETUP
│   ├── setup.sh / setup.bat                    # Script thiết lập tự động môi trường CSDL local
│   └── build.sh / build.bat                    # Lệnh build Maven & tự động xuất file JAR ra dist/
│
└── dist/                                       # BẢN PHÁT HÀNH ĐÓNG GÓI (DISTRIBUTION - Không commit vào Git)
    └── XimiLib.jar                             # File thực thi Java độc lập (Fat JAR sinh ra khi build)
```

---

## 📖 Hệ Thống Tài Liệu Kỹ Thuật

Toàn bộ quy chuẩn phát triển và ràng buộc kiến trúc được quy định tập trung:

- **Kiến trúc phần mềm:** [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — Clean Architecture, Pure DI Container, SePay Gateway, Observer đa máy.
- **Lược đồ cơ sở dữ liệu:** [docs/DATABASE_SCHEMA.md](docs/DATABASE_SCHEMA.md) — Ma trận 23 bảng, Mermaid ERD, Từ điển dữ liệu, Khóa bi quan.
- **Quy tắc nghiệp vụ:** [docs/BUSINESS_RULES.md](docs/BUSINESS_RULES.md) — Máy trạng thái FSM Phiếu mượn/Cuốn sách, Công thức phạt & bồi thường (BR-01..BR-14).
- **Tiến độ dự án:** [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) — Ma trận tính năng F-01..F-12, DoD, Các cột mốc dự án.
- **Ma trận truy vết & Sai lệch:** [docs/TRACEABILITY.md](docs/TRACEABILITY.md) — Ánh xạ YC-01..YC-14, UC 3.2.1..3.2.9 và Deviation Register DEV-01..DEV-10.
- **Chuẩn phong cách mã nguồn:** [docs/CODING_STYLE.md](docs/CODING_STYLE.md) — Hướng dẫn phong cách sinh viên low-code (R-13..R-17), cấu trúc rõ ràng, biến tường minh.
- **Cẩm nang demo & vấn đáp đồ án:** [docs/DEMO_AND_DEFENSE_GUIDE.md](docs/DEMO_AND_DEFENSE_GUIDE.md) — Hướng dẫn kịch bản 8 luồng nghiệp vụ cốt lõi và ngân hàng câu hỏi - trả lời bảo vệ đồ án trước hội đồng.
- **Công cụ PBL Workspace:** Mở trực tiếp [docs/academic/pbl_workspace.html](docs/academic/pbl_workspace.html) trên trình duyệt để quản lý tiến độ nhóm và sơ đồ thực thể tương tác.
