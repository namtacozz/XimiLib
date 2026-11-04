package com.ximilib.presentation.util;

import com.ximilib.domain.model.User;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserSession {

    private static final List<User> USERS = new CopyOnWriteArrayList<>();
    private static volatile User currentUser = null;
    private static volatile String catalogViewMode = "TABLE";

    static {
        resetDefaultUsers();
    }

    public static void resetDefaultUsers() {
        USERS.clear();
        currentUser = null;

        USERS.add(new User(
            "ND001", "admin", "admin123",
            "Nguyễn Việt Nhật", "nhat.nguyen@dut.udn.vn", "0905 111 222",
            "Quản Trị"
        ));

        USERS.add(new User(
            "ND002", "kien.phan", "123456",
            "Phan Vũ Xuân Kiên", "kien.phan@dut.udn.vn", "0905 123 456",
            "Thủ Thư"
        ));

        USERS.add(new User(
            "ND003", "nam.le", "123456",
            "Lê Võ Thành Nam", "nam.le@dut.udn.vn", "0905 333 444",
            "Thủ Thư"
        ));

        USERS.add(new User(
            "ND004", "sinhvien", "123456",
            "Trần Văn An", "an.tran@sv.dut.udn.vn", "0914 999 888",
            "Độc Giả"
        ));
    }

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
                return false;
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

    public static String getCatalogViewMode() {
        return catalogViewMode;
    }

    public static void setCatalogViewMode(String mode) {
        if ("GRID".equalsIgnoreCase(mode)) {
            catalogViewMode = "GRID";
        } else {
            catalogViewMode = "TABLE";
        }
    }
}
