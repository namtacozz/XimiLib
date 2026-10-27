import sys
sys.stdout.reconfigure(encoding='utf-8')

file_path = r'F:\AppMaking\XimiLib\docs\DATABASE_SCHEMA.md'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Update version header
content = content.replace(
    'Version: 1.0 | Last updated: 2026-10-27',
    'Version: 1.1 | Last updated: 2026-10-27'
)

# Update nguoidung in Section 4.1
old_nguoidung = """- **`nguoidung`**:
  - `ID` (`VARCHAR(50)`, PK, NOT NULL): Mã người dùng (ví dụ: `ND001`).
  - `Tendangnhap` (`VARCHAR(50)`, NOT NULL, UNIQUE): Tên đăng nhập.
  - `Matkhau` (`VARCHAR(255)`, NOT NULL): Hash mật khẩu BCrypt.
  - `Hoten` (`NVARCHAR(100)`, NOT NULL): Họ và tên đầy đủ.
  - `Email` (`VARCHAR(100)`, UNIQUE, NULL): Thư điện tử.
  - `Gioitinh` (`BOOLEAN`, NULL): Giới tính (1: Nam, 0: Nữ).
  - `Sdt` (`VARCHAR(20)`, NULL): Số điện thoại liên hệ.
  - `Mavaitro` (`VARCHAR(50)`, FK → `vaitro.Mavaitro`, NOT NULL): Quyền hạn.
  - `Trangthai` (`INT`, NOT NULL, DEFAULT 1): 1: Hoạt động, 0: Khóa."""

new_nguoidung = """- **`nguoidung`**:
  - `ID` (`VARCHAR(50)`, PK, NOT NULL): Mã người dùng (ví dụ: `ND001`).
  - `Tendangnhap` (`VARCHAR(50)`, NOT NULL, UNIQUE): Tên đăng nhập.
  - `Matkhau` (`VARCHAR(255)`, NOT NULL): Hash mật khẩu BCrypt (chuẩn ≥ 8 ký tự, 1 hoa, 1 số, 1 ký tự đặc biệt).
  - `Hoten` (`NVARCHAR(100)`, NOT NULL): Họ và tên đầy đủ.
  - `Email` (`VARCHAR(100)`, UNIQUE, NULL): Thư điện tử.
  - `Gioitinh` (`BOOLEAN`, NULL): Giới tính (1: Nam, 0: Nữ).
  - `Ngaysinh` (`DATE`, NULL): Ngày sinh của độc giả (UC-2).
  - `Diachi` (`NVARCHAR(255)`, NULL): Địa chỉ cư trú (UC-2).
  - `Cccd` (`VARCHAR(255)`, NULL): Số CCCD 9 hoặc 12 chữ số mã hóa AES-256 (C4).
  - `Sdt` (`VARCHAR(20)`, UNIQUE, NULL): Số điện thoại liên hệ (C13).
  - `Mavaitro` (`VARCHAR(50)`, FK → `vaitro.Mavaitro`, NOT NULL): Quyền hạn.
  - `Trangthai` (`INT`, NOT NULL, DEFAULT 1): 1: Hoạt động, 0: Khóa bởi thủ thư.
  - `solansai` (`INT`, NOT NULL, DEFAULT 0): Số lần đăng nhập sai liên tiếp (BR-01).
  - `khoaden` (`DATETIME`, NULL): Thời điểm kết thúc khóa tạm 15 phút (BR-01)."""

content = content.replace(old_nguoidung, new_nguoidung)

# Update phieumuon in Section 4.4
old_phieumuon = """- **`phieumuon`**:
  - `Maphieumuon` (`VARCHAR(50)`, PK, NOT NULL): Mã phiếu mượn (`PM001`).
  - `ID` (`VARCHAR(50)`, FK → `nguoidung.ID`, NOT NULL): Độc giả mượn.
  - `Mathe` (`VARCHAR(50)`, FK → `thethuvien.Mathe`, NOT NULL): Thẻ dùng mượn.
  - `Ngaymuon` (`DATE`, NOT NULL): Ngày tạo giao dịch mượn.
  - `Ngaytra` (`DATE`, NULL): Hạn trả ấn định (14 ngày sau theo BR-02).
  - `Trangthai` (`INT`, NOT NULL, DEFAULT 1): 1: Đang mượn, 2: Đã trả, 3: Quá hạn.
  - `Ghichuphieu` (`NVARCHAR(255)`, NULL): Ghi chú."""

