-- ============================================================================
-- Migration V1: Khởi tạo CSDL 20 bảng chuẩn cho XimiLib
-- Dự án: XimiLib (PBL) - Chuẩn hóa UTF8MB4
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------------------
-- 1. NHÓM PHÂN QUYỀN & NGƯỜI DÙNG
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `vaitro` (
  `Mavaitro` VARCHAR(50) NOT NULL,
  `Tenvaitro` NVARCHAR(50) NOT NULL,
  `Mota` TEXT,
  PRIMARY KEY (`Mavaitro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `nguoidung` (
  `ID` VARCHAR(50) NOT NULL,
  `Tendangnhap` VARCHAR(50) NOT NULL UNIQUE,
  `Matkhau` VARCHAR(255) NOT NULL,
  `Hoten` NVARCHAR(100) NOT NULL,
  `Email` VARCHAR(100) UNIQUE,
  `Gioitinh` BOOLEAN,
  `Sdt` VARCHAR(20),
  `Mavaitro` VARCHAR(50) NOT NULL,
  `Trangthai` INT DEFAULT 1,
  PRIMARY KEY (`ID`),
  CONSTRAINT `fk_nguoidung_vaitro` FOREIGN KEY (`Mavaitro`) REFERENCES `vaitro` (`Mavaitro`) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `thethuvien` (
  `Mathe` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50) NOT NULL,
  `Ngaycap` DATE NOT NULL,
  `Ngayhethan` DATE NOT NULL,
  `Trangthai` INT DEFAULT 1,
  PRIMARY KEY (`Mathe`),
  CONSTRAINT `fk_thethuvien_nguoidung` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 2. NHÓM DANH MỤC SÁCH & TÁC GIẢ
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `theloai` (
  `Matheloai` INT NOT NULL AUTO_INCREMENT,
  `Tentheloai` NVARCHAR(100) NOT NULL,
  `Mota` NVARCHAR(255),
  PRIMARY KEY (`Matheloai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `tacgia` (
  `Matacgia` VARCHAR(50) NOT NULL,
  `Tentacgia` NVARCHAR(100) NOT NULL,
  `Mota` TEXT,
  PRIMARY KEY (`Matacgia`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `nhaxuatban` (
  `manxb` VARCHAR(50) NOT NULL,
  `tennxb` NVARCHAR(100) NOT NULL,
  `diachinxb` NVARCHAR(255),
  `sdt` VARCHAR(20),
  `mota` TEXT,
  PRIMARY KEY (`manxb`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `sach` (
  `Masach` VARCHAR(50) NOT NULL,
  `Matheloai` INT,
  `Manxb` VARCHAR(50),
  `Tensach` NVARCHAR(255) NOT NULL,
  `Isbn` VARCHAR(100),
  `Namxb` INT,
  `Sotrang` INT,
  `Giabia` DECIMAL(12,2) DEFAULT 0.00,
  `Motasach` TEXT,
  `Hinhanh` VARCHAR(255),
  `IsDeleted` BOOLEAN DEFAULT FALSE,
  PRIMARY KEY (`Masach`),
  KEY `idx_sach_tensach` (`Tensach`),
  KEY `idx_sach_isbn` (`Isbn`),
  CONSTRAINT `fk_sach_theloai` FOREIGN KEY (`Matheloai`) REFERENCES `theloai` (`Matheloai`) ON DELETE SET NULL,
  CONSTRAINT `fk_sach_nhaxuatban` FOREIGN KEY (`Manxb`) REFERENCES `nhaxuatban` (`manxb`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `sach_tacgia` (
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

CREATE TABLE IF NOT EXISTS `khuvuc` (
  `Makhuvuc` VARCHAR(50) NOT NULL,
  `Tenkhuvuc` NVARCHAR(100) NOT NULL,
  `Motakhuvuc` NVARCHAR(255),
  PRIMARY KEY (`Makhuvuc`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `kesach` (
  `Makesach` VARCHAR(50) NOT NULL,
  `Makhuvuc` VARCHAR(50) NOT NULL,
  `Tenkesach` NVARCHAR(100) NOT NULL,
  `Tang` INT DEFAULT 1,
  `Motake` NVARCHAR(255),
  PRIMARY KEY (`Makesach`),
  CONSTRAINT `fk_kesach_khuvuc` FOREIGN KEY (`Makhuvuc`) REFERENCES `khuvuc` (`Makhuvuc`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `cuonsach` (
  `Macuonsach` VARCHAR(50) NOT NULL,
  `Masach` VARCHAR(50) NOT NULL,
  `Makesach` VARCHAR(50),
  `Isbn` VARCHAR(50),
  `Ngaynhap` DATE,
  `Tinhtrangsach` NVARCHAR(50) DEFAULT 'Mới',
  `Trangthaisach` NVARCHAR(50) DEFAULT 'KHA_DUNG',
  PRIMARY KEY (`Macuonsach`),
  CONSTRAINT `fk_cuonsach_sach` FOREIGN KEY (`Masach`) REFERENCES `sach` (`Masach`) ON DELETE CASCADE,
  CONSTRAINT `fk_cuonsach_kesach` FOREIGN KEY (`Makesach`) REFERENCES `kesach` (`Makesach`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 4. NHÓM MƯỢN TRẢ, GIA HẠN & ĐẶT CHỖ
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `phieumuon` (
  `Maphieumuon` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50) NOT NULL,
  `Mathe` VARCHAR(50) NOT NULL,
  `Ngaymuon` DATE NOT NULL,
  `Ngaytra` DATE,
  `Trangthai` INT DEFAULT 1,
  `Ghichuphieu` NVARCHAR(255),
  PRIMARY KEY (`Maphieumuon`),
  CONSTRAINT `fk_phieumuon_nguoidung` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `fk_phieumuon_the` FOREIGN KEY (`Mathe`) REFERENCES `thethuvien` (`Mathe`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `chitietphieumuon` (
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

CREATE TABLE IF NOT EXISTS `giahanmuon` (
  `Magiahan` VARCHAR(50) NOT NULL,
  `Maphieumuon` VARCHAR(50) NOT NULL,
  `Ngaygiahan` DATETIME NOT NULL,
  `Hantracu` DATETIME NOT NULL,
  `Hantramoi` DATETIME NOT NULL,
  `Lydogiahan` NVARCHAR(255),
  `Trangthai` INT DEFAULT 1,
  PRIMARY KEY (`Magiahan`),
  CONSTRAINT `fk_giahan_phieumuon` FOREIGN KEY (`Maphieumuon`) REFERENCES `phieumuon` (`Maphieumuon`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `datcho` (
  `Madatcho` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50) NOT NULL,
  `Masach` VARCHAR(50) NOT NULL,
  `Ngaydat` DATETIME NOT NULL,
  `Vitri` INT DEFAULT 1,
  `Ngayhethan` DATETIME NOT NULL,
  `Trangthai` INT DEFAULT 1,
  PRIMARY KEY (`Madatcho`),
  CONSTRAINT `fk_datcho_nguoidung` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `fk_datcho_sach` FOREIGN KEY (`Masach`) REFERENCES `sach` (`Masach`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 5. NHÓM PHẠT, THANH TOÁN & THÔNG BÁO
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `phieuphat` (
  `Maphieuphat` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50) NOT NULL,
  `Machitiet` VARCHAR(50),
  `Lydophat` NVARCHAR(255) NOT NULL,
  `Sotienphat` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  `Ngayphat` DATETIME NOT NULL,
  `Trangthai` INT DEFAULT 0,
  `Ghichuphat` NVARCHAR(255),
  PRIMARY KEY (`Maphieuphat`),
  CONSTRAINT `fk_phieuphat_nguoidung` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE CASCADE,
  CONSTRAINT `fk_phieuphat_ctpm` FOREIGN KEY (`Machitiet`) REFERENCES `chitietphieumuon` (`Machitiet`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `thanhtoan` (
  `Mathanhtoan` VARCHAR(50) NOT NULL,
  `Maphieuphat` VARCHAR(50) NOT NULL,
  `Ngaythanhtoan` DATETIME NOT NULL,
  `Sotien` DECIMAL(12,2) NOT NULL,
  `Phuongthuc` NVARCHAR(50) DEFAULT 'Tiền mặt',
  `Trangthai` INT DEFAULT 1,
  PRIMARY KEY (`Mathanhtoan`),
  CONSTRAINT `fk_thanhtoan_phieuphat` FOREIGN KEY (`Maphieuphat`) REFERENCES `phieuphat` (`Maphieuphat`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `thongbao` (
  `Mathongbao` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50) NOT NULL,
  `Tieude` NVARCHAR(255) NOT NULL,
  `Noidung` TEXT NOT NULL,
  `Loaithongbao` NVARCHAR(50) DEFAULT 'Hệ thống',
  `Ngaytao` DATETIME NOT NULL,
  `Dadoc` INT DEFAULT 0,
  PRIMARY KEY (`Mathongbao`),
  CONSTRAINT `fk_thongbao_nguoidung` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- 6. NHÓM NHẬP SÁCH (KHO)
-- --------------------------------------------------------

CREATE TABLE IF NOT EXISTS `nhapsach` (
  `Manhapsach` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50) NOT NULL,
  `Ngaynhap` DATETIME NOT NULL,
  `Nhacungcap` NVARCHAR(100),
  `Tongtien` DECIMAL(14,2) DEFAULT 0.00,
  `Ghichunhap` NVARCHAR(255),
  PRIMARY KEY (`Manhapsach`),
  CONSTRAINT `fk_nhapsach_nguoidung` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `chitietnhapsach` (
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

SET FOREIGN_KEY_CHECKS = 1;

-- Khởi tạo Vai trò mặc định
INSERT INTO `vaitro` (`Mavaitro`, `Tenvaitro`, `Mota`) VALUES
('ADMIN', 'Quản trị viên', 'Toàn quyền cấu hình hệ thống, quản lý tài khoản'),
('LIBRARIAN', 'Thủ thư', 'Quản lý kho sách, lập phiếu mượn/trả và xử lý phạt'),
('READER', 'Độc giả', 'Tra cứu sách, đặt trước và xem lịch sử cá nhân')
ON DUPLICATE KEY UPDATE `Tenvaitro` = VALUES(`Tenvaitro`);
