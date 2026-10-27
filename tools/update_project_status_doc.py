import sys
sys.stdout.reconfigure(encoding='utf-8')

file_path = r'F:\AppMaking\XimiLib\docs\PROJECT_STATUS.md'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Update version header
content = content.replace(
    'Version: 1.0 | Last updated: 2026-10-27',
    'Version: 1.1 | Last updated: 2026-10-27'
)

# Add F-12 to table in Section 1
f11_row = "| **F-11** | **Đánh giá & Bình luận Sách** | **Nice** | **Chưa làm (0%)** | Lê Võ Thành Nam (BE) | Độc giả viết nhận xét và chấm điểm sao cho các đầu sách đã đọc. |"
f12_row = f11_row + "\n| **F-12** | **Thống kê & Báo cáo Định kỳ (UC-7)** | **Should** | **Chưa làm (0%)** | Phan Vũ Xuân Kiên (FE/DB) | 4 nhóm thống kê theo tuần/tháng/quý/năm, quá hạn, top sách, hư hỏng; xuất file Excel (Apache POI) và PDF (OpenPDF) kèm audit log. |"
content = content.replace(f11_row, f12_row)

# Append Section 5: Intentional deviations
section_5 = """
---

## 5. Bảng Sai lệch Có chủ đích so với Đặc tả SRS (Intentional Deviations)

Theo phân tích hiện trạng và tính khả thi trong phạm vi đồ án Desktop PBL, các điểm sau đây được xác nhận là sai lệch có chủ đích so với bản đặc tả gốc `SRS.md`:

| Mục trong SRS | Cách thực hiện trong Dự án XimiLib | Lý do kỹ thuật / Phạm vi PBL |
|---|---|---|
| **UC-1: Lưu và gửi JWT Token** | Quản lý bằng `UserSession` an toàn luồng trong tiến trình JVM; không tạo token JWT. | Ứng dụng Desktop kết nối trực tiếp CSDL MySQL tập trung qua JDBC/HikariCP, không có tầng API Gateway / Microservice làm bên nhận và xác thực token JWT. |
| **§3.3.1: 5.000 user đồng thời, 95% request API < 500ms; §3.3.4: Horizontal Autoscaling; §3.3.3: Uptime 99.9%, MTTR < 30 phút** | Không áp dụng các tiêu chuẩn phân tán đám mây. Thay thế bằng: Tối ưu chỉ mục (Indexes) trên MySQL, phân trang phía server (LIMIT/OFFSET), kiểm soát kết nối qua Connection Pool, thử nghiệm đồng thời 5–10 máy trạm trong mạng LAN. | Hệ thống là đồ án Desktop Client-Server tập trung, không triển khai Kubernetes/Cloud Cluster. |
| **UC-5: "Ứng dụng di động (Mobile App)"** | Triển khai giao diện dành cho Độc giả trên cùng ứng dụng Desktop FlatLaf, phân quyền qua vai trò `READER`. | Nền tảng dự án đã chốt là Java Swing Desktop đa nền tảng. |
| **§3.3.2: Mã hóa AES-256 các trường nhạy cảm** | Áp dụng mã hóa AES-256 cho trường `Cccd` (Số CMND/CCCD) trong bảng `nguoidung`; tìm kiếm trùng bằng giá trị mã hóa cố định. Mật khẩu dùng BCrypt. | CCCD là trường thông tin nhạy cảm duy nhất về danh tính công dân trong lược đồ CSDL. |
| **Sao lưu tự động 00:00 (RPO < 24h)** | Cung cấp kịch bản `scripts/backup.bat` (Windows) và `scripts/backup.sh` (Linux/macOS) sử dụng `mysqldump` kết hợp hướng dẫn đặt lịch Task Scheduler / Cron job của HĐH. | Tách biệt sao lưu ra khỏi vòng đời ứng dụng Swing, đảm bảo an toàn ngay cả khi ứng dụng không mở. |
"""

if "## 5. Bảng Sai lệch Có chủ đích so với Đặc tả SRS" not in content:
    content = content + "\n" + section_5

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated docs/PROJECT_STATUS.md successfully.")
