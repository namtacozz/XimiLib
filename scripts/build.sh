#!/bin/bash
set -e

# Chuyển về thư mục gốc của dự án
cd "$(dirname "$0")/.."

echo "==================================================="
echo "  XIMILIB BUILD SYSTEM (Java 17 + Maven)"
echo "==================================================="
echo "[1/2] Bắt đầu đóng gói Maven từ sources/pom.xml..."

mvn clean package -f "sources/pom.xml" -DskipTests

echo "[2/2] Kiểm tra bản build trong thư mục dist/..."
if [ -f "dist/XimiLib.jar" ]; then
    echo "[THÀNH CÔNG] Đã tạo: dist/XimiLib.jar"
else
    echo "[LỖI] Không tìm thấy file dist/XimiLib.jar sau khi đóng gói!"
    exit 1
fi

echo "Hoàn tất! Chạy ứng dụng bằng ./run.sh hoặc java -jar dist/XimiLib.jar"
echo "==================================================="
