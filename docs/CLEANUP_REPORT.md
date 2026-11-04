# BÁO CÁO KIỂM KÊ VÀ ĐỀ XUẤT DỌN DẸP KHO MÃ NGUỒN XIMILIB
**Mã đợt dọn dẹp:** `CLEANUP-20261104`  
**Ngày thực hiện:** 04/11/2026  
**Nhánh thao tác:** `chore/repo-cleanup` (Tạo từ tag an toàn: `pre-cleanup-20261104`)  
**Bản sao lưu an toàn toàn diện:** `F:\AppMaking\XimiLib_backup_20261104.zip` (17.5 MB)

---

## 1. TỔNG QUAN HIỆN TRẠNG REPOSITORY (BASELINE GIAI ĐOẠN 0)
* **Tổng số file tracked bởi Git:** **518 files**
* **Tổng dung lượng thư mục dự án:** **195.61 MB** (bao gồm cả `.git`, `dist` và `sources/target`)
* **Kiểm thử tự động:** **121 / 121 test cases JUnit 5 PASS 100%** (Thời gian chạy: 36.16s).
* **Kiến trúc Clean Architecture:** `DependencyRuleTest` (2 tests) và `StyleRuleTest` (8 tests) **Xanh 100%**.

---

## 2. BẢNG PHÂN LOẠI CHI TIẾT CÁC ĐỐI TƯỢNG KIỂM KÊ (NHÓM A, B, C, D, E)

| STT | Đường Dẫn Đối Tượng | Nhóm | Loại File | Dung Lượng | Trạng Thái Git | Bằng Chứng Tham Chiếu | Đề Xuất Xử Lý | Mức Độ Rủi Ro |
|:---:|---|:---:|---|:---:|:---:|---|---|:---:|
| 1 | `database/raw_from_workspace.sql` | **A** | SQL dump cũ | 9.7 KB | Tracked | 0 tham chiếu trong code/scripts/docs. CSDL chuẩn nằm tại `db/schema.sql` và `db/migrations/`. | Xóa qua `git rm` | **Không có (0%)** |
| 2 | `database/backups/.gitkeep` | **A** | Placeholder | 0 B | Tracked | Thư mục `database/` trùng ý và đã lỗi thời so với thư mục chuẩn `db/`. | Xóa qua `git rm` | **Không có (0%)** |
| 3 | `db/fix_passwords.sql` | **A** | Script patch cũ | 392 B | Tracked | 0 tham chiếu. Script chạy tay vá hash admin, hiện logic đã chuyển vào `AuthenticationServiceImpl`. | Xóa qua `git rm` | **Không có (0%)** |
| 4 | `tools/tools/DataMigrator.class` | **A** | Binary `.class` | 11.2 KB | Untracked (Ignored) | File nhị phân biên dịch rơi vãi trong thư mục tools. | Chuyển ra thư mục cách ly | **Không có (0%)** |
| 5 | `tools/tools/SeedUsers.class` | **A** | Binary `.class` | 4.1 KB | Untracked (Ignored) | File nhị phân biên dịch rơi vãi trong thư mục tools. | Chuyển ra thư mục cách ly | **Không có (0%)** |
| 6 | `logs/db-init-*.log` (5 files) | **A** | Runtime Logs | 1.8 KB | Untracked (Ignored) | Log tạm sinh ra khi chạy cờ `--init-db`. | Dọn file log, giữ lại `logs/.gitkeep` | **Không có (0%)** |
| 7 | `sources/logs/audit.log` | **A** | Runtime Log | 3.1 KB | Untracked (Ignored) | Log kiểm toán sinh ra trong quá trình chạy test JUnit. | Giữ thư mục, dọn file log | **Không có (0%)** |
| 8 | `import-report/` (9 files CSV) | **B** | Dữ liệu CSV tạm | 63.4 KB | Tracked | 0 tham chiếu. Báo cáo đối soát khi nạp 221 đầu sách vào `seed_books.sql`. | Xóa qua `git rm` (hoặc lưu archive) | **Thấp** (đã có seed SQL) |
| 9 | `tools/*.py` (13 scripts Python) | **B** | Script dev cũ | ~45 KB | Tracked | 0 tham chiếu trong scripts/CI/docs. Dùng trong giai đoạn phân tích dữ liệu ban đầu. | Xóa qua `git rm` | **Thấp** |
| 10 | `tools/DataMigrator.java` | **B** | Java tool độc lập | 11.9 KB | Tracked | Không còn tham chiếu trong AGENTS.md hay README.md. | Xóa qua `git rm` | **Thấp** |
| 11 | `tools/PasswordHashGenerator.java` | **B** | Java tool độc lập | 1.4 KB | Tracked | Tiện ích sinh hash BCrypt thủ công cho dev. | Xóa qua `git rm` hoặc chuyển `scripts/` | **Thấp** |
| 12 | `tools/SeedUsers.java` | **B** | Java tool độc lập | 4.7 KB | Tracked | Tiện ích sinh seed user vào MySQL. CSDL đã có migration V3/V5. | Xóa qua `git rm` | **Thấp** |
| 13 | `assets/loginpics/plushy-woman-work.jpg` | **B** | Ảnh JPEG thô | 3.3 MB | Tracked | Ảnh gốc chưa nén. Ứng dụng thật nạp ảnh đã tối ưu tại `resources/images/login/` (118 KB). | Xóa khỏi repo để giảm 3.3 MB | **Thấp** |
| 14 | `docs/BAO_CAO_DEV_NHAT.md` | **C** | Báo cáo cá nhân | 18.9 KB | Tracked | Báo cáo tiến độ cá nhân của Dev Nhật trong Sprint 17. | Di chuyển sang `docs/archive/` | **Không có** |
| 15 | `.gitignore` cập nhật | **C** | File cấu hình Git | 948 B | Tracked | Cần bổ sung ignore cho: `logs/*.log`, `sources/logs/`, `tools/tools/`. | Cập nhật `.gitignore` | **Không có** |
| 16 | Toàn bộ Danh sách Bảo Vệ | **D** | Mã nguồn, DB, Docs | ~150 MB | Tracked | Được bảo vệ tuyệt đối theo quy tắc duy trì hệ thống. | **GIỮ NGUYÊN 100%** | **0%** |
| 17 | `config/db.properties` (thật) | **E** | Cấu hình DB thật | 735 B | Untracked (Ignored) | Chứa thông tin đăng nhập DB cục bộ của máy dev. Đã nằm ngoài Git an toàn. | Giữ nguyên tại máy dev | **An toàn** |

