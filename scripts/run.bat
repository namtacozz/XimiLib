@echo off
setlocal
title XimiLib - Desktop Client
cd /d "%~dp0.."

echo ===================================================
echo   KHOI CHAY UNG DUNG THU VIEN XIMILIB
echo ===================================================

if not exist "dist\XimiLib.jar" (
    echo [CANH BAO] Chua tim thay dist\XimiLib.jar.
    echo Dang tu dong goi lai ma nguon qua scripts\build.bat...
    call "scripts\build.bat"
)

echo Dang mo giao dien XimiLib Desktop...
java -jar "dist\XimiLib.jar"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [LOI] Khong the chay ung dung! Ma loi: %ERRORLEVEL%
    pause
    exit /b %ERRORLEVEL%
)
endlocal
