# BẢN ĐẶC TẢ HỆ THỐNG THIẾT KẾ XIMILIB (DESIGN BRIEF)

> Tài liệu thiết kế giao diện đồ họa XimiLib theo phong cách FlatLaf hiện đại kết hợp bảng màu pastel thanh lịch.
> Mục tiêu: Tinh gọn, thẩm mỹ pastel hiện đại, trực quan, phân cấp thị giác rõ ràng, hoàn toàn miễn nhiễm với lỗi font Unicode và đạt chuẩn tiếp cận WCAG 2.2 AA.

---

## 1. Đối Tượng Sử Dụng & Bối Cảnh
* **Độc giả & Sinh viên (Patron / Student):** Cần tra cứu nhanh, đăng ký mượn về hoặc giữ chỗ tại chỗ, xem số dư phạt và quét mã VietQR Napas247 tiện lợi.
* **Thủ thư (Librarian):** Thao tác bàn lưu hành quầy với tần suất cao, quét mã vạch CCCD/Sách liên tục, cần phím tắt (`Ctrl+F`, `Enter`, `Esc`), độ tương phản cao và phản hồi tức thời < 100ms.
* **Quản trị viên (Admin):** Quản lý phân quyền RBAC, theo dõi biểu đồ xu hướng mượn trả và kết xuất báo cáo Excel/Print.

---

## 2. Hệ Thống Màu Thương Hiệu (Pastel Pink Theme Tokens)
Bảo tồn 100% nhận diện thương hiệu Pastel Pink, không hard-code mã màu hex trong View:

| Token trong `UITheme` | Giá trị Màu Hex | Mục Đích Sử Dụng |
|---|---|---|
| `PRIMARY` | `#DB2777` | Nút chính (CTA), viền focus active, checkbox đã chọn, trang phân trang hiện tại |
| `PRIMARY_HOVER` | `#BE185D` | Trạng thái rê chuột nút chính |
| `PRIMARY_PRESSED` | `#9D174D` | Trạng thái nhấn giữ nút chính |
| `PASTEL` | `#F9A8D4` | Nền icon tròn, chấm chỉ báo trạng thái nhẹ |
| `PASTEL_MID` | `#FBCFE8` | Viền nhấn trang trí, thanh tiến độ |
| `TINT` | `#FCE7F3` | Nền tiêu đề bảng (`TableHeader`), dòng đang chọn, mục sidebar đang mở |
| `TINT_LIGHT` | `#FDF2F8` | Nền hover dòng, banner thông báo nhẹ, placeholder skeleton |
| `PAGE_BG` | `#FFF7FA` | Nền toàn bộ cửa sổ ứng dụng và các trang nội dung |
| `CARD_BORDER` | `#F3E4EC` | Đường viền thẻ card, vạch phân cách chia đôi form |
| `TEXT_PRIMARY` | `#1F1720` | Văn bản chính, tiêu đề (Tỉ lệ tương phản > 10:1 trên nền trắng) |
| `TEXT_MUTED` | `#6B5A66` | Phụ đề, nhãn trường, placeholder (Tương phản $\ge$ 4.8:1, đạt chuẩn WCAG AA) |
| `SUCCESS` | `#047857` (Nền: `#D1FAE5`) | Huy hiệu còn sách, thanh toán thành công |
| `WARNING` | `#B45309` (Nền: `#FEF3C7`) | Cảnh báo còn 1 cuốn, cảnh báo Caps Lock, sách sắp đến hạn |
| `DANGER` | `#B91C1C` (Nền: `#FEE2E2`) | Báo hết sách, nợ phạt, khóa tài khoản, viền lỗi input |

---

## 3. Thang Đo Khoảng Cách (8px Spacing Grid)
Mọi lề (`margin`), đệm (`padding`) và khoảng hở (`gap`) tuân thủ nghiêm ngặt cấp số nhân 8px:
* **4px (Micro):** Khoảng cách giữa icon và chữ trong badge, khoảng cách viền focus ring.
* **8px (Small):** Khoảng cách giữa nhãn và ô nhập liệu, khoảng cách giữa các badge.
* **12px (Regular):** Đệm trong của ô nhập liệu (`JTextField`), padding dòng bảng.
* **16px (Medium):** Khoảng cách giữa các hàng form, đệm trong của thẻ nhỏ.
* **24px (Large):** Đệm trong của Card chính (`SectionCard`), lề tiêu đề trang (`PageHeader`).
* **32px (X-Large):** Khoảng cách giữa các khối nghiệp vụ lớn trên trang chủ.

---

## 4. Quy Chuẩn Bo Góc (Border Radius) & Độ Nổi (Elevation)
* **Ô nhập liệu & Nút bấm (`PrimaryButton`, `TextInputField`):** Bo góc `8px`.
* **Thẻ nghiệp vụ & Khung Modal (`SectionCard`, `ModalDialog`):** Bo góc `12px` hoặc `16px`.
* **Huy hiệu trạng thái (`StatusBadge`):** Dạng pill tròn hoàn toàn `999px`.
* **Độ nổi:** Chỉ sử dụng viền kẻ mảnh `1px` màu `CARD_BORDER` kết hợp bóng mờ FlatLaf `dropShadow` nhẹ 2px, không lạm dụng hiệu ứng 3D đổ bóng đậm lỗi thời.

---

## 5. Quy Chuẩn Icon & Đồ Họa Vector (Anti-Tofu Rule)
* Tuyệt đối không dùng ký tự Unicode đặc thù làm icon (như `✕`, `✔`, `⚠`, `➜`, `★`) vì gây lỗi ô vuông (glyph tofu) trên các máy Windows không cài font tiếng Việt mở rộng.
* **100% Icon sử dụng:**
  1. `FlatSVGIcon` nạp từ thư viện icon chuẩn.
  2. Các hàm vẽ vector `Graphics2D` tập trung trong `IconFactory` (như `createUserIcon`, `createBookIcon`, `createGridIcon`, `createListIcon`, `createWarningTriangleIcon`).

---

## 6. Bộ Thành Phần Tái Sử Dụng (Reusable UI Component Set)
Mọi màn hình chỉ ghép từ các khối chuẩn:
1. `PageHeader`: Tiêu đề trang, phụ đề và các nút thao tác nhanh.
2. `SectionCard`: Khung thẻ nền trắng bo góc viền nhẹ đựng nội dung nghiệp vụ.
3. `PrimaryButton` & `SecondaryButton`: Nút chính nổi bật và nút phụ viền mảnh.
4. `TextInputField` & `PasswordInputField`: Ô nhập liệu chuẩn hóa placeholder và outline lỗi.
5. `StatusBadge`: Huy hiệu trạng thái viên thuốc phân biệt rõ màu và chữ (chống rào cản mù màu).
6. `FormErrorLabel`: Nhãn báo lỗi tức thời dưới chân từng trường nhập.
7. `EmptyStatePanel`: Trạng thái rỗng kèm minh họa vector và nút hành động khôi phục.