---

## 3. THỐNG KÊ DỰ KIẾN SAU KHI DỌN DẸP ĐƯỢC PHÊ DUYỆT

* **Số lượng file tracked dự kiến giảm:** **27 files** (từ 518 xuống 491 files).
* **Dung lượng repository dự kiến giảm:** Giảm **~3.5 MB** trực tiếp trong Git (chủ yếu từ file ảnh thô 3.3MB và các file SQL/CSV/scripts rác).
* **Độ sạch cây thư mục gốc:** Loại bỏ hoàn toàn 3 thư mục rác/lỗi thời ở cấp 1 (`database/`, `import-report/`, `tools/`). Cây thư mục gốc chỉ còn các thư mục tiêu chuẩn:
  `assets/`, `config/`, `db/`, `dist/`, `docs/`, `logs/`, `scripts/`, `sources/`.

---

## 4. KẾ HOẠCH CÁCH LY & LỆNH KHÔI PHỤC (ROLLBACK PLAN)

1. Mọi file untracked bị dọn dẹp sẽ được di chuyển vào thư mục cách ly:
   `F:\AppMaking\XimiLib_trash_20261104/` giữ nguyên cấu trúc thư mục.
2. Mọi file tracked được dọn dẹp qua lệnh `git rm` trên nhánh `chore/repo-cleanup`.
3. Khi cần khôi phục bất kỳ file nào:
   ```bash
   git checkout pre-cleanup-20261104 -- <đường_dẫn_file>
   ```
   hoặc giải nén từ bản backup: `F:\AppMaking\XimiLib_backup_20261104.zip`.
