# TÀI LIỆU THIẾT KẾ HỆ THỐNG
**Dự án:** Phần mềm Quản lý Thư viện (Đồ án PBL - Dự án Công nghệ phần mềm)
**Đơn vị:** Trường Đại học Bách khoa - Đại học Đà Nẵng
**Đại diện nhóm:** Lê Võ Thành Nam

---

## 1. TỔNG QUAN KIẾN TRÚC & MÔI TRƯỜNG (ARCHITECTURE & ENVIRONMENT)
*   **Hệ quản trị Cơ sở dữ liệu (DBMS):** MySQL. Lựa chọn này đảm bảo tính toàn vẹn dữ liệu, hỗ trợ tốt các truy vấn phức tạp và dễ dàng phân quyền trực tiếp dưới CSDL.
*   **Mô hình Kiến trúc:** Hệ thống tuân thủ nghiêm ngặt kiến trúc 3 lớp (3-layer Architecture) bao gồm:
    *   **GUI (Graphical User Interface):** Lớp giao diện người dùng, được xây dựng bằng Python và PyQt6.
    *   **BLL (Business Logic Layer):** Lớp xử lý nghiệp vụ cốt lõi (tính toán phí phạt, kiểm tra điều kiện mượn/trả, phân tích phân hạng thẻ).
    *   **DAL (Data Access Layer):** Lớp giao tiếp trực tiếp với MySQL, chứa các câu lệnh SQL (CRUD).
*   **Chiến lược Quản lý Mã nguồn (Git Workflow):** Dự án sử dụng mô hình tối giản với **1 nhánh `main` duy nhất**. Các thành viên sẽ push và merge code trực tiếp trên nhánh này để đảm bảo tính tích hợp liên tục và giảm thiểu rủi ro xung đột (conflict) cho nhóm nhỏ.

---

## 2. PHÂN QUYỀN VÀ TÁC NHÂN (ROLES & PERMISSIONS)
Hệ thống được thiết kế phục vụ 3 nhóm đối tượng người dùng riêng biệt với ranh giới quyền hạn rõ ràng:

1.  **Quản trị viên (Admin):**
    *   Nắm toàn quyền kiểm soát hệ thống.
    *   Quản lý tài khoản và phân quyền cho Thủ thư.
    *   Cấu hình các tham số hệ thống (mức phí phạt, giá các hạng thẻ thành viên).
2.  **Thủ thư (Librarian):**
    *   **Quản lý danh mục sách:** Thêm, sửa, xóa, phân loại sách.
    *   **Quản lý độc giả:** Cấp thẻ, cập nhật hồ sơ, nâng/hạ hạng thẻ.
    *   **Nghiệp vụ cốt lõi:** Lập phiếu mượn/trả sách, quản lý danh sách mượn quá hạn và thu phí phạt.
    *   **Báo cáo:** Xem và xuất các thống kê, báo cáo hoạt động của thư viện.
3.  **Độc giả (Reader):**
    *   **Tài khoản cá nhân:** Đăng nhập bằng tài khoản được cấp (Thông tin gồm: Tên, SĐT, Ngày sinh, Giới tính, Email).
    *   **Tiện ích:** Tra cứu, khám phá danh mục sách; xem lịch sử mượn trả cá nhân; theo dõi các sách đang mượn.
    *   **Tương tác:** Lập thẻ mượn sách trực tuyến, gửi yêu cầu gia hạn sách, thực hiện đánh giá (rating) và đề xuất sách mới.

---

## 3. THIẾT KẾ GIAO DIỆN NGƯỜI DÙNG (UI/UX)
*   **Bố cục màn hình chính (Main Layout):** Dựa trên thiết kế tham khảo từ image_300628.jpg, phần mềm áp dụng phong cách thiết kế hiện đại, bao gồm:
    *   **Sidebar (Thanh điều hướng bên trái):** Phân chia rõ ràng các module (Dashboard, Sách, Độc giả, Mượn/Trả, Cài đặt). Có khả năng mở rộng/thu gọn.
    *   **Top Bar:** Chứa thanh tìm kiếm toàn cục, thông báo, và thông tin tài khoản đang đăng nhập.
    *   **Main Content:** Vùng hiển thị dữ liệu chính dạng bảng (Data Table) tích hợp sẵn tính năng phân trang (pagination), bộ lọc thời gian và các nút thao tác nhanh (Sửa/Xóa) trực tiếp trên từng dòng dữ liệu tương tự như image_300628.jpg.
