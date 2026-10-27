import sys
sys.stdout.reconfigure(encoding='utf-8')

file_path = r'F:\AppMaking\XimiLib\docs\BUSINESS_RULES.md'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Update version header
content = content.replace(
    'Version: 1.0 | Last updated: 2026-10-27',
    'Version: 1.1 | Last updated: 2026-10-27'
)

# Update FSM Section 1.1
old_fsm_1_1 = """### 1.1 FSM Phiếu Mượn (`phieumuon`)
Mỗi phiếu mượn đại diện cho một giao dịch mượn sách của độc giả và chuyển đổi trạng thái theo sơ đồ sau:

```mermaid
stateDiagram-v2
    [*] --> DANG_MUON: Thủ thư duyệt mượn (BR-02, BR-04)
    DANG_MUON --> DANG_MUON: Gia hạn thành công (BR-03)
    DANG_MUON --> DA_TRA: Trả đủ sách đúng hạn
    DANG_MUON --> QUA_HAN: Quá ngày hẹn trả (Quét lúc 00:00)
    QUA_HAN --> DA_TRA: Trả sách + lập phiếu phạt (BR-06)
    DANG_MUON --> DA_TRA: Báo mất/hỏng + lập phiếu đền bù (BR-08)
    QUA_HAN --> DA_TRA: Báo mất khi quá hạn + lập đền bù
    DA_TRA --> [*]
```

#### Bảng Chuyển trạng thái Phiếu mượn:
| Trạng thái nguồn | Sự kiện kích hoạt | Điều kiện kiểm tra (Guard) | Trạng thái đích | Hành động kèm theo |
|---|---|---|---|---|
| `[*]` | Tạo phiếu mượn | Độc giả thỏa mãn Validation Matrix | `1` (`DANG_MUON`) | Giảm tồn kho, chuyển cuốn sách sang `DANG_MUON`. |
| `1` (`DANG_MUON`) | Yêu cầu gia hạn | Chưa gia hạn lần nào & không ai đặt trước | `1` (`DANG_MUON`) | Tăng ngày hẹn trả thêm 7 ngày (BR-03), lưu `giahanmuon`. |
| `1` (`DANG_MUON`) | Quét quá hạn | `CURRENT_DATE > Ngaytra` | `3` (`QUA_HAN`) | Đánh dấu quá hạn, gửi thông báo nhắc nhở tới độc giả. |
| `1` (`DANG_MUON`) | Trả sách | Trả đủ số lượng sách, không hư hỏng | `2` (`DA_TRA`) | Chuyển cuốn sách về `KHA_DUNG`, đóng phiếu mượn. |
| `3` (`QUA_HAN`) | Trả sách trễ | Độc giả mang trả sách | `2` (`DA_TRA`) | Tính tiền phạt BR-06, tạo bản ghi `phieuphat`, chuyển sách về `KHA_DUNG`. |
| `1` hoặc `3` | Báo mất/hỏng | Độc giả/thủ thư xác nhận mất hoặc hỏng | `2` (`DA_TRA`) | Đóng phiếu mượn, chuyển cuốn sách sang `HONG_MAT`, tạo `phieuphat` bồi thường (BR-08). |"""

