-- ========================================================
-- CƠ SỞ DỮ LIỆU: XimiLib (Quản Lý Thư Viện)
-- Hệ quản trị: MySQL 8.0
-- Thiết kế: Phan Vũ Xuân Kiên (FE/DB) & Lê Võ Thành Nam (BE)
-- Ngày khởi tạo: 21/10/2026
-- Nguồn xuất: Xuất tự động từ pbl_workspace.html (Bản gốc chưa chuẩn hóa FK)
-- ========================================================

CREATE DATABASE IF NOT EXISTS `ximilib_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ximilib_db`;

-- Bảng: nguoidung (Quản lý tài khoản và thông tin người dùng trong thư viện)
CREATE TABLE IF NOT EXISTS `nguoidung` (
  `ID` VARCHAR(50) NOT NULL,
  `Tendangnhap` VARCHAR(50),
  `Matkhau` VARCHAR(50),
  `Hoten` NVARCHAR(50),
  `Email` VARCHAR(50),
  `Gioitinh` BOOLEAN,
  `Sdt` VARCHAR(50),
  `Mavaitro` VARCHAR(100),
  `Trangthai` INT,
  PRIMARY KEY (`ID`),
  CONSTRAINT `fk_nguoidung_ID_1` FOREIGN KEY (`ID`) REFERENCES `vaitro` (`Mavaitro`) ON DELETE CASCADE,
  CONSTRAINT `fk_nguoidung_ID_2` FOREIGN KEY (`ID`) REFERENCES `thethuvien` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: vaitro (Quản lý vai trò của người dùng trong thư viện)
CREATE TABLE IF NOT EXISTS `vaitro` (
  `Mavaitro` VARCHAR(50) NOT NULL,
  `Tenvaitro` VARCHAR(50),
  `Mota` TEXT,
  PRIMARY KEY (`Mavaitro`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: theloai (Quản lý thể loại của sách)
CREATE TABLE IF NOT EXISTS `theloai` (
  `Matheloai` INT NOT NULL AUTO_INCREMENT,
  `Tentheloai` VARCHAR(50),
  `Mota` VARCHAR(50),
  PRIMARY KEY (`Matheloai`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: tacgia (Mô tả thông tin tác giả)
CREATE TABLE IF NOT EXISTS `tacgia` (
  `Matacgia` VARCHAR(50) NOT NULL,
  `Tentacgia` VARCHAR(50),
  `Mota` TEXT,
  PRIMARY KEY (`Matacgia`),
  CONSTRAINT `fk_tacgia_matacgia_1` FOREIGN KEY (`matacgia`) REFERENCES `sach_tacgia` (`Matacgia`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: nhaxuatban (Quản lý thông tin các nhà xuất bản có sách trong thư viện)
CREATE TABLE IF NOT EXISTS `nhaxuatban` (
  `manxb` VARCHAR(50) NOT NULL,
  `tennxb` VARCHAR(50),
  `diachinxb` VARCHAR(50),
  `sdt` VARCHAR(50),
  `mota` TEXT,
  PRIMARY KEY (`manxb`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: sach (Quản lý thông tin và số lượng sách)
CREATE TABLE IF NOT EXISTS `sach` (
  `Masach` VARCHAR(50) NOT NULL,
  `Matheloai` VARCHAR(50),
  `Manxb` VARCHAR(50),
  `Tensach` NVARCHAR(255),
  `Isbn` VARCHAR(100),
  `Namxb` BIGINT,
  `Sotrang` INT,
  `Giabia` DECIMAL(10,2),
  `Motasach` NVARCHAR(1000),
  PRIMARY KEY (`Masach`),
  CONSTRAINT `fk_sach_Masach_1` FOREIGN KEY (`Masach`) REFERENCES `sach_tacgia` (`Masach`) ON DELETE CASCADE,
  CONSTRAINT `fk_sach_Matheloai_2` FOREIGN KEY (`Matheloai`) REFERENCES `theloai` (`Matheloai`) ON DELETE CASCADE,
  CONSTRAINT `fk_sach_Manxb_3` FOREIGN KEY (`Manxb`) REFERENCES `nhaxuatban` (`manxb`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: thethuvien (Quản lý thẻ thư viện của độc giả)
CREATE TABLE IF NOT EXISTS `thethuvien` (
  `Mathe` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50),
  `Ngaycap` DATE,
  `Ngayhethan` DATE,
  `Trangthai` INT,
  PRIMARY KEY (`Mathe`),
  CONSTRAINT `fk_thethuvien_ID_1` FOREIGN KEY (`ID`) REFERENCES `datcho` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: sach_tacgia (Bảng trung gian giữa sách và tác giả)
CREATE TABLE IF NOT EXISTS `sach_tacgia` (
  `Masach` VARCHAR(50) NOT NULL,
  `Matacgia` VARCHAR(100) NOT NULL,
  `Vaitrotacgia` NVARCHAR(50),
  PRIMARY KEY (`Masach`, `Matacgia`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: khuvuc (Các khu vực đọc sách(Khu Văn hóa,Khu Thiên văn,...))
CREATE TABLE IF NOT EXISTS `khuvuc` (
  `Makhuvuc` VARCHAR(50) NOT NULL,
  `Tenkhuvuc` NVARCHAR(50),
  `Motakhuvuc` NVARCHAR(50),
  PRIMARY KEY (`Makhuvuc`),
  CONSTRAINT `fk_khuvuc_Makhuvuc_1` FOREIGN KEY (`Makhuvuc`) REFERENCES `kesach` (`Makhuvuc`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: kesach (Quản lý các kệ sách thuộc từng khu vực)
CREATE TABLE IF NOT EXISTS `kesach` (
  `Makesach` VARCHAR(50) NOT NULL,
  `Makhuvuc` VARCHAR(50),
  `Tenkesach` NVARCHAR(50),
  `Tang` INT,
  `Motake` NVARCHAR(255),
  PRIMARY KEY (`Makesach`),
  CONSTRAINT `fk_kesach_Makesach_1` FOREIGN KEY (`Makesach`) REFERENCES `cuonsach` (`Makesach`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: cuonsach ()
CREATE TABLE IF NOT EXISTS `cuonsach` (
  `Macuonsach` VARCHAR(50) NOT NULL,
  `Masach` VARCHAR(50),
  `Makesach` VARCHAR(50),
  `Isbn` VARCHAR(50),
  `Ngaynhap` DATE,
  `Tinhtrangsach` VARCHAR(50),
  `Trangthaisach` VARCHAR(50),
  PRIMARY KEY (`Macuonsach`),
  CONSTRAINT `fk_cuonsach_Macuonsach_1` FOREIGN KEY (`Macuonsach`) REFERENCES `chitietphieumuon` (`Macuonsach`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: phieumuon ()
CREATE TABLE IF NOT EXISTS `phieumuon` (
  `Maphieumuon` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50),
  `Mathe` VARCHAR(50),
  `Ngaymuon` DATE,
  `Ngaytra` DATE,
  `Trangthai` INT,
  `Ghichuphieu` NVARCHAR(255),
  PRIMARY KEY (`Maphieumuon`),
  CONSTRAINT `fk_phieumuon_Maphieumuon_1` FOREIGN KEY (`Maphieumuon`) REFERENCES `chitietphieumuon` (`Maphieumuon`) ON DELETE CASCADE,
  CONSTRAINT `fk_phieumuon_ID_2` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: chitietphieumuon ()
CREATE TABLE IF NOT EXISTS `chitietphieumuon` (
  `Machitiet` VARCHAR(50) NOT NULL,
  `Maphieumuon` VARCHAR(50),
  `Macuonsach` VARCHAR(50),
  `Ngaytra` DATETIME,
  `Tinhtrangkhimuon` NVARCHAR(50),
  `Tinhtrangkhitra` NVARCHAR(50),
  `Tienphat` DECIMAL(10,2),
  PRIMARY KEY (`Machitiet`),
  CONSTRAINT `fk_chitietphieumuon_Maphieumuon_1` FOREIGN KEY (`Maphieumuon`) REFERENCES `giahanmuon` (`Maphieumuon`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: giahanmuon (Quản lý lịch sử gia hạn thời gian mượn)
CREATE TABLE IF NOT EXISTS `giahanmuon` (
  `Magiahan` VARCHAR(50) NOT NULL,
  `Maphieumuon` VARCHAR(50),
  `Ngaygiahan` DATETIME,
  `Hantracu` DATETIME,
  `Hantramoi` DATETIME,
  `Lydogiahan` NVARCHAR(255),
  `Trangthai` INT,
  PRIMARY KEY (`Magiahan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: datcho (Quản lý việc người dùng đặt trước một đầu sách)
CREATE TABLE IF NOT EXISTS `datcho` (
  `Madatcho` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50),
  `Masach` VARCHAR(50),
  `Ngaydat` DATETIME,
  `Vitri` INT,
  `Ngayhethan` DATETIME,
  `Trangthai` INT,
  PRIMARY KEY (`Madatcho`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: thongbao (Quản lý các thông báo gửi đến người dùng)
CREATE TABLE IF NOT EXISTS `thongbao` (
  `Mathongbao` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50),
  `Tieude` NVARCHAR(255),
  `Noidung` NVARCHAR(255),
  `Loaithongbao` NVARCHAR(50),
  `Ngaytao` DATETIME,
  `Dadoc` INT,
  PRIMARY KEY (`Mathongbao`),
  CONSTRAINT `fk_thongbao_ID_1` FOREIGN KEY (`ID`) REFERENCES `nguoidung` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: phieuphat (Quản lý các khoản phạt phát sinh do vi phạm quy định thư viện)
CREATE TABLE IF NOT EXISTS `phieuphat` (
  `Maphieuphat` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50),
  `Machitiet` VARCHAR(50),
  `Lydophat` NVARCHAR(255),
  `Sotienphat` DECIMAL(10,2),
  `Ngayphat` DATETIME,
  `Trangthai` INT,
  `Ghichuphat` NVARCHAR(255),
  PRIMARY KEY (`Maphieuphat`),
  CONSTRAINT `fk_phieuphat_Machitiet_1` FOREIGN KEY (`Machitiet`) REFERENCES `chitietphieumuon` (`Machitiet`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: thanhtoan (Quản lý việc thanh toán các khoản phạt)
CREATE TABLE IF NOT EXISTS `thanhtoan` (
  `Mathanhtoan` VARCHAR(50) NOT NULL,
  `Maphieuphat` VARCHAR(50),
  `Ngaythanhtoan` DATETIME,
  `Sotien` DECIMAL(10,2),
  `Phuongthuc` NVARCHAR(50),
  `Trangthai` INT,
  PRIMARY KEY (`Mathanhtoan`),
  CONSTRAINT `fk_thanhtoan_Maphieuphat_1` FOREIGN KEY (`Maphieuphat`) REFERENCES `phieuphat` (`Maphieuphat`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: nhapsach (Quản lý thông tin chung của một lần nhập sách vào thư viện)
CREATE TABLE IF NOT EXISTS `nhapsach` (
  `Manhapsach` VARCHAR(50) NOT NULL,
  `ID` VARCHAR(50),
  `Ngaynhap` DATETIME,
  `Nhacungcap` NVARCHAR(100),
  `Tongtien` DECIMAL(10,2),
  `Ghichunhap` NVARCHAR(100),
  PRIMARY KEY (`Manhapsach`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng: chitietnhapsach (Lưu chi tiết từng đầu sách được nhập trong một phiếu nhập)
CREATE TABLE IF NOT EXISTS `chitietnhapsach` (
  `Machitietnhap` VARCHAR(50) NOT NULL,
  `Manhapsach` VARCHAR(50),
  `Masach` VARCHAR(50),
  `Soluong` INT,
  `Dongia` DECIMAL(10,2),
  `Thanhtien` DECIMAL(10,2),
  PRIMARY KEY (`Machitietnhap`),
  CONSTRAINT `fk_chitietnhapsach_Manhapsach_1` FOREIGN KEY (`Manhapsach`) REFERENCES `nhapsach` (`Manhapsach`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
