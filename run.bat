@echo off
setlocal
title XimiLib - Quan Ly Thu Vien
cd /d "%~dp0"

echo ===================================================
echo   DANG KHOI CHAY UNG DUNG THU VIEN XIMILIB...
echo ===================================================

if not exist "config\db.properties" (
    echo [LOI] Chua cau hinh co so du lieu - thieu file config\db.properties!
    echo Vui long chay script thiet lap moi truong:
    echo   scripts\setup.bat
    echo hoac sao chep config\db.properties.demo sang config\db.properties.
    pause
    exit /b 1
)

findstr /c:"CHANGE_ME" "config\db.properties" >nul 2>nul
if %errorlevel% equ 0 (
    echo [LOI] File config\db.properties van con chua gia tri mau 'CHANGE_ME'!
    echo Vui long cap nhat thong tin ket noi hoac chay scripts\setup.bat.
    pause
    exit /b 1
)

if not exist "dist\XimiLib.jar" (
    echo [CANH BAO] File dist\XimiLib.jar chua ton tai.
    echo Dang thuc hien build tu dong [mvn clean package -DskipTests]...
    call mvn clean package -f sources\pom.xml -DskipTests
    if not exist "dist\XimiLib.jar" (
        echo [LOI] Build that bai, khong the tao dist\XimiLib.jar!
        pause
        exit /b 1
    )
)

echo Dang mo giao dien XimiLib Desktop...
java -jar "dist\XimiLib.jar"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [LOI] Khong the chay ung dung! Ma loi: %ERRORLEVEL%
    pause
)
endlocal
