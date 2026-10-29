@echo off
setlocal
title XimiLib - Build Maven and Export Release JAR
cd /d "%~dp0.."

echo ===================================================
echo   XIMILIB BUILD SYSTEM (Java 17 + Maven)
echo ===================================================
echo [1/2] Bat dau dong goi Maven tu sources/pom.xml...

call mvn clean package -f "sources/pom.xml" -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [LOI] Dong goi Maven that bai! Ma loi: %ERRORLEVEL%
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/2] Kiem tra ban build trong thu muc dist/...
if exist "dist\XimiLib.jar" (
    echo [THANH CONG] Da tao: dist\XimiLib.jar
) else (
    echo [LOI] Khong tim thay file dist\XimiLib.jar sau khi dong goi!
    pause
    exit /b 1
)

echo.
echo Hoan tat! Ban co the chay ung dung bang file run.bat hoac scripts\run.bat
echo ===================================================
endlocal
