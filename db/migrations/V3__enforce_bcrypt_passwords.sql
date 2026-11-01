-- ============================================================================
-- Migration V3: Chuẩn hóa toàn bộ mật khẩu người dùng sang BCrypt hash (R-08, BR-01)
-- Dự án: XimiLib (PBL3)
-- Xử lý: Cập nhật các tài khoản đang lưu mật khẩu chuỗi trần hoặc hash không hợp lệ
-- ============================================================================

-- Migration thực thi trên CSDL hiện hành của kết nối JDBC

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

-- 3. Chuẩn hóa ID tài khoản admin sang 'ND001' nếu đang mang mã cũ 'USR-ADMIN-01'
-- LƯU Ý KHI THỰC THI:
--   - Toàn bộ khối lệnh phải được thực thi trong MỘT phiên kết nối duy nhất (do dùng SET FOREIGN_KEY_CHECKS).
--   - Điều kiện an toàn tuyệt đối: Khối lệnh CHỈ chuyển đổi khi CSDL tồn tại người dùng 'USR-ADMIN-01'
--     VÀ CHƯA tồn tại người dùng 'ND001'.
--   - Trường hợp DB đã tồn tại đồng thời cả hai mã ID ('USR-ADMIN-01' và 'ND001'), script sẽ KHÔNG tự ý
--     thay đổi dữ liệu để tránh nhập nhằng/trộn lẫn dữ liệu của 2 tài khoản; quản trị viên cần đối soát thủ công.
SET FOREIGN_KEY_CHECKS = 0;

UPDATE `thethuvien` SET `ID` = 'ND001'
WHERE `ID` = 'USR-ADMIN-01'
  AND EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'USR-ADMIN-01') AS tmp_old)
  AND NOT EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'ND001') AS tmp_new);

UPDATE `phieumuon` SET `ID` = 'ND001'
WHERE `ID` = 'USR-ADMIN-01'
  AND EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'USR-ADMIN-01') AS tmp_old)
  AND NOT EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'ND001') AS tmp_new);

UPDATE `datcho` SET `ID` = 'ND001'
WHERE `ID` = 'USR-ADMIN-01'
  AND EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'USR-ADMIN-01') AS tmp_old)
  AND NOT EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'ND001') AS tmp_new);

UPDATE `phieuphat` SET `ID` = 'ND001'
WHERE `ID` = 'USR-ADMIN-01'
  AND EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'USR-ADMIN-01') AS tmp_old)
  AND NOT EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'ND001') AS tmp_new);

UPDATE `thongbao` SET `ID` = 'ND001'
WHERE `ID` = 'USR-ADMIN-01'
  AND EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'USR-ADMIN-01') AS tmp_old)
  AND NOT EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'ND001') AS tmp_new);

UPDATE `nhapsach` SET `ID` = 'ND001'
WHERE `ID` = 'USR-ADMIN-01'
  AND EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'USR-ADMIN-01') AS tmp_old)
  AND NOT EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'ND001') AS tmp_new);

UPDATE `binhluan` SET `id_nguoidung` = 'ND001'
WHERE `id_nguoidung` = 'USR-ADMIN-01'
  AND EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'USR-ADMIN-01') AS tmp_old)
  AND NOT EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'ND001') AS tmp_new);

UPDATE `gopy` SET `id_nguoidung` = 'ND001'
WHERE `id_nguoidung` = 'USR-ADMIN-01'
  AND EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'USR-ADMIN-01') AS tmp_old)
  AND NOT EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'ND001') AS tmp_new);

UPDATE `nguoidung`
SET `ID` = 'ND001'
WHERE `Tendangnhap` = 'admin' AND `ID` = 'USR-ADMIN-01'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `ID` FROM `nguoidung` WHERE `ID` = 'ND001') AS tmp_new);

SET FOREIGN_KEY_CHECKS = 1;

-- 4. Đảm bảo nạp đầy đủ các tài khoản mẫu nếu CSDL cũ chưa có
INSERT INTO `nguoidung` (`ID`, `Tendangnhap`, `Matkhau`, `Hoten`, `Email`, `Gioitinh`, `Sdt`, `Mavaitro`, `Trangthai`) VALUES
('ND001', 'admin', '$2a$10$GQBiVxZMhZXp.now2MCu3.OW8KqSUGVH5//qTuN1/Lb.zps5QMo02', 'Nguyễn Việt Nhật', 'nhat.nguyen@dut.udn.vn', 1, '0905111222', 'ADMIN', 1),
('ND002', 'kien.phan', '$2a$10$9BQggIBf5SOkBmU6Xgldruss1DCko8/sJgeUFVcWRJpJs7OxlkaHC', 'Phan Vũ Xuân Kiên', 'kien.phan@dut.udn.vn', 1, '0905123456', 'LIBRARIAN', 1),
('ND003', 'nam.le', '$2a$10$9BQggIBf5SOkBmU6Xgldruss1DCko8/sJgeUFVcWRJpJs7OxlkaHC', 'Lê Võ Thành Nam', 'nam.le@dut.udn.vn', 1, '0905333444', 'LIBRARIAN', 1),
('ND004', 'sinhvien', '$2a$10$9BQggIBf5SOkBmU6Xgldruss1DCko8/sJgeUFVcWRJpJs7OxlkaHC', 'Trần Văn An', 'an.tran@sv.dut.udn.vn', 1, '0914999888', 'READER', 1)
ON DUPLICATE KEY UPDATE `Hoten` = VALUES(`Hoten`);
