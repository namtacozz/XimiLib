package com.ximilib.application.port;

import java.util.List;

/**
 * BookCommentPort: Cổng giao tiếp cho chức năng Đánh giá & Bình luận sách (SRS UC-8, F-11).
 * Tách biệt hoàn toàn tầng Application/Presentation khỏi chi tiết truy cập CSDL CSDL MySQL.
 */
public interface BookCommentPort {

    /**
     * Lấy danh sách bình luận, số lượt nhận xét và điểm đánh giá trung bình của một đầu sách.
     *
     * @param bookId Mã đầu sách
     * @return Dữ liệu bình luận và đánh giá của sách
     */
    BookCommentData getBookComments(String bookId);

    /**
     * Thêm một đánh giá/bình luận mới cho đầu sách.
     *
     * @param bookId  Mã đầu sách
     * @param userId  Mã tài khoản người đánh giá
     * @param content Nội dung nhận xét
     * @param stars   Số sao đánh giá (1-5)
     */
    void addComment(String bookId, String userId, String content, int stars);

    /**
     * Dòng dữ liệu chi tiết một nhận xét của độc giả.
     */
    record BookCommentItem(String author, String text, int stars, String date) {}

    /**
     * Dữ liệu tổng hợp đánh giá và danh sách nhận xét của đầu sách.
     */
    class BookCommentData {
        private final double averageRating;
        private final int totalReviews;
        private final List<BookCommentItem> comments;

        public BookCommentData(double averageRating, int totalReviews, List<BookCommentItem> comments) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
            this.comments = comments;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public int getTotalReviews() {
            return totalReviews;
        }

        public List<BookCommentItem> getComments() {
            return comments;
        }
    }
}
