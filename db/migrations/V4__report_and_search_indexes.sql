-- ============================================================================
-- Migration V4: Tối ưu hóa chỉ mục (Indexes) phục vụ Báo cáo, Thống kê và Tìm kiếm
-- Dự án: XimiLib (PBL3)
-- Đối chiếu: DATABASE_SCHEMA.md §Chỉ mục tối ưu hóa
-- ============================================================================

-- 1. Index tăng tốc đếm tồn kho cuốn sách và khóa bi quan SELECT ... FOR UPDATE (R-05)
-- Phục vụ: BookRepositoryImpl.searchBooks (đếm available_copies) và CirculationRepositoryImpl (FOR UPDATE)
CREATE INDEX idx_cuonsach_status_book ON cuonsach (Masach, Trangthaisach);

-- 2. Index tăng tốc truy vấn phiếu mượn quá hạn và tác vụ quét tự động nền (BR-06, UC-7)
-- Phục vụ: ReportRepositoryImpl.getOverdueReport và BackgroundJobManager
CREATE INDEX idx_phieumuon_overdue ON phieumuon (Trangthai, Hantranhan);

-- 3. Index tăng tốc truy vấn thống kê lượt mượn theo thời gian trên Dashboard (UC-7)
-- Phục vụ: Biểu đồ xu hướng mượn sách 7 ngày/30 ngày gần nhất
CREATE INDEX idx_phieumuon_date ON phieumuon (Ngaymuon);

-- 4. Index tăng tốc truy vấn tổng hợp doanh thu tiền phạt theo ngày/tháng (UC-7, F-05)
-- Phục vụ: Biểu đồ doanh thu phạt và báo cáo tài chính
CREATE INDEX idx_thanhtoan_date ON thanhtoan (Ngaythanhtoan);
