#!/bin/bash
set -e

# Chuyển về thư mục gốc của dự án
cd "$(dirname "$0")/.."

echo "==================================================="
echo "  XIMILIB BUILD SYSTEM (Java 17 + Maven)"
echo "==================================================="
echo "[1/3] Bắt đầu đóng gói Maven từ sources/pom.xml..."

mvn clean package -f "sources/pom.xml" -DskipTests

echo "[2/3] Kiểm tra và sao chép bản build vào thư mục dist/..."
mkdir -p dist

if [ -f "sources/target/XimiLib-app.jar" ]; then
    cp -f "sources/target/XimiLib-app.jar" "dist/XimiLib.jar"
    echo "[THÀNH CÔNG] Đã cập nhật: dist/XimiLib.jar"
elif [ -f "sources/target/XimiLib-1.0.0.jar" ]; then
    cp -f "sources/target/XimiLib-1.0.0.jar" "dist/XimiLib.jar"
    echo "[THÀNH CÔNG] Đã cập nhật: dist/XimiLib.jar"
else
    echo "[CẢNH BÁO] Không tìm thấy file JAR đầu ra trong sources/target!"
fi

echo "[3/3] Hoàn tất! Chạy ứng dụng bằng ./run.sh hoặc java -jar dist/XimiLib.jar"
echo "==================================================="
