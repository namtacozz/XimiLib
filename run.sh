#!/bin/bash
set -e

# Luôn chuyển về thư mục gốc của dự án
cd "$(dirname "$0")"

echo "==================================================="
echo "  ĐANG KHỞI CHẠY ỨNG DỤNG THƯ VIỆN XIMILIB..."
echo "==================================================="

# 1. Kiểm tra file config/db.properties tồn tại và không còn chữ CHANGE_ME
if [ ! -f "config/db.properties" ] || grep -q "CHANGE_ME" "config/db.properties" 2>/dev/null; then
    echo "[LỖI] Chưa cấu hình cơ sở dữ liệu hoặc cấu hình vẫn còn chứa 'CHANGE_ME'!"
    echo "Vui lòng chạy script thiết lập môi trường tự động:"
    echo "  ./scripts/setup.sh"
    echo "Hoặc sao chép config/db.properties.demo sang config/db.properties và cập nhật thông tin."
    exit 1
fi

# 2. Kiểm tra dist/XimiLib.jar, nếu thiếu thì đóng gói Maven
if [ ! -f "dist/XimiLib.jar" ]; then
    echo "[CẢNH BÁO] File dist/XimiLib.jar chưa tồn tại."
    echo "Đang thực hiện build tự động (mvn clean package -DskipTests)..."
    mvn clean package -f sources/pom.xml -DskipTests
fi

# 3. Khởi chạy ứng dụng từ thư mục gốc
echo "Đang mở giao diện XimiLib Desktop..."
java -jar "dist/XimiLib.jar"
