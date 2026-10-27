-- Nạp dữ liệu khu vực, kệ sách và bản sao vật lý mẫu cho XimiLib
INSERT IGNORE INTO khuvuc (Makhuvuc, Tenkhuvuc, Motakhuvuc) VALUES
('KV-A', 'Khu A - Khoa học Tự nhiên & Kỹ thuật', 'Tầng 1-2 Tòa thư viện'),
('KV-B', 'Khu B - Khoa học Xã hội & Kinh tế', 'Tầng 2 Tòa thư viện'),
('KV-C', 'Khu C - Công nghệ Thông tin & Ngoại ngữ', 'Tầng 3 Tòa thư viện');

INSERT IGNORE INTO kesach (Makesach, Makhuvuc, Tenkesach, Tang, Motake) VALUES
('KE-A1', 'KV-A', 'Kệ A1 - Toán & Lý luận', 1, 'Kệ sách tự nhiên'),
('KE-A2', 'KV-A', 'Kệ A2 - Cơ khí & Xây dựng', 1, 'Kệ sách kỹ thuật'),
('KE-B1', 'KV-B', 'Kệ B1 - Văn học kinh điển', 2, 'Kệ sách văn học'),
('KE-B2', 'KV-B', 'Kệ B2 - Kỹ năng sống & Tâm lý', 2, 'Kệ sách xã hội'),
('KE-C1', 'KV-C', 'Kệ C1 - Lập trình & Phần mềm', 3, 'Kệ sách CNTT'),
('KE-C2', 'KV-C', 'Kệ C2 - Ngoại ngữ & Từ điển', 3, 'Kệ sách ngoại ngữ');

-- Tạo 3 cuốn vật lý cho mỗi đầu sách hiện có
INSERT IGNORE INTO cuonsach (Macuonsach, Masach, Makesach, Isbn, Ngaynhap, Tinhtrangsach, Trangthaisach)
SELECT 
    CONCAT(s.Masach, '-C01') AS Macuonsach,
    s.Masach,
    CASE 
        WHEN s.Matheloai IN (1, 3) THEN 'KE-A1'
        WHEN s.Matheloai IN (2, 8) THEN 'KE-B1'
        WHEN s.Matheloai IN (6, 7) THEN 'KE-B2'
        WHEN s.Matheloai = 9 THEN 'KE-C1'
        WHEN s.Matheloai = 10 THEN 'KE-C2'
        ELSE 'KE-A2'
    END AS Makesach,
    s.Isbn,
    '2026-01-15',
    'Mới 100%',
    'KHA_DUNG'
FROM sach s;

INSERT IGNORE INTO cuonsach (Macuonsach, Masach, Makesach, Isbn, Ngaynhap, Tinhtrangsach, Trangthaisach)
SELECT 
    CONCAT(s.Masach, '-C02') AS Macuonsach,
    s.Masach,
    CASE 
        WHEN s.Matheloai IN (1, 3) THEN 'KE-A1'
        WHEN s.Matheloai IN (2, 8) THEN 'KE-B1'
        WHEN s.Matheloai IN (6, 7) THEN 'KE-B2'
        WHEN s.Matheloai = 9 THEN 'KE-C1'
        WHEN s.Matheloai = 10 THEN 'KE-C2'
        ELSE 'KE-A2'
    END AS Makesach,
    s.Isbn,
    '2026-01-15',
    'Mới 100%',
    'KHA_DUNG'
FROM sach s;

INSERT IGNORE INTO cuonsach (Macuonsach, Masach, Makesach, Isbn, Ngaynhap, Tinhtrangsach, Trangthaisach)
SELECT 
    CONCAT(s.Masach, '-C03') AS Macuonsach,
    s.Masach,
    CASE 
        WHEN s.Matheloai IN (1, 3) THEN 'KE-A1'
        WHEN s.Matheloai IN (2, 8) THEN 'KE-B1'
        WHEN s.Matheloai IN (6, 7) THEN 'KE-B2'
        WHEN s.Matheloai = 9 THEN 'KE-C1'
        WHEN s.Matheloai = 10 THEN 'KE-C2'
        ELSE 'KE-A2'
    END AS Makesach,
    s.Isbn,
    '2026-01-15',
    'Mới 100%',
    'KHA_DUNG'
FROM sach s;
