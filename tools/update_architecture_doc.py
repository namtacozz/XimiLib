import sys
sys.stdout.reconfigure(encoding='utf-8')

file_path = r'F:\AppMaking\XimiLib\docs\ARCHITECTURE.md'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Update version header
content = content.replace(
    'Version: 1.0 | Last updated: 2026-10-27',
    'Version: 1.1 | Last updated: 2026-10-27'
)

# C12: Remove NotificationGateway
content = content.replace(
    '│   └── port/                               # Interfaces giao tiếp ngoại vi (PaymentGateway, NotificationGateway)',
    '│   └── port/                               # Interfaces giao tiếp cổng thanh toán ngoại vi (PaymentGateway)'
)

# C8: Update BookRepositoryImpl example
old_repo_impl = """public class BookRepositoryImpl implements BookRepository {
    private final DatabaseConnectionManager connectionManager;
    private final BookRowMapper rowMapper = new BookRowMapper();

    public BookRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<Book> findById(int id) {
        String sql = "SELECT * FROM sach WHERE masach = ? AND trangthai != 'DA_XOA'";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn đầu sách: " + id, e);
        }
        return Optional.empty();
    }"""

new_repo_impl = """public class BookRepositoryImpl implements BookRepository {
    private final DatabaseConnectionManager connectionManager;

    public BookRepositoryImpl(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<BookSummaryDto> findById(String bookId) {
        String sql = "SELECT s.*, t.Tentheloai, nxb.tennxb FROM sach s "
                   + "LEFT JOIN theloai t ON s.Matheloai = t.Matheloai "
                   + "LEFT JOIN nhaxuatban nxb ON s.Manxb = nxb.manxb "
                   + "WHERE s.Masach = ? AND s.IsDeleted = FALSE";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Map sang BookSummaryDto
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi truy vấn đầu sách: " + bookId, e);
        }
        return Optional.empty();
    }"""

content = content.replace(old_repo_impl, new_repo_impl)

# C7 & C8: Fix soft-delete example in ARCHITECTURE.md
content = content.replace(
    "String sql = \"UPDATE sach SET trangthai = 'DA_XOA' WHERE masach = ?\";",
    "String sql = \"UPDATE sach SET IsDeleted = TRUE WHERE Masach = ?\";"
)

# C9: Fix naming convention section 7.1
old_naming = """- **Tên CSDL:** Bảng và cột viết theo chuẩn `snake_case` chữ thường (ví dụ: `sach`, `masach`, `ngay_muon`, `trang_thai`)."""
new_naming = """- **Tên CSDL:** Bảng viết chữ thường (`sach`, `nguoidung`, `phieumuon`), cột viết theo chuẩn `PascalCase` chữ hoa đầu (`Masach`, `Tensach`, `IsDeleted`, `Ngaymuon`) khớp 100% với `DATABASE_SCHEMA.md`."""
content = content.replace(old_naming, new_naming)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated docs/ARCHITECTURE.md successfully.")