new_phieumuon = """- **`phieumuon`**:
  - `Maphieumuon` (`VARCHAR(50)`, PK, NOT NULL): Mã phiếu mượn (`PM001`).
  - `ID` (`VARCHAR(50)`, FK → `nguoidung.ID`, NOT NULL): Độc giả mượn.
  - `Mathe` (`VARCHAR(50)`, FK → `thethuvien.Mathe`, NOT NULL): Thẻ dùng mượn.
  - `Hinhthuc` (`ENUM('TAI_CHO', 'VE_NHA')`, NOT NULL, DEFAULT 'VE_NHA'): Hình thức mượn (BR-04, C3).
  - `Ngaymuon` (`DATE`, NOT NULL): Ngày tạo giao dịch mượn.
  - `Ngaytra` (`DATE`, NULL): Hạn trả ấn định (14 ngày đối với `VE_NHA`, 17:00 đối với `TAI_CHO`).
  - `Hantranhan` (`DATETIME`, NULL): Hạn 24 giờ nhận sách đối với phiếu đặt mượn online (UC-5).
  - `Trangthai` (`INT`, NOT NULL, DEFAULT 0): Trạng thái phiếu (C2):
    * `0: CHO_DUYET` (Đã đặt online, giữ sách 24 giờ).
    * `1: DANG_MUON` (Đã xuất sách tại quầy).
    * `2: DA_TRA` (Đã hoàn trả đủ).
    * `3: QUA_HAN` (Quá hạn chưa trả).
    * `4: CHO_XU_LY_BOI_THUONG` (Đã báo mất/hỏng, chờ nộp tiền phạt BR-08, C1).
    * `5: DA_MAT` (Đã đền bù xong và đóng phiếu).
    * `6: DA_HUY` (Hủy do quá 24h không đến nhận).
  - `Ghichuphieu` (`NVARCHAR(255)`, NULL): Ghi chú."""

content = content.replace(old_phieumuon, new_phieumuon)

# Add Section 4.7
section_4_7 = """
### 4.7 Phân hệ 7: Mở Rộng Theo Đặc Tả SRS (C6: Bình luận, Góp ý, Cấu hình)
- **`binhluan`** (UC-8, F-11):
  - `id` (`INT`, PK, AUTO_INCREMENT): Mã bình luận.
  - `Masach` (`VARCHAR(50)`, FK → `sach.Masach`, NOT NULL): Đầu sách được đánh giá.
  - `id_nguoidung` (`VARCHAR(50)`, FK → `nguoidung.ID`, NOT NULL): Độc giả gửi bình luận.
  - `noidung` (`TEXT`, NOT NULL): Nội dung thảo luận.
  - `diemsao` (`INT`, NULL, DEFAULT 5): Điểm đánh giá (1 đến 5 sao).
  - `ngaytao` (`DATETIME`, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Thời gian gửi.
  - `IsDeleted` (`BOOLEAN`, NOT NULL, DEFAULT FALSE): Ẩn bình luận vi phạm.
- **`gopy`** (F-10):
  - `id` (`INT`, PK, AUTO_INCREMENT): Mã góp ý.
  - `id_nguoidung` (`VARCHAR(50)`, FK → `nguoidung.ID`, NULL): Độc giả gửi (hoặc ẩn danh).
  - `tieude` (`VARCHAR(255)`, NOT NULL): Tiêu đề góp ý.
  - `noidung` (`TEXT`, NOT NULL): Nội dung phản ánh dịch vụ.
  - `ngaytao` (`DATETIME`, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Thời gian gửi.
  - `trangthai` (`VARCHAR(50)`, NOT NULL, DEFAULT 'CHO_TIEP_NHAN'): Trạng thái xử lý.
- **`cauhinh`** (SRS §2.3, §3.3.4):
  - `khoa` (`VARCHAR(100)`, PK, NOT NULL): Tên tham số quy định hệ thống.
  - `giatri` (`VARCHAR(255)`, NOT NULL): Giá trị cấu hình hiện hành.
  - `mota` (`VARCHAR(255)`, NULL): Diễn giải ý nghĩa quy định.
"""

if "### 4.7 Phân hệ 7" not in content:
    content = content.replace("## 5. Bảng Tra cứu Trạng thái", section_4_7 + "\n## 5. Bảng Tra cứu Trạng thái")

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated docs/DATABASE_SCHEMA.md successfully.")
