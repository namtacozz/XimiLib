package com.ximilib.application.port;

import java.util.List;

public interface BookCommentPort {

    BookCommentData getBookComments(String bookId);

    void addComment(String bookId, String userId, String content, int stars);

    record BookCommentItem(String author, String text, int stars, String date) {}

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
