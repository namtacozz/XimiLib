# XimiLib — Hệ Thống Quản Lý Thư Viện (Library Management System)

> **Đồ án PBL3:** Hệ thống Quản lý Thư viện Desktop đa nền tảng (Windows / macOS / Linux)  
> **Khoa Công nghệ Thông tin — Trường Đại học Bách Khoa, Đại học Đà Nẵng**  
> **Phiên bản:** `1.0.0` | **Nền tảng:** `Java 17 (Swing FlatLaf)` + `MySQL 8.0+`

---

## 👥 Nhóm Tác Giả & Phân Công Nhiệm Vụ

| STT | Thành viên | Vai trò | Trách nhiệm chính |
|:---:|---|:---:|---|
| 1 | **Nguyễn Việt Nhật** | **Tech Lead** | Kiến trúc hệ thống Clean Architecture, Phân quyền RBAC, Code Review & Điều phối dự án. |
| 2 | **Lê Võ Thành Nam** | **Backend (BE)** | Xử lý nghiệp vụ Mượn - Trả, FSM, Tích hợp cổng thanh toán VietQR (SePay API). |
| 3 | **Phan Vũ Xuân Kiên** | **Frontend & DB (FE/DB)** | Thiết kế CSDL 20 bảng MySQL, Xây dựng giao diện Desktop FlatLaf, Digital Card. |

---

## 📁 Cấu Trúc Cây Thư Mục Dự Án (Enterprise Java & AI-First Layout)

```text
XimiLib/                                        # [ROOT] Thư mục gốc dự án
├── .git/                                       # Git repository
├── .vscode/                                    # Cấu hình IDE VS Code (Java runtime, extensions)
├── .gitattributes                              # Chuẩn hóa kết thúc dòng (CRLF cho .bat, LF cho code/md)
├── .gitignore                                  # Danh sách loại trừ file build, log và backup
├── README.md                                   # Tài liệu giới thiệu và hướng dẫn tổng quan dự án
├── AGENTS.md                                   # Điểm vào Single Source of Truth (SSOT) cho AI Coder
├── run.bat                                     # Shortcut khởi chạy ứng dụng nhanh tại Root
│
├── docs/                                       # TÀI LIỆU KỸ THUẬT SSOT (LIVING DOCUMENTATION)
│   ├── ARCHITECTURE.md                         # Clean Architecture, DI Wiring, Observer đa máy, SePay
│   ├── DATABASE_SCHEMA.md                      # Lược đồ 20 bảng, Mermaid ERD, Pessimistic Lock, Indexes
│   ├── BUSINESS_RULES.md                       # Máy trạng thái FSM, Công thức phạt/đền bù, BR-01..BR-09
│   ├── PROJECT_STATUS.md                       # Ma trận F-01..F-11 MoSCoW, DoD, Changelog Week 10
│   └── academic/                               # Báo cáo và tài liệu học thuật đồ án PBL
│       ├── BaoCao_TienDo_GiuaKi_PBL_Nhom1.docx # Báo cáo tiến độ giữa kỳ
│       ├── Dac_Ta_SRS_XimiLib.docx             # Đặc tả yêu cầu phần mềm chuẩn IEEE 830
│       ├── Tong_Quan_Kien_Truc_Patterns.docx   # Tài liệu phân tích kiến trúc và Design Patterns
│       └── pbl_workspace.html                  # Công cụ quản lý tiến độ & ERD tương tác của nhóm
│
├── db/                                         # QUẢN TRỊ CƠ SỞ DỮ LIỆU MYSQL 8.0+
│   ├── schema.sql                              # Script DDL 20 bảng, khóa ngoại và chỉ mục chính thức
│   ├── migrations/                             # Các bản migration phiên bản hóa (V1, V2, V3)
│   └── seed/                                   # Dữ liệu mẫu (sách, bản sao, phiếu mượn demo)
│
├── assets/                                     # TÀI NGUYÊN HÌNH ẢNH & MOCKUP
│   ├── librack_exact_dashboard.jpg             # Ảnh mẫu thiết kế giao diện Dashboard
│   ├── librack_exact_login.jpg                 # Ảnh mẫu thiết kế giao diện Đăng nhập
│   └── usecase.jpg                             # Sơ đồ Use Case tổng thể hệ thống
│
├── sources/                                    # MÃ NGUỒN DỰ ÁN MAVEN (Java 17)
│   ├── pom.xml                                 # Cấu hình Maven dependencies & Assembly plugin
│   └── src/
│       ├── main/java/com/ximilib/              # Mã nguồn các tầng (domain, application, infra, presentation)
│       ├── main/resources/                     # Cấu hình runtime và tài nguyên đóng gói trong JAR
│       └── test/java/com/ximilib/              # Unit tests (JUnit 5 + Mockito)
│
├── scripts/                                    # CÔNG CỤ TỰ ĐỘNG HÓA BUILD & RUN
│   ├── build.bat                               # Lệnh build Maven & tự động xuất file JAR ra dist/
│   └── run.bat                                 # Kịch bản chạy ứng dụng từ dist/XimiLib.jar
│
├── dist/                                       # BẢN PHÁT HÀNH ĐÓNG GÓI (DISTRIBUTION)
│   └── XimiLib.jar                             # File thực thi Java độc lập (Fat JAR kèm dependencies)
│
└── logs/                                       # NHẬT KÝ HOẠT ĐỘNG HỆ THỐNG
    └── .gitkeep                                # Giữ thư mục logs trong Git
```

