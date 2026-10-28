-- ========================================================
-- CƠ SỞ DỮ LIỆU: XimiLib (Quản Lý Thư Viện)
-- Hệ quản trị: MySQL 8.0+
-- Thiết kế: Phan Vũ Xuân Kiên (FE/DB) & Lê Võ Thành Nam (BE)
-- Phê duyệt & Tối ưu: Nguyễn Việt Nhật (Tech Lead)
-- Ngày cập nhật: 21/10/2026
-- Mô hình: Kiến trúc 3 tầng (GUI - BLL - DAL)
-- ========================================================

CREATE DATABASE IF NOT EXISTS `ximilib_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ximilib_db`;

-- Tắt tạm thời kiểm tra khóa ngoại để khởi tạo an toàn
SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------------------
-- 1. NHÓM PHÂN QUYỀN & NGƯỜI DÙNG
-- --------------------------------------------------------

-- Bảng: vaitro (Quản lý các vai trò trong hệ thống: ADMIN, LIBRARIAN, READER)
DROP TABLE IF EXISTS `vaitro`;
CREATE TABLE `vaitro` (
  `Mavaitro` VARCHAR(50) NOT NULL,
  `Tenvaitro` NVARCHAR(50) NOT NULL,
  `Mota` TEXT,
  PRIMARY KEY (`Mavaitro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: nguoidung (Tài khoản và thông tin người dùng trong thư viện)
DROP TABLE IF EXISTS `nguoidung`;
CREATE TABLE `nguoidung` (
  `ID` VARCHAR(50) NOT NULL,
  `Tendangnhap` VARCHAR(50) NOT NULL UNIQUE,
  `Matkhau` VARCHAR(255) NOT NULL, -- Độ dài 255 để lưu hash BCrypt/Argon2
  `Hoten` NVARCHAR(100) NOT NULL,
  `Email` VARCHAR(100) UNIQUE,
  `Gioitinh` BOOLEAN,
  `Sdt` VARCHAR(20),
  `Mavaitro` VARCHAR(50) NOT NULL,
  `Trangthai` INT DEFAULT 1, -- 1: Hoạt động, 0: Khóa
  PRIMARY KEY (`ID`),
  CONSTRAINT `fk_nguoidung_vaitro` FOREIGN KEY (`Mavaitro`) REFERENCES `vaitro` (`Mavaitro`) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: thethuvien (Quản lý thẻ thư viện của độc giả)
DROP TABLE IF EXISTS `thethuvien`;
CREATE TABLE `thethuvien` (
  `Mathe` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50) NOT NULL,
  `Ngaycap` DATE NOT NULL,
  `Ngayhethan` DATE NOT NULL,
  `Trangthai` INT DEFAULT 1, -- 1: Còn hạn, 0: Hết hạn / Khóa
  PRIMARY KEY (`Mathe`),
  CONSTRAINT `fk_thethuvien_nguoidung` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 2. NHÓM DANH MỤC SÁCH & TÁC GIẢ
-- --------------------------------------------------------

-- Bảng: theloai (Thể loại sách: Công nghệ, Văn học, Kinh tế,...)
DROP TABLE IF EXISTS `theloai`;
CREATE TABLE `theloai` (
  `Matheloai` INT NOT NULL AUTO_INCREMENT,
  `Tentheloai` NVARCHAR(100) NOT NULL,
  `Mota` NVARCHAR(255),
  PRIMARY KEY (`Matheloai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: tacgia (Thông tin tác giả)
DROP TABLE IF EXISTS `tacgia`;
CREATE TABLE `tacgia` (
  `Matacgia` VARCHAR(50) NOT NULL,
  `Tentacgia` NVARCHAR(100) NOT NULL,
  `Mota` TEXT,
  PRIMARY KEY (`Matacgia`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: nhaxuatban (Thông tin Nhà xuất bản)
DROP TABLE IF EXISTS `nhaxuatban`;
CREATE TABLE `nhaxuatban` (
  `manxb` VARCHAR(50) NOT NULL,
  `tennxb` NVARCHAR(100) NOT NULL,
  `diachinxb` NVARCHAR(255),
  `sdt` VARCHAR(20),
  `mota` TEXT,
  PRIMARY KEY (`manxb`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: sach (Thông tin đầu sách chung)
DROP TABLE IF EXISTS `sach`;
CREATE TABLE `sach` (
  `Masach` VARCHAR(50) NOT NULL,
  `Matheloai` INT,
  `Manxb` VARCHAR(50),
  `Tensach` NVARCHAR(255) NOT NULL,
  `Isbn` VARCHAR(100),
  `Namxb` INT,
  `Sotrang` INT,
  `Giabia` DECIMAL(12,2) DEFAULT 0.00,
  `Motasach` TEXT,
  `Hinhanh` VARCHAR(255), -- Đường dẫn tương đối của ảnh bìa trong thư mục tài nguyên
  `IsDeleted` BOOLEAN DEFAULT FALSE, -- Cơ chế xóa mềm (Soft-delete)
  PRIMARY KEY (`Masach`),
  KEY `idx_sach_tensach` (`Tensach`),
  KEY `idx_sach_isbn` (`Isbn`),
  CONSTRAINT `fk_sach_theloai` FOREIGN KEY (`Matheloai`) REFERENCES `theloai` (`Matheloai`) ON DELETE SET NULL,
  CONSTRAINT `fk_sach_nhaxuatban` FOREIGN KEY (`Manxb`) REFERENCES `nhaxuatban` (`manxb`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng trung gian: sach_tacgia (Quan hệ N - N giữa sách và tác giả)
DROP TABLE IF EXISTS `sach_tacgia`;
CREATE TABLE `sach_tacgia` (
  `Masach` VARCHAR(50) NOT NULL,
  `Matacgia` VARCHAR(50) NOT NULL,
  `Vaitrotacgia` NVARCHAR(50) DEFAULT 'Tác giả chính',
  PRIMARY KEY (`Masach`, `Matacgia`),
  CONSTRAINT `fk_st_sach` FOREIGN KEY (`Masach`) REFERENCES `sach` (`Masach`) ON DELETE CASCADE,
  CONSTRAINT `fk_st_tacgia` FOREIGN KEY (`Matacgia`) REFERENCES `tacgia` (`Matacgia`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 3. NHÓM KHO SÁCH & VỊ TRÍ KỆ SÁCH
-- --------------------------------------------------------

-- Bảng: khuvuc (Khu vực đọc sách: Tầng 1, Khu Tự nhiên, Khu Xã hội,...)
DROP TABLE IF EXISTS `khuvuc`;
CREATE TABLE `khuvuc` (
  `Makhuvuc` VARCHAR(50) NOT NULL,
  `Tenkhuvuc` NVARCHAR(100) NOT NULL,
  `Motakhuvuc` NVARCHAR(255),
  PRIMARY KEY (`Makhuvuc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: kesach (Kệ sách thuộc từng khu vực)
DROP TABLE IF EXISTS `kesach`;
CREATE TABLE `kesach` (
  `Makesach` VARCHAR(50) NOT NULL,
  `Makhuvuc` VARCHAR(50) NOT NULL,
  `Tenkesach` NVARCHAR(100) NOT NULL,
  `Tang` INT DEFAULT 1,
  `Motake` NVARCHAR(255),
  PRIMARY KEY (`Makesach`),
  CONSTRAINT `fk_kesach_khuvuc` FOREIGN KEY (`Makhuvuc`) REFERENCES `khuvuc` (`Makhuvuc`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: cuonsach (Bản sao vật lý cụ thể của một đầu sách)
DROP TABLE IF EXISTS `cuonsach`;
CREATE TABLE `cuonsach` (
  `Macuonsach` VARCHAR(50) NOT NULL,
  `Masach` VARCHAR(50) NOT NULL,
  `Makesach` VARCHAR(50),
  `Isbn` VARCHAR(50),
  `Ngaynhap` DATE,
  `Tinhtrangsach` NVARCHAR(50) DEFAULT 'Mới',
  `Trangthaisach` NVARCHAR(50) DEFAULT 'KHA_DUNG', -- KHA_DUNG, DANG_MUON, DANG_DAT_TRUOC, HONG_MAT
  PRIMARY KEY (`Macuonsach`),
  CONSTRAINT `fk_cuonsach_sach` FOREIGN KEY (`Masach`) REFERENCES `sach` (`Masach`) ON DELETE CASCADE,
  CONSTRAINT `fk_cuonsach_kesach` FOREIGN KEY (`Makesach`) REFERENCES `kesach` (`Makesach`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 4. NHÓM MƯỢN TRẢ, GIA HẠN & ĐẶT CHỖ
-- --------------------------------------------------------

-- Bảng: phieumuon (Phiếu mượn sách của độc giả)
DROP TABLE IF EXISTS `phieumuon`;
CREATE TABLE `phieumuon` (
  `Maphieumuon` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50) NOT NULL,
  `Mathe` VARCHAR(50) NOT NULL,
  `Ngaymuon` DATE NOT NULL,
  `Ngaytra` DATE,
  `Trangthai` INT DEFAULT 1, -- 1: Đang mượn, 2: Đã trả, 3: Quá hạn
  `Ghichuphieu` NVARCHAR(255),
  PRIMARY KEY (`Maphieumuon`),
  CONSTRAINT `fk_phieumuon_nguoidung` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `fk_phieumuon_the` FOREIGN KEY (`Mathe`) REFERENCES `thethuvien` (`Mathe`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: chitietphieumuon (Chi tiết từng cuốn sách trong phiếu mượn)
DROP TABLE IF EXISTS `chitietphieumuon`;
CREATE TABLE `chitietphieumuon` (
  `Machitiet` VARCHAR(50) NOT NULL,
  `Maphieumuon` VARCHAR(50) NOT NULL,
  `Macuonsach` VARCHAR(50) NOT NULL,
  `Ngaytra` DATETIME,
  `Tinhtrangkhimuon` NVARCHAR(100),
  `Tinhtrangkhitra` NVARCHAR(100),
  `Tienphat` DECIMAL(12,2) DEFAULT 0.00,
  PRIMARY KEY (`Machitiet`),
  CONSTRAINT `fk_ctpm_phieumuon` FOREIGN KEY (`Maphieumuon`) REFERENCES `phieumuon` (`Maphieumuon`) ON DELETE CASCADE,
  CONSTRAINT `fk_ctpm_cuonsach` FOREIGN KEY (`Macuonsach`) REFERENCES `cuonsach` (`Macuonsach`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: giahanmuon (Lịch sử yêu cầu gia hạn mượn sách)
DROP TABLE IF EXISTS `giahanmuon`;
CREATE TABLE `giahanmuon` (
  `Magiahan` VARCHAR(50) NOT NULL,
  `Maphieumuon` VARCHAR(50) NOT NULL,
  `Ngaygiahan` DATETIME NOT NULL,
  `Hantracu` DATETIME NOT NULL,
  `Hantramoi` DATETIME NOT NULL,
  `Lydogiahan` NVARCHAR(255),
  `Trangthai` INT DEFAULT 1, -- 1: Chấp thuận, 0: Từ chối
  PRIMARY KEY (`Magiahan`),
  CONSTRAINT `fk_giahan_phieumuon` FOREIGN KEY (`Maphieumuon`) REFERENCES `phieumuon` (`Maphieumuon`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: datcho (Độc giả đặt trước đầu sách trên ứng dụng)
DROP TABLE IF EXISTS `datcho`;
CREATE TABLE `datcho` (
  `Madatcho` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50) NOT NULL,
  `Masach` VARCHAR(50) NOT NULL,
  `Ngaydat` DATETIME NOT NULL,
  `Vitri` INT DEFAULT 1, -- Thứ tự hàng đợi đặt
  `Ngayhethan` DATETIME NOT NULL, -- Hết hạn sau 24h giữ sách
  `Trangthai` INT DEFAULT 1, -- 1: Đang chờ, 2: Đã nhận, 0: Đã hủy
  PRIMARY KEY (`Madatcho`),
  CONSTRAINT `fk_datcho_nguoidung` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `fk_datcho_sach` FOREIGN KEY (`Masach`) REFERENCES `sach` (`Masach`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 5. NHÓM PHẠT, THANH TOÁN & THÔNG BÁO
-- --------------------------------------------------------

-- Bảng: phieuphat (Khoản phạt trễ hạn hoặc đền bù mất/hỏng sách)
DROP TABLE IF EXISTS `phieuphat`;
CREATE TABLE `phieuphat` (
  `Maphieuphat` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50) NOT NULL,
  `Machitiet` VARCHAR(50),
  `Lydophat` NVARCHAR(255) NOT NULL,
  `Sotienphat` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `Ngayphat` DATETIME NOT NULL,
  `Trangthai` INT DEFAULT 0, -- 0: Chưa nộp, 1: Đã thanh toán
  `Ghichuphat` NVARCHAR(255),
  PRIMARY KEY (`Maphieuphat`),
  CONSTRAINT `fk_phieuphat_nguoidung` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `fk_phieuphat_ctpm` FOREIGN KEY (`Machitiet`) REFERENCES `chitietphieumuon` (`Machitiet`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: thanhtoan (Lịch sử thanh toán tiền phạt)
DROP TABLE IF EXISTS `thanhtoan`;
CREATE TABLE `thanhtoan` (
  `Mathanhtoan` VARCHAR(50) NOT NULL,
  `Maphieuphat` VARCHAR(50) NOT NULL,
  `Ngaythanhtoan` DATETIME NOT NULL,
  `Sotien` DECIMAL(12,2) NOT NULL,
  `Phuongthuc` NVARCHAR(50) DEFAULT 'Tiền mặt', -- Tiền mặt / Chuyển khoản VietQR
  `Trangthai` INT DEFAULT 1,
  PRIMARY KEY (`Mathanhtoan`),
  CONSTRAINT `fk_thanhtoan_phieuphat` FOREIGN KEY (`Maphieuphat`) REFERENCES `phieuphat` (`Maphieuphat`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: thongbao (Hệ thống thông báo cho người dùng)
DROP TABLE IF EXISTS `thongbao`;
CREATE TABLE `thongbao` (
  `Mathongbao` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50) NOT NULL,
  `Tieude` NVARCHAR(255) NOT NULL,
  `Noidung` TEXT NOT NULL,
  `Loaithongbao` NVARCHAR(50) DEFAULT 'Hệ thống', -- Nhắc hạn, Quá hạn, Phạt, Hệ thống
  `Ngaytao` DATETIME NOT NULL,
  `Dadoc` INT DEFAULT 0, -- 0: Chưa đọc, 1: Đã đọc
  PRIMARY KEY (`Mathongbao`),
  CONSTRAINT `fk_thongbao_nguoidung` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 6. NHÓM NHẬP SÁCH (KHO)
-- --------------------------------------------------------

-- Bảng: nhapsach (Phiếu nhập lô sách từ Nhà cung cấp)
DROP TABLE IF EXISTS `nhapsach`;
CREATE TABLE `nhapsach` (
  `Manhapsach` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50) NOT NULL, -- Thủ thư / Admin thực hiện nhập
  `Ngaynhap` DATETIME NOT NULL,
  `Nhacungcap` NVARCHAR(100),
  `Tongtien` DECIMAL(14,2) DEFAULT 0.00,
  `Ghichunhap` NVARCHAR(255),
  PRIMARY KEY (`Manhapsach`),
  CONSTRAINT `fk_nhapsach_nguoidung` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: chitietnhapsach (Chi tiết các đầu sách trong đợt nhập)
DROP TABLE IF EXISTS `chitietnhapsach`;
CREATE TABLE `chitietnhapsach` (
  `Machitietnhap` VARCHAR(50) NOT NULL,
  `Manhapsach` VARCHAR(50) NOT NULL,
  `Masach` VARCHAR(50) NOT NULL,
  `Soluong` INT NOT NULL DEFAULT 1,
  `Dongia` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `Thanhtien` DECIMAL(14,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (`Machitietnhap`),
  CONSTRAINT `fk_ctns_nhapsach` FOREIGN KEY (`Manhapsach`) REFERENCES `nhapsach` (`Manhapsach`) ON DELETE CASCADE,
  CONSTRAINT `fk_ctns_sach` FOREIGN KEY (`Masach`) REFERENCES `sach` (`Masach`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bật lại kiểm tra khóa ngoại sau khi khởi tạo xong
SET FOREIGN_KEY_CHECKS = 1;

-- ========================================================
-- DỮ LIỆU KHỞI TẠO CƠ BẢN (INITIAL SEED DATA)
-- ========================================================

-- Khởi tạo Vai trò
INSERT INTO `vaitro` (`Mavaitro`, `Tenvaitro`, `Mota`) VALUES
('ADMIN', 'Quản trị viên', 'Toàn quyền cấu hình hệ thống, quản lý tài khoản'),
('LIBRARIAN', 'Thủ thư', 'Quản lý kho sách, lập phiếu mượn/trả và xử lý phạt'),
('READER', 'Độc giả', 'Tra cứu sách, đặt trước và xem lịch sử cá nhân')
ON DUPLICATE KEY UPDATE `Tenvaitro` = VALUES(`Tenvaitro`);

-- Khởi tạo Tài khoản Quản trị viên mặc định (Mật khẩu mặc định: admin123, đã băm BCrypt)
INSERT INTO `nguoidung` (`ID`, `Tendangnhap`, `Matkhau`, `Hoten`, `Email`, `Gioitinh`, `Sdt`, `Mavaitro`, `Trangthai`) VALUES
('USR-ADMIN-01', 'admin', '$2a$10$GQBiVxZMhZXp.now2MCu3.OW8KqSUGVH5//qTuN1/Lb.zps5QMo02', 'Quản trị viên Hệ thống', 'admin@ximilib.edu.vn', 1, '0905123456', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE `Hoten` = VALUES(`Hoten`);

-- Khởi tạo một số Thể loại mẫu
INSERT INTO `theloai` (`Matheloai`, `Tentheloai`, `Mota`) VALUES
(1, 'Công nghệ thông tin', 'Sách lập trình, thuật toán, cơ sở dữ liệu'),
(2, 'Khoa học máy tính', 'Kiến trúc máy tính, mạng, hệ điều hành'),
(3, 'Văn học & Nghệ thuật', 'Tiểu thuyết, truyện ngắn, văn học cổ điển'),
(4, 'Kinh tế & Quản trị', 'Quản trị kinh doanh, tài chính, marketing')
ON DUPLICATE KEY UPDATE `Tentheloai` = VALUES(`Tentheloai`);