new_fsm_1_1 = """### 1.1 FSM Phiếu Mượn (`phieumuon`) (C1, C2, C3)
Mỗi phiếu mượn đại diện cho một giao dịch mượn sách của độc giả và chuyển đổi trạng thái theo chuẩn SRS UC-5, UC-6 và UC-9:

```mermaid
stateDiagram-v2
    [*] --> CHO_DUYET: Độc giả đặt mượn Online (UC-5)
    [*] --> DANG_MUON: Thủ thư xuất mượn trực tiếp tại quầy (UC-6)
    CHO_DUYET --> DANG_MUON: Thủ thư xác nhận xuất sách tại quầy
    CHO_DUYET --> DA_HUY: Quá 24 giờ không đến nhận sách (Tự động hủy)
    DANG_MUON --> DANG_MUON: Gia hạn thành công (+7 ngày, BR-03)
    DANG_MUON --> DA_TRA: Trả đủ sách đúng hạn
    DANG_MUON --> QUA_HAN: Quá ngày hẹn trả (Quét lúc 00:00)
    QUA_HAN --> DA_TRA: Trả sách + lập phiếu phạt trễ hạn (BR-06)
    DANG_MUON --> CHO_XU_LY_BOI_THUONG: Báo mất sách (UC-9, BR-08)
    QUA_HAN --> CHO_XU_LY_BOI_THUONG: Báo mất khi quá hạn (UC-9, BR-08)
    CHO_XU_LY_BOI_THUONG --> DA_MAT: Đã nộp đủ tiền đền bù (Đóng phiếu)
    DA_TRA --> [*]
    DA_MAT --> [*]
    DA_HUY --> [*]
```

#### Bảng Chuyển trạng thái Phiếu mượn:
| Trạng thái nguồn | Sự kiện kích hoạt | Điều kiện kiểm tra (Guard) | Trạng thái đích | Hành động kèm theo |
|---|---|---|---|---|
| `[*]` | Đặt mượn Online | Độc giả thỏa mãn V-01..V-05 | `0` (`CHO_DUYET`) | Khóa bi quan 1 cuốn `KHA_DUNG` $\rightarrow$ `DANG_DAT_TRUOC` (giữ 24h). |
| `[*]` | Mượn tại quầy | Độc giả thỏa mãn V-01..V-05 | `1` (`DANG_MUON`) | Quét mã vạch, chuyển cuốn sang `DANG_MUON`, hạn 14 ngày (`VE_NHA`) hoặc 17:00 (`TAI_CHO`). |
| `0` (`CHO_DUYET`) | Xuất sách tại quầy | Độc giả trình thẻ nhận sách trong 24h | `1` (`DANG_MUON`) | Cuốn chuyển `DANG_MUON`, gửi `thongbao` xác nhận. |
| `0` (`CHO_DUYET`) | Quá hạn 24 giờ | Quá thời điểm `Hantranhan` | `6` (`DA_HUY`) | Background job tự hủy phiếu, chuyển cuốn về lại `KHA_DUNG`. |
| `1` (`DANG_MUON`) | Yêu cầu gia hạn | Chưa gia hạn lần nào & không ai đặt trước | `1` (`DANG_MUON`) | Tăng ngày hẹn trả thêm 7 ngày (BR-03), lưu `giahanmuon`. |
| `1` (`DANG_MUON`) | Quét quá hạn | `CURRENT_DATE > Ngaytra` | `3` (`QUA_HAN`) | Đánh dấu quá hạn, gửi thông báo nhắc nhở tới độc giả. |
| `1` (`DANG_MUON`) | Trả sách | Trả đủ số lượng sách, nguyên vẹn | `2` (`DA_TRA`) | Chuyển cuốn sách về `KHA_DUNG`, đóng phiếu mượn. |
| `3` (`QUA_HAN`) | Trả sách trễ | Độc giả mang trả sách | `2` (`DA_TRA`) | Tính tiền phạt BR-06 (2.000đ/ngày), tạo bản ghi `phieuphat`, chuyển sách về `KHA_DUNG`. |
| `1` hoặc `3` | Báo mất sách | Độc giả/thủ thư báo mất sách (UC-9) | `4` (`CHO_XU_LY_BOI_THUONG`) | Chuyển cuốn sang `HONG_MAT`, tính đền bù theo BR-08, lập `phieuphat` lý do "Mất sách". **Phiếu mượn chưa đóng!** |
| `4` | Nộp tiền đền bù | Độc giả nộp phạt tiền mặt / VietQR | `5` (`DA_MAT`) | Ghi nhận `thanhtoan`, cập nhật `phieuphat` thành Đã nộp, **chính thức đóng phiếu mượn**. |"""

content = content.replace(old_fsm_1_1, new_fsm_1_1)

