# Sổ Đăng Ký Đề Xuất Bị Từ Chối Của Ponytail (Ponytail Rejected Register)

> Tài liệu ghi nhận các đề xuất rút gọn code của /ponytail nhưng bị từ chối do vi phạm nguyên tắc ưu tiên "Dễ giải thích hơn ngắn" (Luật R-16) hoặc làm thay đổi hành vi nghiệp vụ.

---

| STT | Đề Xuất / Vị Trí | Ponytail Đề Xuất | Lý Do Từ Chối (Quy Tắc Vi Phạm) | Giải Pháp Low-Code Được Chọn |
|:---:|---|---|---|---|
| 1 | `FineCalculationService` (Domain) | Dùng biểu thức lambda / ternary rút gọn công thức phạt | Vi phạm R-15 (Cấm ternary lồng, ưu tiên if-else rõ nghĩa) | Dùng hàm `if (overdueDays > 0)` và return `BigDecimal` tường minh |
| 2 | `DatabaseConnectionManager` (Infra) | Dùng HikariCP hoặc Connection Pool bên ngoài | Vi phạm R-12 (Whitelist thư viện, cấm thêm dependency ngoài) | Giữ `java.sql.DriverManager` thuần túy theo yêu cầu đồ án |
| 3 | `AuthenticationService` (App) | Dùng `Optional<User>` và chained callbacks `.map().orElseThrow()` | Vi phạm R-15 (Cấm Optional chaining, khó debug khi vấn đáp) | Dùng kiểm tra `User user = ...; if (user == null)` return sớm |
| 4 | `BookCatalogView` (Presentation) | Dùng Dynamic Reflection để tự động binding cột bảng JTable | Vi phạm R-15 (Cấm Reflection, sinh viên không giải thích được) | Dùng `DefaultTableModel` cơ bản và switch-case cột tường minh |
| 5 | Giao diện Swing (Presentation) | Dùng timer animation fade-in/fade-out chuyển cảnh | Vi phạm R-11, R-16 (Tránh timer animation đa tầng gây giật lag trên máy yếu) | Dùng FlatLaf client properties tĩnh, giao diện chuyển tức thời < 50ms |
