#!/bin/bash
set -e

# Chuyển về thư mục gốc của dự án
cd "$(dirname "$0")"

echo "==================================================="
echo "  ĐANG KHỞI CHẠY ỨNG DỤNG THƯ VIỆN XIMILIB..."
echo "==================================================="

if [ ! -f "dist/XimiLib.jar" ]; then
    echo "[CẢNH BÁO] File dist/XimiLib.jar chưa tồn tại."
    echo "Đang thực hiện build tự động..."
    chmod +x "scripts/build.sh"
    ./scripts/build.sh
fi

java -jar "dist/XimiLib.jar"