# Add exact verbatim SRS messages in Validation Matrix (Section 4)
old_val_matrix = """## 4. Ma trận Kiểm tra Hợp lệ khi Mượn Sách (Validation Matrix)

Trước khi thực hiện mượn sách hoặc xuất sách, hệ thống bắt buộc kiểm tra tuần tự 5 điều kiện sau:

| Mã kiểm tra | Điều kiện nghiệp vụ | Quy tắc liên quan | Hành động khi vi phạm (Fail Action) |
|---|---|---|---|
| `V-01` | Thẻ thư viện còn hạn sử dụng | `BR-07` | Báo lỗi `ERR_CARD_EXPIRED`: Thẻ thư viện đã hết hạn. |
| `V-02` | Không có khoản phạt chưa thanh toán | `BR-06`, `BR-07` | Báo lỗi `ERR_UNPAID_FINE`: Còn nợ tiền phạt chưa nộp. |
| `V-03` | Số lượng sách đang mượn < 3 cuốn | `BR-02` | Báo lỗi `ERR_BORROW_LIMIT`: Đã đạt giới hạn tối đa 3 cuốn. |
| `V-04` | Không có sách nào đang quá hạn trả | `BR-07` | Báo lỗi `ERR_OVERDUE_BOOKS`: Có sách quá hạn chưa trả. |
| `V-05` | Cuốn sách có trạng thái `KHA_DUNG` | FSM Cuốn sách | Báo lỗi `ERR_BOOK_UNAVAILABLE`: Sách không khả dụng để mượn. |"""

new_val_matrix = """## 4. Ma trận Kiểm tra Hợp lệ khi Mượn Sách (Validation Matrix - Nguyên Văn SRS C10)

Trước khi thực hiện mượn sách hoặc xuất sách, hệ thống bắt buộc kiểm tra tuần tự 5 điều kiện với **thông báo lỗi chuẩn nguyên văn bản Đặc tả SRS**:

| Mã | Điều kiện nghiệp vụ | Quy tắc | Thông báo hiển thị nguyên văn theo SRS (Bắt buộc) |
|---|---|---|---|
| `V-01` | Thẻ thư viện còn hạn sử dụng | `BR-07` | **"Thẻ thư viện của bạn đã hết hạn sử dụng. Vui lòng liên hệ thủ thư để gia hạn"** |
| `V-02` | Không có khoản phạt nợ | `BR-06`, `BR-07` | **"Bạn có khoản phạt chưa hoàn thành. Vui lòng nộp phạt trước khi mượn tiếp"** |
| `V-03` | Số lượng sách mang về < 3 cuốn | `BR-02` | **"Bạn đã đạt giới hạn mượn tối đa 3 cuốn sách mang về"** (Chỉ tính hình thức `VE_NHA`) |
| `V-04` | Không có sách quá hạn | `BR-07` | **"Bạn có sách quá hạn chưa hoàn trả. Vui lòng trả sách trước khi mượn tiếp"** |
| `V-05` | Cuốn sách có trạng thái `KHA_DUNG` | FSM Cuốn sách | **"Sách hiện không còn bản sao khả dụng trên kệ"** |

### Bảng Thông báo Lỗi Hệ thống Khác (Khớp 100% SRS):
- Đăng ký trùng CCCD/Email: **"Số CMND/CCCD hoặc Email đã được đăng ký trên hệ thống"** (UC-2)
- Đăng nhập sai thông tin: **"Tên đăng nhập hoặc mật khẩu không chính xác"** (UC-1)
- Tài khoản bị khóa: **"Tài khoản của bạn đã bị khóa. Vui lòng liên hệ thủ thư"** (UC-1, Trangthai = 0)
- Lỗi kết nối CSDL: **"Không thể kết nối đến máy chủ, vui lòng thử lại sau"** (UC-1, Phi chức năng)
- Tra cứu không thấy sách: **"Không tìm thấy cuốn sách nào khớp với từ khóa của bạn"** (UC-4)
- Thêm sách thành công: **"Thêm sách thành công"** (UC-3)
- Xóa sách đang mượn: **"Không thể xóa đầu sách đang có độc giả mượn"** (UC-3, BR-09)
- Đặt mượn thành công: **"Đặt mượn thành công! Vui lòng đến quầy thư viện xuất trình thẻ thư viện trong vòng 24 giờ để nhận sách"** (UC-5)"""

content = content.replace(old_val_matrix, new_val_matrix)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated docs/BUSINESS_RULES.md successfully.")
