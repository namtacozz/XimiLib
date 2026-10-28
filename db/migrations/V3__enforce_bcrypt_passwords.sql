-- ============================================================================
-- Migration V3: Chuẩn hóa toàn bộ mật khẩu người dùng sang BCrypt hash (R-08, BR-01)
-- Dự án: XimiLib (PBL3)
-- Xử lý: Cập nhật các tài khoản đang lưu mật khẩu chuỗi trần hoặc hash không hợp lệ
-- ============================================================================

USE `ximilib_db`;

-- 1. Cập nhật tài khoản admin với hash BCrypt chuẩn của 'admin123' nếu đang mang hash giả hoặc chuỗi trần
UPDATE `nguoidung`
SET `Matkhau` = '$2a$10$GQBiVxZMhZXp.now2MCu3.OW8KqSUGVH5//qTuN1/Lb.zps5QMo02'
WHERE `Tendangnhap` = 'admin'
  AND (`Matkhau` = 'admin123'
       OR `Matkhau` = '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890'
       OR LENGTH(`Matkhau`) < 60
       OR `Matkhau` NOT LIKE '$2_%$%');

-- 2. Cập nhật các tài khoản nhân viên / sinh viên mặc định (mật khẩu mặc định '123456') nếu còn chuỗi trần
UPDATE `nguoidung`
SET `Matkhau` = '$2a$10$9BQggIBf5SOkBmU6Xgldruss1DCko8/sJgeUFVcWRJpJs7OxlkaHC'
WHERE (`Matkhau` = '123456' OR LENGTH(`Matkhau`) < 60 OR `Matkhau` NOT LIKE '$2_%$%')
  AND `Tendangnhap` IN ('kien.phan', 'nam.le', 'sinhvien');
