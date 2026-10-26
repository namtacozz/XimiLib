package com.ximilib.gui.util;

import com.ximilib.models.User;
import java.util.ArrayList;
import java.util.List;

/**
 * UserSession: Quản lý danh sách tài khoản mẫu trong bộ nhớ
 * và lưu trữ phiên đăng nhập hiện tại của người dùng.
 * Sẵn sàng để Nam ghép nối vào AuthService (BLL) & CSDL MySQL sau này.
 */
public class UserSession {

    private static final List<User> USERS = new ArrayList<>();
    private static User currentUser = null;

    static {
        // 1. Tài khoản Quản Trị Viên (TechLead Nhật)
        USERS.add(new User(
            "ND001", "admin", "123456",
            "Nguyễn Việt Nhật", "nhat.nguyen@dut.udn.vn", "0905 111 222",
            "Quản Trị"
        ));

        // 2. Tài khoản Thủ Thư (Kiên - GUI & DB)
        USERS.add(new User(
            "ND002", "kien.phan", "123456",
            "Phan Vũ Xuân Kiên", "kien.phan@dut.udn.vn", "0905 123 456",
            "Thủ Thư"
        ));

        // 3. Tài khoản Thủ Thư (Nam - BLL & Core)
        USERS.add(new User(
            "ND003", "nam.le", "123456",
            "Lê Võ Thành Nam", "nam.le@dut.udn.vn", "0905 333 444",
            "Thủ Thư"
        ));

        // 4. Tài khoản Bạn Đọc / Sinh Viên
        USERS.add(new User(
            "ND004", "sinhvien", "123456",
            "Trần Văn An", "an.tran@sv.dut.udn.vn", "0914 999 888",
            "Độc Giả"
        ));
    }

    /**
     * Xác thực thông tin đăng nhập theo username hoặc email
     */
    public static User authenticate(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || password == null) return null;
        for (User user : USERS) {
            boolean matchesIdentifier = user.getUsername().equalsIgnoreCase(usernameOrEmail) 
                                     || user.getEmail().equalsIgnoreCase(usernameOrEmail);
            if (matchesIdentifier && user.getPassword().equals(password)) {
                currentUser = user;
                return user;
            }
        }
        return null;
    }

    /**
     * Đăng ký tài khoản mới vào hệ thống
     */
    public static boolean register(User newUser) {
        for (User user : USERS) {
            if (user.getUsername().equalsIgnoreCase(newUser.getUsername()) ||
                user.getEmail().equalsIgnoreCase(newUser.getEmail())) {
                return false; // Trùng username hoặc email
            }
        }
        USERS.add(newUser);
        return true;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static List<User> getAllUsers() {
        return USERS;
    }
}
