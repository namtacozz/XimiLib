-- ============================================================================
-- Migration V2: Cập nhật CSDL theo Đặc tả SRS và xử lý mâu thuẫn C1 - C14
-- Dự án: XimiLib (PBL) - Chuẩn hóa UTF8MB4
-- ============================================================================

USE `ximilib_db`;

-- 1. Cập nhật bảng `nguoidung` (C4, C5, C13)
-- Thêm thông tin định danh độc giả (Ngày sinh, Địa chỉ, CCCD mã hóa AES-256),
-- cột kiểm soát số lần đăng nhập sai và thời gian khóa (BR-01).
ALTER TABLE `nguoidung`
    ADD COLUMN `Ngaysinh` DATE NULL AFTER `Gioitinh`,
    ADD COLUMN `Diachi` VARCHAR(255) NULL AFTER `Ngaysinh`,
    ADD COLUMN `Cccd` VARCHAR(255) NULL AFTER `Diachi`,
    ADD COLUMN `solansai` INT NOT NULL DEFAULT 0 AFTER `Trangthai`,
    ADD COLUMN `khoaden` DATETIME NULL AFTER `solansai`,
    ADD UNIQUE KEY `uq_nguoidung_sdt` (`Sdt`),
    ADD KEY `idx_nguoidung_cccd` (`Cccd`);

-- 2. Cập nhật bảng `phieumuon` (C2, C3)
-- Bổ sung hình thức mượn (Đọc tại chỗ / Mượn về nhà) và hạn giữ 24 giờ.
-- Mở rộng trạng thái: 0: CHO_DUYET, 1: DANG_MUON, 2: DA_TRA, 3: QUA_HAN,
--                     4: CHO_XU_LY_BOI_THUONG, 5: DA_MAT, 6: DA_HUY.
ALTER TABLE `phieumuon`
    ADD COLUMN `Hinhthuc` ENUM('TAI_CHO', 'VE_NHA') NOT NULL DEFAULT 'VE_NHA' AFTER `Mathe`,
    ADD COLUMN `Hantranhan` DATETIME NULL AFTER `Ngaytra`,
    MODIFY COLUMN `Trangthai` INT NOT NULL DEFAULT 0;

-- 3. Bổ sung bảng `binhluan` (C6, UC-8, F-11: Bình luận và đánh giá sao)
CREATE TABLE IF NOT EXISTS `binhluan` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `Masach` VARCHAR(50) NOT NULL,
    `id_nguoidung` VARCHAR(50) NOT NULL,
    `noidung` TEXT NOT NULL,
    `diemsao` INT NULL DEFAULT 5,
    `ngaytao` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `IsDeleted` BOOLEAN NOT NULL DEFAULT FALSE,
    KEY `idx_bl_masach` (`Masach`),
    KEY `idx_bl_nguoidung` (`id_nguoidung`),
    CONSTRAINT `fk_bl_sach` FOREIGN KEY (`Masach`) REFERENCES `sach` (`Masach`) ON DELETE CASCADE,
    CONSTRAINT `fk_bl_nguoidung` FOREIGN KEY (`id_nguoidung`) REFERENCES `nguoidung` (`ID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bổ sung bảng `gopy` (C6, F-10: Góp ý và phản hồi thư viện)
CREATE TABLE IF NOT EXISTS `gopy` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `id_nguoidung` VARCHAR(50) NULL,
    `tieude` VARCHAR(255) NOT NULL,
    `noidung` TEXT NOT NULL,
    `ngaytao` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `trangthai` VARCHAR(50) NOT NULL DEFAULT 'CHO_TIEP_NHAN',
    KEY `idx_gopy_nguoidung` (`id_nguoidung`),
    CONSTRAINT `fk_gopy_nguoidung` FOREIGN KEY (`id_nguoidung`) REFERENCES `nguoidung` (`ID`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bổ sung bảng `cauhinh` (C6: Quy định và tham số hệ thống động)
CREATE TABLE IF NOT EXISTS `cauhinh` (
    `khoa` VARCHAR(100) NOT NULL PRIMARY KEY,
    `giatri` VARCHAR(255) NOT NULL,
    `mota` VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Nạp các quy định mặc định theo bản đặc tả SRS
INSERT INTO `cauhinh` (`khoa`, `giatri`, `mota`) VALUES
('MAX_BOOKS_BORROW', '3', 'Số lượng sách mang về tối đa độc giả được mượn cùng lúc (SRS UC-5)'),
('LOAN_DAYS_HOME', '14', 'Thời hạn mượn sách mang về nhà tối đa tính bằng ngày (BR-02)'),
('DUE_TIME_DESK', '17:00', 'Giờ hết hạn trả sách trong ngày đối với mượn đọc tại chỗ (BR-04)'),
('RENEW_DAYS', '7', 'Số ngày gia hạn thêm cho mỗi lần (BR-03)'),
('MAX_RENEW_COUNT', '1', 'Số lần gia hạn tối đa cho mỗi phiếu mượn (BR-03)'),
('FINE_PER_DAY', '2000', 'Mức phạt trễ hạn mỗi ngày trên mỗi cuốn sách tính bằng VNĐ (BR-06)'),
('TECHNICAL_FEE', '20000', 'Phí xử lý kỹ thuật/hư hại nhẹ tính bằng VNĐ (BR-08)'),
('COMPENSATION_RATE', '2.0', 'Tỷ lệ bồi thường mất sách so với giá bìa: 200% (BR-08)'),
('HOLD_HOURS', '24', 'Thời gian tối đa giữ sách đã đặt mượn tính bằng giờ (SRS UC-5)'),
('CARD_VALIDITY_DAYS', '365', 'Thời hạn mặc định của thẻ thư viện tính bằng ngày khi đăng ký'),
('SEPAY_ACCOUNT_NO', '0905123456', 'Số tài khoản ngân hàng nhận chuyển khoản VietQR'),
('SEPAY_BANK', 'MBBank', 'Tên ngân hàng thụ hưởng VietQR')
ON DUPLICATE KEY UPDATE `giatri` = VALUES(`giatri`);
