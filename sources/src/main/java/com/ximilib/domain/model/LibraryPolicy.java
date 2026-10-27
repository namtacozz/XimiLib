package com.ximilib.domain.model;

import java.math.BigDecimal;

/**
 * LibraryPolicy: Điểm khai báo tập trung các hằng số quy định nghiệp vụ theo chuẩn R-09 và SRS.
 * Không rải số tự do trong code.
 */
public final class LibraryPolicy {

    private LibraryPolicy() {}

    /** Số cuốn sách mang về tối đa độc giả được mượn cùng lúc (SRS UC-5) */
    public static final int MAX_BOOKS_BORROW = 3;

    /** Thời hạn mượn sách mang về nhà tối đa: 14 ngày (BR-02) */
    public static final int LOAN_DAYS_HOME = 14;

    /** Giờ hết hạn trả sách trong ngày đối với mượn đọc tại chỗ: 17:00 (BR-04) */
    public static final String DUE_TIME_DESK = "17:00";

    /** Số ngày gia hạn thêm cho mỗi lần: 7 ngày (BR-03) */
    public static final int RENEW_DAYS = 7;

    /** Số lần gia hạn tối đa cho mỗi phiếu mượn: 1 lần (BR-03) */
    public static final int MAX_RENEW_COUNT = 1;

    /** Mức phạt trễ hạn mỗi ngày trên mỗi cuốn sách: 2.000 VNĐ (BR-06) */
    public static final BigDecimal FINE_PER_DAY = new BigDecimal("2000.00");

    /** Phí xử lý kỹ thuật/hư hại nhẹ: 20.000 VNĐ (BR-08) */
    public static final BigDecimal TECHNICAL_FEE = new BigDecimal("20000.00");

    /** Tỷ lệ bồi thường mất sách so với giá bìa: 200% (BR-08) */
    public static final BigDecimal COMPENSATION_RATE = new BigDecimal("2.0");

    /** Thời gian giữ sách đặt mượn online: 24 giờ (SRS UC-5) */
    public static final int HOLD_HOURS = 24;

    /** Thời hạn mặc định của thẻ thư viện khi đăng ký mới: 365 ngày (1 năm) */
    public static final int CARD_VALIDITY_DAYS = 365;

    /** Giới hạn số lần đăng nhập sai trước khi tạm khóa: 5 lần (BR-01) */
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    /** Thời gian tạm khóa tài khoản khi nhập sai quá 5 lần: 15 phút (BR-01) */
    public static final int LOCKOUT_MINUTES = 15;
}
