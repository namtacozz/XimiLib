-- ============================================================================
-- Migration V5: Đồng bộ hóa toàn diện theo SRS IEEE Std 830-1998
-- Dự án: XimiLib (PBL3)
-- Phê duyệt theo Deviation Register: DEV-01, DEV-02, DEV-04, DEV-06, DEV-07
-- Nội dung:
--   1. Tạo bảng `auditlog` lưu vết kiểm toán tập trung (UC 3.2.7, DEV-04)
--   2. Bổ sung cột `magiaodich` trong bảng `thanhtoan` có UNIQUE index chống trùng VietQR
--   3. Tạo chỉ mục composite `idx_sach_search` trên `sach(Tensach, Isbn, Matheloai)` (UC 3.2.4)
--   4. Tạo chỉ mục `idx_datcho_expiring` trên `datcho(Trangthai, Ngayhethan)` (UC 3.2.5)
--   5. Bổ sung cột `updated_at` và index `idx_cuonsach_sync` trên `cuonsach` (Observer đa máy)
--   6. Khởi tạo các tham số cấu hình quy định thư viện chuẩn trong bảng `cauhinh` (DEV-06, YC-10)
-- ============================================================================

-- 1. Tạo bảng nhật ký kiểm toán (AuditLog - DEV-04)
CREATE TABLE IF NOT EXISTS `auditlog` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `user_id` VARCHAR(50) NULL,
    `hanhdong` VARCHAR(100) NOT NULL,
    `chitiet` TEXT NULL,
    `thoigian` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ip` VARCHAR(50) NULL,
    INDEX `idx_auditlog_user` (`user_id`),
    INDEX `idx_auditlog_thoigian` (`thoigian`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Thêm cột magiaodich cho bảng thanhtoan (chống trùng lặp VietQR)
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'thanhtoan' AND COLUMN_NAME = 'magiaodich'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE thanhtoan ADD COLUMN magiaodich VARCHAR(100) NULL AFTER Sotien;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Unique index cho magiaodich
SET @idx_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'thanhtoan' AND INDEX_NAME = 'uq_thanhtoan_magiaodich'
);
SET @sql := IF(@idx_exists = 0, 'ALTER TABLE thanhtoan ADD UNIQUE INDEX uq_thanhtoan_magiaodich (magiaodich);', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. Composite index tìm kiếm sách (UC 3.2.4)
SET @idx_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sach' AND INDEX_NAME = 'idx_sach_search'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_sach_search ON sach (Tensach, Isbn, Matheloai);', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. Index quét hết hạn giữ sách 24h (UC 3.2.5)
SET @idx_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'datcho' AND INDEX_NAME = 'idx_datcho_expiring'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_datcho_expiring ON datcho (Trangthai, Ngayhethan);', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. Bổ sung cột updated_at và Index đồng bộ Observer đa máy (cuonsach.updated_at)
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cuonsach' AND COLUMN_NAME = 'updated_at'
);
SET @sql := IF(@col_exists = 0, 'ALTER TABLE cuonsach ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cuonsach' AND INDEX_NAME = 'idx_cuonsach_sync'
);
SET @sql := IF(@idx_exists = 0, 'CREATE INDEX idx_cuonsach_sync ON cuonsach (updated_at);', 'SELECT 1;');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6. Khởi tạo/cập nhật tham số cấu hình quy định thư viện chuẩn SRS (bảng cauhinh - YC-10, DEV-06)
INSERT INTO `cauhinh` (`khoa`, `giatri`, `mota`) VALUES
('policy.max_borrow_books', '3', 'Số lượng sách tối đa độc giả được mượn mang về (BR-02)'),
('policy.borrow_duration_days', '14', 'Thời hạn mượn sách mang về nhà chuẩn (ngày) (BR-02)'),
('policy.loan_renewal_days', '7', 'Số ngày gia hạn mượn sách thêm mỗi lần (ngày) (BR-03)'),
('policy.max_renewal_times', '1', 'Số lần tối đa được phép gia hạn một phiếu mượn (BR-03)'),
('policy.fine_per_day_vnd', '2000', 'Mức tiền phạt quá hạn cho mỗi ngày trễ trên mỗi cuốn sách (VNĐ) (BR-06)'),
('policy.tech_fee_vnd', '20000', 'Phí xử lý kỹ thuật và biên mục khi bồi thường mất/hỏng sách (VNĐ) (BR-08)'),
('policy.compensation_rate', '2.0', 'Tỷ lệ bồi thường theo giá bìa sách khi làm mất sách (200%) (BR-08)'),
('policy.hold_reservation_hours', '24', 'Thời gian giữ sách đặt online trước khi tự động hủy (giờ) (BR-05)'),
('policy.max_login_attempts', '5', 'Số lần đăng nhập sai tối đa trước khi tạm khóa (BR-01)'),
('policy.lockout_minutes', '15', 'Thời gian tạm khóa tài khoản khi nhập sai quá số lần (phút) (BR-01)')
ON DUPLICATE KEY UPDATE `giatri` = VALUES(`giatri`), `mota` = VALUES(`mota`);
