UPDATE nguoidung 
SET Matkhau = '$2a$10$GQBiVxZMhZXp.now2MCu3.OW8KqSUGVH5//qTuN1/Lb.zps5QMo02',
    solansai = 0,
    khoaden = NULL,
    Trangthai = 1
WHERE Tendangnhap = 'admin';

UPDATE nguoidung 
SET Matkhau = '$2a$10$9BQggIBf5SOkBmU6Xgldruss1DCko8/sJgeUFVcWRJpJs7OxlkaHC',
    solansai = 0,
    khoaden = NULL,
    Trangthai = 1
WHERE Tendangnhap IN ('kien.phan', 'nam.le', 'sinhvien');
