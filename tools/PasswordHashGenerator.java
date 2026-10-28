package tools;

import com.ximilib.infrastructure.security.SecurityUtil;

/**
 * PasswordHashGenerator: Công cụ một lần sinh chuỗi băm BCrypt an toàn cho tài khoản người dùng và seed CSDL.
 * Tuân thủ R-07, R-08.
 *
 * Cách dùng:
 *   java -cp "sources/target/classes:sources/target/dependency/*:~/.m2/repository/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar" tools.PasswordHashGenerator [mat_khau_can_bam]
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        String rawPassword = (args != null && args.length > 0 && !args[0].trim().isEmpty())
                ? args[0].trim()
                : "admin123";

        SecurityUtil hasher = new SecurityUtil();
        String hashed = hasher.hash(rawPassword);
        boolean matches = hasher.matches(rawPassword, hashed);

        System.out.println("==================================================");
        System.out.println("  XimiLib - Công Cụ Sinh Mã Băm BCrypt (R-08)");
        System.out.println("==================================================");
        System.out.println("Mật khẩu thô : " + rawPassword);
        System.out.println("Mã băm BCrypt: " + hashed);
        System.out.println("Kiểm thử khớp: " + (matches ? "THÀNH CÔNG (MATCH)" : "THẤT BẠI"));
        System.out.println("==================================================");
    }
}
