#!/bin/bash
# ==============================================================================
# XimiLib - Script Thiết lập Tự động Môi trường CSDL Local (PBL3)
# Tính chất: Idempotent (chạy một lần hoặc nhiều lần đều an toàn, không xóa đè dữ liệu)
# ==============================================================================
set -e

# Chuyển về thư mục gốc của dự án
cd "$(dirname "$0")/.."

echo "==================================================="
echo "  XIMILIB - THIẾT LẬP MÔI TRƯỜNG DỰ ÁN PBL3"
echo "==================================================="

# 1. Kiểm tra Java 25 LTS
if ! command -v java >/dev/null 2>&1; then
    echo "[LỖI] Không tìm thấy Java trên hệ thống!"
    echo "Vui lòng cài đặt OpenJDK 25 LTS và thêm vào biến môi trường PATH."
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ -n "$JAVA_VER" ] && [ "$JAVA_VER" -lt 21 ] 2>/dev/null; then
    echo "[LỖI] Phiên bản Java hiện tại là $JAVA_VER. Dự án yêu cầu Java 21 LTS trở lên."
    exit 1
fi
echo "[OK] Môi trường Java: Phiên bản $JAVA_VER"

# 2. Kiểm tra mysql client
if ! command -v mysql >/dev/null 2>&1; then
    echo "[LỖI] Không tìm thấy lệnh 'mysql' (MySQL Client) trên hệ thống!"
    echo "Vui lòng cài đặt MySQL Client hoặc MySQL Server (phiên bản 8.0 trở lên)."
    exit 1
fi
echo "[OK] MySQL Client đã sẵn sàng."

# Khai báo hằng số tập trung
DEMO_CONFIG="config/db.properties.demo"
REAL_CONFIG="config/db.properties"
DB_NAME="ximilib_db"
DB_USER="ximilib_app"

if [ ! -f "$DEMO_CONFIG" ]; then
    echo "[LỖI] Không tìm thấy file mẫu $DEMO_CONFIG!"
    exit 1
fi

# Đọc mật khẩu ximilib_app từ file demo (không hardcode trong script)
APP_PASS=$(grep -E '^db\.password=' "$DEMO_CONFIG" | cut -d'=' -f2- | tr -d '\r\n ')
if [ -z "$APP_PASS" ]; then
    echo "[LỖI] Không thể đọc db.password từ $DEMO_CONFIG!"
    exit 1
fi

# Dọn dẹp biến nhạy cảm khi script kết thúc hoặc bị hủy
cleanup() {
    unset MYSQL_PWD
    unset ROOT_PWD
}
trap cleanup EXIT INT TERM

# 3. Hỏi mật khẩu root MySQL bằng nhập ẩn (read -s)
echo -n "Nhập mật khẩu root MySQL của máy bạn (nhập ẩn, Enter nếu không có pass): "
read -s ROOT_PWD
echo ""

export MYSQL_PWD="$ROOT_PWD"

# Kiểm tra xác thực root
if ! mysql -u root -e "SELECT 1;" >/dev/null 2>&1; then
    echo "[LỖI] Xác thực tài khoản root MySQL thất bại! Vui lòng kiểm tra lại mật khẩu hoặc dịch vụ MySQL."
    exit 1
fi
echo "[OK] Kết nối MySQL root thành công."

# 4. Tạo cơ sở dữ liệu và tài khoản ứng dụng
echo "[1/4] Khởi tạo cơ sở dữ liệu '$DB_NAME' và tài khoản '$DB_USER'..."
mysql -u root -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -e "CREATE USER IF NOT EXISTS '$DB_USER'@'localhost' IDENTIFIED BY '$APP_PASS';"
mysql -u root -e "ALTER USER '$DB_USER'@'localhost' IDENTIFIED BY '$APP_PASS';"
mysql -u root -e "GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'localhost';"

