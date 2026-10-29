package com.ximilib.gui.util;

import com.ximilib.domain.model.User;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * UserSession: Quản lý danh sách tài khoản mẫu trong bộ nhớ
 * và lưu trữ phiên đăng nhập hiện tại của người dùng.
 * Sẵn sàng để Nam ghép nối vào AuthService (BLL) & CSDL MySQL sau này.
 */
public class UserSession {

    private static final List<User> USERS = new CopyOnWriteArrayList<>();
    private static volatile User currentUser = null;

    static {
        resetDefaultUsers();
    }

    /**
     * Khởi tạo lại danh sách tài khoản mặc định (hỗ trợ cả chạy thật và Unit Test)
     */
    public static void resetDefaultUsers() {
        USERS.clear();
        currentUser = null;

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
        String query = usernameOrEmail.trim();

        for (User user : USERS) {
            boolean matchesIdentifier = (user.getUsername() != null && user.getUsername().equalsIgnoreCase(query))
                                     || (user.getEmail() != null && user.getEmail().equalsIgnoreCase(query));
            if (matchesIdentifier && user.getPassword() != null && user.getPassword().equals(password)) {
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
        if (newUser == null || newUser.getUsername() == null || newUser.getUsername().trim().isEmpty()) {
            return false;
        }

        String newUsername = newUser.getUsername().trim();
        String newEmail = (newUser.getEmail() != null) ? newUser.getEmail().trim() : null;

        for (User user : USERS) {
            boolean duplicateUsername = user.getUsername() != null 
                                     && user.getUsername().equalsIgnoreCase(newUsername);
            boolean duplicateEmail = user.getEmail() != null && newEmail != null 
                                  && !newEmail.isEmpty() 
                                  && user.getEmail().equalsIgnoreCase(newEmail);
            if (duplicateUsername || duplicateEmail) {
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
