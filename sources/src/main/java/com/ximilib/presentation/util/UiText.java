package com.ximilib.presentation.util;

public final class UiText {

    private UiText() {
    }

    public static final String APP_NAME = "XimiLib";
    public static final String APP_SUBTITLE = "Quản lý Thư viện";
    public static final String ROLE_ADMIN = "Quản trị viên";
    public static final String ROLE_LIBRARIAN = "Thủ thư";
    public static final String ROLE_READER = "Độc giả";
    public static final String ROLE_GUEST = "Khách viếng thăm";

    public static final String BTN_LOGIN = "ĐĂNG NHẬP";
    public static final String BTN_LOGGING_IN = "Đang xác thực...";
    public static final String BTN_REGISTER = "Đăng Ký";
    public static final String BTN_REGISTERING = "Đang đăng ký...";
    public static final String BTN_GUEST = "Xem Với Tư Cách Khách";
    public static final String BTN_LOGOUT = "Đăng xuất";
    public static final String BTN_CANCEL = "Hủy bỏ";
    public static final String BTN_CONFIRM = "Xác nhận";
    public static final String BTN_SAVE = "Lưu thay đổi";
    public static final String BTN_CLOSE = "Đóng";
    public static final String BTN_SEARCH = "Tìm kiếm";
    public static final String BTN_RESET = "Đặt lại";
    public static final String BTN_BORROW = "Mượn sách";
    public static final String BTN_RETURN = "Trả sách";
    public static final String BTN_EXTEND = "Gia hạn";
    public static final String BTN_RESERVE = "Đặt trước sách";
    public static final String BTN_EXPORT_EXCEL = "Xuất Excel";
    public static final String BTN_PRINT_REPORT = "In báo cáo";

    public static final String LBL_USERNAME = "Tên đăng nhập, email hoặc số điện thoại";
    public static final String LBL_PASSWORD = "Mật khẩu";
    public static final String LBL_CONFIRM_PASSWORD = "Nhập lại mật khẩu";
    public static final String LBL_FULL_NAME = "Họ và tên";
    public static final String LBL_BIRTH_DATE = "Ngày sinh";
    public static final String LBL_CCCD = "Số CCCD / CMND";
    public static final String LBL_EMAIL = "Địa chỉ email";
    public static final String LBL_PHONE = "Số điện thoại";
    public static final String LBL_ADDRESS = "Địa chỉ liên hệ";
    public static final String LBL_REMEMBER_LOGIN = "Ghi nhớ đăng nhập";
    public static final String LBL_FORGOT_PASSWORD = "Quên mật khẩu?";
    public static final String LBL_NO_ACCOUNT = "Chưa có tài khoản?";
    public static final String LBL_HAVE_ACCOUNT = "Đã có tài khoản?";
    public static final String LBL_REGISTER_NOW = "Đăng ký ngay";
    public static final String LBL_LOGIN_NOW = "Đăng nhập ngay";
    public static final String LBL_OR = "hoặc";
    public static final String LBL_CAPS_LOCK_ON = "Caps Lock đang bật";

    public static final String PH_LOGIN_INPUT = "Tên đăng nhập, email hoặc số điện thoại";
    public static final String PH_PASSWORD = "Nhập mật khẩu của bạn";
    public static final String PH_CONFIRM_PASSWORD = "Nhập lại mật khẩu để xác nhận";
    public static final String PH_FULL_NAME = "Ví dụ: Nguyễn Văn An";
    public static final String PH_DATE = "dd/MM/yyyy";
    public static final String PH_CCCD = "9 hoặc 12 chữ số";
    public static final String PH_EMAIL = "tenban@domain.com";
    public static final String PH_PHONE = "10 chữ số (bắt đầu bằng 0)";
    public static final String PH_ADDRESS = "Số nhà, tên đường, quận/huyện, tỉnh/thành";
    public static final String PH_SEARCH_BOOKS = "Tìm theo tên sách, tác giả hoặc thể loại (Ctrl + F)";

    public static final String REG_GROUP_PERSONAL = "1. Thông tin cá nhân";
    public static final String REG_GROUP_CONTACT = "2. Thông tin liên hệ";
    public static final String REG_GROUP_ACCOUNT = "3. Tài khoản và bảo mật";

    public static final String PWD_RULE_LENGTH = "Tối thiểu 8 ký tự";
    public static final String PWD_RULE_UPPERCASE = "Ít nhất 1 chữ hoa";
    public static final String PWD_RULE_DIGIT = "Ít nhất 1 chữ số";
    public static final String PWD_RULE_SPECIAL = "Ít nhất 1 ký tự đặc biệt (@, #, $, !...)";

