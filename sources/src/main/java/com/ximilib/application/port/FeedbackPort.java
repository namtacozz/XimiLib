package com.ximilib.application.port;

import java.util.List;

/**
 * FeedbackPort: Cổng giao tiếp cho chức năng Góp ý & Phản hồi dịch vụ thư viện (F-10).
 * Tuân thủ Clean Architecture (R-01).
 */
public interface FeedbackPort {

    /**
     * Gửi ý kiến đóng góp / phản hồi từ bạn đọc.
     *
     * @param userId  Mã bạn đọc
     * @param title   Tiêu đề góp ý
     * @param content Nội dung chi tiết
     */
    void submitFeedback(String userId, String title, String content);

    /**
     * Lấy toàn bộ danh sách góp ý để thủ thư / quản trị viên tiếp nhận.
     *
     * @return Danh sách các dòng dữ liệu Object[]: [id, hoten, tieude, noidung, ngaytao, trangthaiText]
     */
    List<Object[]> getAllFeedbacks();

    /**
     * Đánh dấu góp ý đã được xử lý.
     *
     * @param id Mã định danh góp ý
     */
    void markAsProcessed(int id);
}