*   **Chủ đề giao diện (Themes):** Hỗ trợ chuyển đổi linh hoạt giữa chế độ Sáng (Light Theme) và Tối (Dark Theme) để tối ưu trải nghiệm thị giác của Thủ thư khi làm việc trong thời gian dài.

---

## 4. CẤU TRÚC DỮ LIỆU & NGHIỆP VỤ CỐT LÕI (CORE CRUD & LOGIC)

### 4.1. Thực thể Sách (Books)
Mỗi đầu sách sẽ được quản lý qua các thuộc tính:
*   `Mã sách` (Primary Key), `Mã ISBN`, `Tên sách`, `Tác giả`, `Thể loại`, `Nhà xuất bản`, `Năm XB`.
*   **Thông tin lưu kho:** `Số lượng tổng`, `Số lượng tồn kho`, `Vị trí kệ sách` (để định vị nhanh).
*   **Thông tin nghiệp vụ:** `Giá sách` (để tính đền bù), **`Hạng sách`** (phục vụ logic mượn), `Đánh giá từ người đọc`.

### 4.2. Thực thể Độc giả (Readers) & Logic Hạng thẻ (Membership Tiers)
*   **Thuộc tính cơ bản:** `Mã thẻ` (Primary Key), `Họ tên`, `Ngày sinh`, `SĐT`, `Email`, `Ngày cấp thẻ`, `Ngày hết hạn`, `Trạng thái thẻ` (Hoạt động/Khóa).
*   **Nghiệp vụ Hạng thẻ (Đặc quyền cốt lõi):**
    *   **Đăng ký & Phí:** Mỗi hạng thẻ có mức phí duy trì hàng tháng khác nhau.
    *   **Quy tắc mượn ngang/thấp hạng:** Thẻ hạng cao có thể mượn sách hạng thấp hơn hoặc ngang hạng mà không cần điều kiện phụ.
    *   **Quy tắc mượn vượt hạng (Cơ chế đặt cọc):** Thẻ hạng thấp muốn mượn sách hạng cao hơn **bắt buộc phải đặt cọc** một khoản tiền tương ứng. Khoảng cách chênh lệch giữa hạng thẻ và hạng sách càng lớn, số tiền cọc hệ thống yêu cầu càng cao.

### 4.3. Động cơ Tìm kiếm (Search Engine)
Hỗ trợ bộ lọc đa chiều (Multi-criteria filter): Lọc kết hợp theo `Tên sách/Mã sách`, `Tác giả`, `Thể loại`, `Năm xuất bản` và `Điểm đánh giá`.

---

## 5. QUY TRÌNH MƯỢN TRẢ & XỬ LÝ VI PHẠM (BORROW/RETURN FLOW)

### 5.1. Quy tắc Mượn sách
*   **Số lượng mượn tối đa:** Không cố định, được hệ thống nội suy dựa trên Hạng thẻ thành viên của độc giả.
*   **Thời hạn mượn:** Cố định **15 ngày** cho mọi đầu sách.
*   **Ràng buộc vi phạm:** Hệ thống chặn tính năng mượn mới đối với các tài khoản đang nợ phí phạt hoặc đang trong thời gian bị phạt cấm mượn. Độc giả buộc phải thanh toán hết nợ hoặc chờ hết hạn phạt mới được tiếp tục sử dụng dịch vụ.
*   **Quy trình tạo phiếu:** Hệ thống tự động hỗ trợ load dữ liệu độc giả và sách để tạo phiếu mượn nhanh chóng, giảm thiểu thao tác nhập liệu thủ công cho Thủ thư.

### 5.2. Chế tài Xử lý Vi phạm
*   **Trả trễ hạn:** Áp dụng mức phạt cố định **2.000 VNĐ / ngày trễ / 1 cuốn sách**.
*   **Làm hỏng / Mất sách:** Áp dụng mức đền bù bằng **200% (x2) Giá trị niêm yết của sách**.

---

## 6. BÁO CÁO VÀ THỐNG KÊ (REPORTS & ANALYTICS)
*   **Màn hình Dashboard (Trang chủ):** Hiển thị trực quan (theo thời gian thực) các chỉ số quan trọng:
    *   Tổng số lượng sách đang tồn kho.
    *   Tổng số lượng thẻ thành viên (Biểu đồ phân loại theo từng Hạng thẻ).
    *   Tổng số lượng sách đang bị mượn quá hạn (Cảnh báo đỏ).
*   **Trích xuất dữ liệu (Export):** Hỗ trợ kết xuất toàn bộ dữ liệu thống kê ra file định dạng **Excel (.xlsx)** phục vụ cho việc lưu trữ, báo cáo cấp trên và kiểm toán thư viện.