    public static final String STOCK_AVAILABLE = "Còn sách";
    public static final String STOCK_ONLY_ONE = "Còn 1 cuốn";
    public static final String STOCK_OUT_OF_STOCK = "Tạm hết";
    public static final String STATUS_BORROWING = "Đang mượn";
    public static final String STATUS_RETURNED = "Đã hoàn trả";
    public static final String STATUS_OVERDUE = "Quá hạn mượn";
    public static final String STATUS_WAITING_COMPENSATION = "Chờ bồi thường";
    public static final String STATUS_LOST = "Đã báo mất";
    public static final String STATUS_FINE_PAID = "Đã nộp phạt";
    public static final String STATUS_FINE_UNPAID = "Chưa nộp phạt";
    public static final String STATUS_ACTIVE = "Đang hoạt động";
    public static final String STATUS_LOCKED = "Đã bị khóa";

    public static final String ERR_REQUIRED_FIELD = "Vui lòng nhập đầy đủ thông tin này";
    public static final String ERR_INVALID_EMAIL = "Địa chỉ email không đúng định dạng";
    public static final String ERR_INVALID_PHONE = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0";
    public static final String ERR_INVALID_CCCD = "Số CMND/CCCD phải gồm 9 hoặc 12 chữ số";
    public static final String ERR_PASSWORD_NOT_MATCH = "Mật khẩu xác nhận không khớp";
    public static final String ERR_PASSWORD_TOO_WEAK = "Mật khẩu chưa đáp ứng đủ 4 tiêu chí bảo mật";
    public static final String ERR_SELECT_BIRTH_DATE = "Vui lòng chọn ngày sinh trên lịch";
    public static final String ERR_LOGIN_FAILED = "Tên đăng nhập hoặc mật khẩu không chính xác.";
    public static final String ERR_ACCOUNT_LOCKED_SRS = "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ thủ thư";
    public static final String ERR_ACCOUNT_LOCKED_15M = "Tài khoản bị tạm khóa 15 phút do nhập sai quá số lần quy định.";
    public static final String ERR_DUPLICATE_USER_SRS = "Số CMND/CCCD hoặc Email đã được đăng ký trên hệ thống";
    public static final String ERR_CANNOT_DELETE_BORROWED_BOOK_SRS = "Không thể xóa đầu sách đang có độc giả mượn";
    public static final String ERR_NO_SEARCH_RESULTS_SRS = "Không tìm thấy cuốn sách nào khớp với từ khóa của bạn";
    public static final String ERR_OVERDUE_RESTRICTION_SRS = "Bạn có sách quá hạn chưa hoàn trả. Vui lòng trả sách trước khi mượn tiếp";
    public static final String ERR_LIMIT_EXCEEDED_SRS = "Bạn đã đạt giới hạn mượn tối đa 3 cuốn sách mang về";
    public static final String ERR_CARD_EXPIRED_SRS = "Thẻ thư viện của bạn đã hết hạn sử dụng. Vui lòng liên hệ thủ thư để gia hạn";
    public static final String ERR_NO_REPORTS_SRS = "Không có phát sinh giao dịch trong mốc thời gian đã chọn";
    public static final String ERR_GUEST_NEED_LOGIN = "Vui lòng đăng nhập để tiếp tục thực hiện";
    public static final String ERR_GUEST_COMMENT_SRS = "Vui lòng đăng nhập để gửi bình luận";

    public static final String MSG_LOGIN_SUCCESS = "Đăng nhập thành công";
    public static final String MSG_REGISTER_SUCCESS = "Đăng ký thành công. Mã thẻ thư viện của bạn:";
    public static final String MSG_LOGOUT_CONFIRM = "Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?";
    public static final String MSG_BORROW_SUCCESS_24H_SRS = "Đăng ký mượn thành công. Vui lòng đến thư viện nhận sách trong vòng 24 giờ";
    public static final String MSG_RETURN_SUCCESS = "Hoàn tất trả sách thành công";
    public static final String MSG_PAYMENT_SUCCESS = "Giao dịch thanh toán thành công";
    public static final String MSG_EXPORT_EXCEL_SUCCESS = "Xuất báo cáo Excel thành công tại:";

    public static final String TITLE_GUEST_BANNER = "Khám phá hàng ngàn đầu sách tại XimiLib";
    public static final String DESC_GUEST_BANNER = "Đăng ký tài khoản để mượn sách mang về, đặt trước và nhận thông báo mượn trả.";
    public static final String TITLE_EMPTY_SEARCH = "Không tìm thấy kết quả phù hợp";
    public static final String DESC_EMPTY_SEARCH = "Thử tìm kiếm với từ khóa ngắn hơn hoặc kiểm tra lại chính tả.";
    public static final String TITLE_CONFIRM_DIALOG = "Xác nhận thao tác";
    public static final String TITLE_ERROR_DIALOG = "Thông báo lỗi";
    public static final String TITLE_SUCCESS_DIALOG = "Thao tác thành công";
}
