@echo off
setlocal
title XimiLib - Build Maven and Export Release JAR
cd /d "%~dp0.."

echo ===================================================
echo   XIMILIB BUILD SYSTEM (Java 17 + Maven)
echo ===================================================
echo [1/3] Bat dau dong goi Maven tu sources/pom.xml...

call mvn clean package -f "sources/pom.xml" -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [LOI] Dong goi Maven that bai! Ma loi: %ERRORLEVEL%
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [2/3] Kiem tra va sao chep ban build vao thu muc dist/...
if not exist "dist" mkdir "dist"

if exist "sources\target\XimiLib-app.jar" (
    copy /y "sources\target\XimiLib-app.jar" "dist\XimiLib.jar" >nul
    echo [THANH CONG] Da cap nhat: dist\XimiLib.jar
) else if exist "sources\target\XimiLib-1.0.0.jar" (
    copy /y "sources\target\XimiLib-1.0.0.jar" "dist\XimiLib.jar" >nul
    echo [THANH CONG] Da cap nhat: dist\XimiLib.jar
) else (
    echo [CANH BAO] Khong tim thay file JAR dau ra trong sources/target!
)

echo.
echo [3/3] Hoan tat! Ban co the chay ung dung bang file run.bat hoac scripts\run.bat
echo ===================================================
endlocal
