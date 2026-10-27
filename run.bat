@echo off
setlocal
title XimiLib - Quan Ly Thu Vien
cd /d "%~dp0"

echo ===================================================
echo   DANG KHOI CHAY UNG DUNG THU VIEN XIMILIB...
echo ===================================================

if not exist "dist\XimiLib.jar" (
    echo [CANH BAO] File dist\XimiLib.jar chua ton tai.
    echo Dang thuc hien build tu dong...
    call "scripts\build.bat"
)

java -jar "dist\XimiLib.jar"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [LOI] Khong the chay ung dung! Ma loi: %ERRORLEVEL%
    pause
)
endlocal
