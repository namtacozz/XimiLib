-- Seed dữ liệu mẫu cho Việc cần xử lý hôm nay (quá hạn, đến hạn) và Chờ nhận sách online (FSM trạng thái 0)
-- Phù hợp với ngày hiện tại (CURDATE() và NOW())

-- Xóa dữ liệu mẫu cũ nếu có
DELETE FROM chitietphieumuon WHERE Maphieumuon IN ('PM-DEMO-01', 'PM-DEMO-02', 'PM-DEMO-03', 'PM-ONLINE-01', 'PM-ONLINE-02');
DELETE FROM phieumuon WHERE Maphieumuon IN ('PM-DEMO-01', 'PM-DEMO-02', 'PM-DEMO-03', 'PM-ONLINE-01', 'PM-ONLINE-02');

-- 1. Phiếu mượn quá hạn 2 ngày (Trạng thái 3: Quá hạn)
INSERT INTO phieumuon (Maphieumuon, ID, Mathe, Hinhthuc, Ngaymuon, Ngaytra, Trangthai, Ghichuphieu)
VALUES ('PM-DEMO-01', 'ND004', 'LIB-2026-00001', 'VE_NHA', CURDATE() - INTERVAL 16 DAY, CURDATE() - INTERVAL 2 DAY, 3, 'Mượn về nhà - Quá hạn 2 ngày');

INSERT INTO chitietphieumuon (Machitiet, Maphieumuon, Macuonsach, Tinhtrangkhimuon, Tienphat)
VALUES ('CT-DEMO-01', 'PM-DEMO-01', 'MS0001-C01', 'Mới 100%', 4000.00);

UPDATE cuonsach SET Trangthaisach = 'DANG_MUON' WHERE Macuonsach = 'MS0001-C01';

-- 2. Phiếu mượn đến hạn hôm nay (Trạng thái 1: Đang mượn - 2 cuốn)
INSERT INTO phieumuon (Maphieumuon, ID, Mathe, Hinhthuc, Ngaymuon, Ngaytra, Trangthai, Ghichuphieu)
VALUES ('PM-DEMO-02', 'ND005', 'LIB-2026-00005', 'VE_NHA', CURDATE() - INTERVAL 14 DAY, CURDATE(), 1, 'Mượn về nhà - Hạn hôm nay');

INSERT INTO chitietphieumuon (Machitiet, Maphieumuon, Macuonsach, Tinhtrangkhimuon, Tienphat)
VALUES ('CT-DEMO-02', 'PM-DEMO-02', 'MS0003-C01', 'Mới 98%', 0.00),
       ('CT-DEMO-03', 'PM-DEMO-02', 'MS0003-C02', 'Tốt', 0.00);

UPDATE cuonsach SET Trangthaisach = 'DANG_MUON' WHERE Macuonsach IN ('MS0003-C01', 'MS0003-C02');

-- 3. Phiếu mượn đến hạn ngày mai (Trạng thái 1: Đang mượn)
INSERT INTO phieumuon (Maphieumuon, ID, Mathe, Hinhthuc, Ngaymuon, Ngaytra, Trangthai, Ghichuphieu)
VALUES ('PM-DEMO-03', 'ND006', 'LIB-2026-00006', 'VE_NHA', CURDATE() - INTERVAL 13 DAY, CURDATE() + INTERVAL 1 DAY, 1, 'Mượn về nhà - Hạn ngày mai');

INSERT INTO chitietphieumuon (Machitiet, Maphieumuon, Macuonsach, Tinhtrangkhimuon, Tienphat)
VALUES ('CT-DEMO-04', 'PM-DEMO-03', 'MS0004-C01', 'Tốt', 0.00);

UPDATE cuonsach SET Trangthaisach = 'DANG_MUON' WHERE Macuonsach = 'MS0004-C01';

-- 4. Phiếu chờ nhận sách online 1 (Trạng thái 0: Chờ nhận, hạn giữ 18h tới)
INSERT INTO phieumuon (Maphieumuon, ID, Mathe, Hinhthuc, Ngaymuon, Hantranhan, Trangthai, Ghichuphieu)
VALUES ('PM-ONLINE-01', 'ND007', 'LIB-2026-00007', 'VE_NHA', CURDATE(), NOW() + INTERVAL 18 HOUR, 0, 'Đăng ký giữ sách trực tuyến');

INSERT INTO chitietphieumuon (Machitiet, Maphieumuon, Macuonsach, Tinhtrangkhimuon, Tienphat)
VALUES ('CT-ONLINE-01', 'PM-ONLINE-01', 'MS0001-C02', 'Mới', 0.00);

UPDATE cuonsach SET Trangthaisach = 'DANG_MUON' WHERE Macuonsach = 'MS0001-C02';

-- 5. Phiếu chờ nhận sách online 2 (Trạng thái 0: Chờ nhận, hạn giữ 26h tới)
INSERT INTO phieumuon (Maphieumuon, ID, Mathe, Hinhthuc, Ngaymuon, Hantranhan, Trangthai, Ghichuphieu)
VALUES ('PM-ONLINE-02', 'ND008', 'LIB-2026-00008', 'VE_NHA', CURDATE(), NOW() + INTERVAL 26 HOUR, 0, 'Đăng ký nhận tại quầy');

INSERT INTO chitietphieumuon (Machitiet, Maphieumuon, Macuonsach, Tinhtrangkhimuon, Tienphat)
VALUES ('CT-ONLINE-02', 'PM-ONLINE-02', 'MS0005-C01', 'Tốt', 0.00);

UPDATE cuonsach SET Trangthaisach = 'DANG_MUON' WHERE Macuonsach = 'MS0005-C01';