---

## ⚡ Hướng Dẫn Cài Đặt & Khởi Chạy Nhanh (Quick Start)

### 1. Yêu cầu Tiên quyết
- **Java Development Kit (JDK):** Phiên bản **Java 17** trở lên.
- **Apache Maven:** Phiên bản **3.8+** (nếu build từ mã nguồn).
- **Hệ quản trị CSDL:** **MySQL 8.0+** (kết nối cổng mặc định `3306`).

### 2. Cấu hình Kết nối CSDL
Hệ thống sử dụng cơ chế nạp cấu hình ưu tiên biến môi trường, sau đó đọc file cục bộ:
1. Sao chép file cấu hình mẫu:
   ```bash
   cp config/db.properties.example config/db.properties
   ```
2. Điền thông tin kết nối CSDL và khóa mã hóa vào `config/db.properties`:
   - `db.host=localhost`
   - `db.port=3306`
   - `db.name=ximilib_db`
   - `db.user=ximilib_app`
   - `db.password=<mat_khau_cua_ban>`
   - `security.aes.key=<khoa_aes_256>`
   *(Hoặc truyền qua biến môi trường: `XIMILIB_DB_HOST`, `XIMILIB_DB_PORT`, `XIMILIB_DB_NAME`, `XIMILIB_DB_USER`, `XIMILIB_DB_PASS`, `XIMILIB_AES_KEY`).*

### 3. Khởi tạo Cơ sở Dữ liệu & Migration
- **Cách 1 (Khuyên dùng - Tự động hoàn toàn qua cờ `--init-db`):**
  Chạy lệnh khởi tạo headless để hệ thống tự động kiểm tra CSDL, nạp schema và thực thi chuỗi migration V1 -> V2 -> V3 -> V4:
  ```bash
  java -jar dist/XimiLib.jar --init-db
  ```
- **Cách 2 (Thủ công qua MySQL Client):**
  ```bash
  mysql -u root -p ximilib_db < db/schema.sql
  mysql -u root -p ximilib_db < db/migrations/V2__srs_schema_updates.sql
  mysql -u root -p ximilib_db < db/migrations/V3__enforce_bcrypt_passwords.sql
  mysql -u root -p ximilib_db < db/migrations/V4__report_and_search_indexes.sql
  mysql -u root -p ximilib_db < db/seed/seed_books.sql
  ```

### 4. Đóng gói Ứng dụng (Build)
Chạy script tự động hóa:
- **Windows:** `scripts\build.bat`
- **Linux / macOS:** `./scripts/build.sh` (hoặc `mvn clean package -f sources/pom.xml -DskipTests`)

File JAR thực thi sẽ tự động được tạo tại `dist/XimiLib.jar`.

### 5. Khởi chạy Ứng dụng (Run)
- **Windows:** Click đúp `run.bat` hoặc chạy `scripts\run.bat`.
- **Linux / macOS:** `./run.sh` hoặc `./scripts/run.sh`.
- **Dòng lệnh Java:**
  ```bash
  java -jar dist/XimiLib.jar
  ```

### 6. Chạy Kiểm Thử Tự Động (Test)
```bash
mvn test -f sources/pom.xml
```

---

## 📖 Hệ Thống Tài Liệu Kỹ Thuật (Single Source of Truth - SSOT)

Toàn bộ quy chuẩn phát triển và ràng buộc kiến trúc được quy định tập trung:

- **Hướng dẫn cho AI Agent:** [AGENTS.md](file:///f:/AppMaking/XimiLib/AGENTS.md) — Điểm vào bắt buộc cho AI Coder, 10 điều răn bất khả xâm phạm.
- **Kiến trúc phần mềm:** [docs/ARCHITECTURE.md](file:///f:/AppMaking/XimiLib/docs/ARCHITECTURE.md) — Clean Architecture, Pure DI Container, SePay Gateway, Observer đa máy.
- **Lược đồ cơ sở dữ liệu:** [docs/DATABASE_SCHEMA.md](file:///f:/AppMaking/XimiLib/docs/DATABASE_SCHEMA.md) — Ma trận 20 bảng, Mermaid ERD, Từ điển dữ liệu, Khóa bi quan.
- **Quy tắc nghiệp vụ:** [docs/BUSINESS_RULES.md](file:///f:/AppMaking/XimiLib/docs/BUSINESS_RULES.md) — FSM Phiếu mượn/Cuốn sách, Công thức phạt & bồi thường (BR-01..BR-09).
- **Tiến độ dự án:** [docs/PROJECT_STATUS.md](file:///f:/AppMaking/XimiLib/docs/PROJECT_STATUS.md) — Ma trận tính năng F-01..F-11, DoD, Nhật ký Sprint Week 10.
- **Công cụ PBL Workspace:** Mở trực tiếp [docs/academic/pbl_workspace.html](file:///f:/AppMaking/XimiLib/docs/academic/pbl_workspace.html) trên trình duyệt để quản lý tiến độ nhóm và sơ đồ thực thể tương tác.