# Hỗ trợ thêm host 127.0.0.1 để tránh lỗi phân giải IP trên Linux/macOS
mysql -u root -e "CREATE USER IF NOT EXISTS '$DB_USER'@'127.0.0.1' IDENTIFIED BY '$APP_PASS';"
mysql -u root -e "ALTER USER '$DB_USER'@'127.0.0.1' IDENTIFIED BY '$APP_PASS';"
mysql -u root -e "GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'127.0.0.1';"
mysql -u root -e "FLUSH PRIVILEGES;"
echo "[OK] Phân quyền CSDL cho '$DB_USER' thành công."

# 5. Kiểm tra và nạp Migration / Seed dữ liệu
TABLE_COUNT=$(mysql -u root -D "$DB_NAME" -N -s -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME';")
if [ "$TABLE_COUNT" -gt 0 ]; then
    echo "[2/4] Cơ sở dữ liệu '$DB_NAME' đã có $TABLE_COUNT bảng. Bỏ qua bước nạp schema và seed (idempotent)."
else
    echo "[2/4] Nạp cấu trúc bảng qua migrations (V1 -> V2 -> V3 -> V4)..."
    mysql -u root -D "$DB_NAME" < "db/migrations/V1__initial_schema.sql"
    mysql -u root -D "$DB_NAME" < "db/migrations/V2__srs_schema_updates.sql"
    mysql -u root -D "$DB_NAME" < "db/migrations/V3__enforce_bcrypt_passwords.sql"
    if [ -f "db/migrations/V4__report_and_search_indexes.sql" ]; then
        mysql -u root -D "$DB_NAME" < "db/migrations/V4__report_and_search_indexes.sql"
    fi

    echo "Đang nạp 221 đầu sách và bản sao mẫu..."
    mysql -u root -D "$DB_NAME" < "db/seed/seed_books.sql"
    if [ -f "db/seed/seed_copies.sql" ]; then
        mysql -u root -D "$DB_NAME" < "db/seed/seed_copies.sql"
    fi
    if [ -f "db/seed/seed_demo_loans.sql" ]; then
        mysql -u root -D "$DB_NAME" < "db/seed/seed_demo_loans.sql"
    fi
    echo "[OK] Nạp migration và dữ liệu mẫu hoàn tất."
fi

# 6. Thiết lập file cấu hình cục bộ config/db.properties
echo "[3/4] Kiểm tra file cấu hình kết nối ứng dụng..."
if [ ! -f "$REAL_CONFIG" ]; then
    cp "$DEMO_CONFIG" "$REAL_CONFIG"
    echo "[OK] Đã tạo $REAL_CONFIG từ $DEMO_CONFIG."
else
    echo "[INFO] File $REAL_CONFIG đã tồn tại. Giữ nguyên cấu hình hiện tại, không ghi đè."
fi

# 7. Kiểm tra tính toàn vẹn cuối cùng
echo "[4/4] Kiểm tra tính toàn vẹn hệ thống..."
FINAL_TABLES=$(mysql -u root -D "$DB_NAME" -N -s -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME';")
FINAL_BOOKS=$(mysql -u root -D "$DB_NAME" -N -s -e "SELECT COUNT(*) FROM sach WHERE IsDeleted = FALSE;")

echo "-> Số bảng hiện có : $FINAL_TABLES / 23"
echo "-> Số sách hiện có : $FINAL_BOOKS / 221"

if [ "$FINAL_TABLES" -eq 23 ] && [ "$FINAL_BOOKS" -eq 221 ]; then
    echo "==================================================="
    echo "[OK] THIẾT LẬP HOÀN TẤT THÀNH CÔNG!"
    echo "Khởi chạy ứng dụng bằng: ./run.sh hoặc java -jar dist/XimiLib.jar"
    echo "==================================================="
else
    echo "[CẢNH BÁO] Kiểm tra tính toàn vẹn chưa đạt (kỳ vọng 23 bảng và 221 sách)."
    exit 1
fi
