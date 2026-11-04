@echo off
setlocal enabledelayedexpansion
title XimiLib - Thiet Lap Moi Truong CSDL Local (PBL3)
cd /d "%~dp0\.."

echo ===================================================
echo   XIMILIB - THIET LAP MOI TRUONG DU AN PBL3
echo ===================================================

rem 1. Kiem tra Java 17+
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [LOI] Khong tim thay Java tren he thong!
    echo Vui long cai dat OpenJDK 17 tro len va them vao bien moi truong PATH.
    exit /b 1
)

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "JAVA_RAW=%%~g"
)
for /f "delims=." %%v in ("%JAVA_RAW%") do set "JAVA_VER=%%v"
if defined JAVA_VER (
    if %JAVA_VER% lss 17 (
        echo [LOI] Phien ban Java hien tai la %JAVA_VER%. Du an yeu cau Java 17 tro len.
        exit /b 1
    )
    echo [OK] Moi truong Java: Phien ban !JAVA_VER!
)

rem 2. Kiem tra MySQL client
where mysql >nul 2>nul
if %errorlevel% neq 0 (
    echo [LOI] Khong tim thay lenh 'mysql' [MySQL Client] tren he thong!
    echo Vui long cai dat MySQL Client hoac MySQL Server [phien ban 8.0 tro len] va them vao PATH.
    exit /b 1
)
echo [OK] MySQL Client da san sang.

rem Khai bao hang so tap trung
set "DEMO_CONFIG=config\db.properties.demo"
set "REAL_CONFIG=config\db.properties"
set "DB_NAME=ximilib_db"
set "DB_USER=ximilib_app"

if not exist "%DEMO_CONFIG%" (
    echo [LOI] Khong tim thay file mau %DEMO_CONFIG%!
    exit /b 1
)

rem Doc mat khau tu file demo (khong hardcode trong script)
set "APP_PASS="
for /f "tokens=1,* delims==" %%A in ('type "%DEMO_CONFIG%" ^| findstr /r "^db\.password="') do (
    set "APP_PASS=%%B"
)
if not defined APP_PASS (
    echo [LOI] Khong the doc db.password tu %DEMO_CONFIG%!
    exit /b 1
)
set "APP_PASS=%APP_PASS: =%"

rem 3. Nhap mat khau root MySQL an danh (PowerShell Read-Host -AsSecureString)
rem Ghi chu bao mat: Mat khau duoc doc an qua PowerShell vao bo nho tam thoi va huy ngay khi xong.
set "ROOT_PWD="
for /f "delims=" %%p in ('powershell -NoProfile -Command "$p = Read-Host 'Nhap mat khau root MySQL (an danh, Enter neu khong co)' -AsSecureString; [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($p))"') do (
    set "ROOT_PWD=%%p"
)

set "MYSQL_PWD=%ROOT_PWD%"

rem Kiem tra xac thuc root
mysql -u root -e "SELECT 1;" >nul 2>nul
if %errorlevel% neq 0 (
    echo [LOI] Xac thuc tai khoan root MySQL that bai! Vui long kiem tra lai mat khau hoac dich vu MySQL.
    set "MYSQL_PWD="
    set "ROOT_PWD="
    exit /b 1
)
echo [OK] Ket noi MySQL root thanh cong.

rem 4. Khoi tao co so du lieu va tai khoan
echo [1/4] Khoi tao co so du lieu '%DB_NAME%' va tai khoan '%DB_USER%'...
mysql -u root -e "CREATE DATABASE IF NOT EXISTS \`%DB_NAME%\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -e "CREATE USER IF NOT EXISTS '%DB_USER%'@'localhost' IDENTIFIED BY '%APP_PASS%';"
mysql -u root -e "ALTER USER '%DB_USER%'@'localhost' IDENTIFIED BY '%APP_PASS%';"
mysql -u root -e "GRANT ALL PRIVILEGES ON \`%DB_NAME%\`.* TO '%DB_USER%'@'localhost';"

mysql -u root -e "CREATE USER IF NOT EXISTS '%DB_USER%'@'127.0.0.1' IDENTIFIED BY '%APP_PASS%';"
mysql -u root -e "ALTER USER '%DB_USER%'@'127.0.0.1' IDENTIFIED BY '%APP_PASS%';"
mysql -u root -e "GRANT ALL PRIVILEGES ON \`%DB_NAME%\`.* TO '%DB_USER%'@'127.0.0.1';"
mysql -u root -e "FLUSH PRIVILEGES;"
echo [OK] Phan quyen CSDL cho '%DB_USER%' thanh cong.

rem 5. Kiem tra va nap Migrations / Seed
set "TABLE_COUNT=0"
for /f %%A in ('mysql -u root -D "%DB_NAME%" -N -s -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '%DB_NAME%';"') do (
    set "TABLE_COUNT=%%A"
)

if %TABLE_COUNT% gtr 0 (
    echo [2/4] Co so du lieu '%DB_NAME%' da co %TABLE_COUNT% bang. Bo qua buoc nap schema va seed [idempotent].
) else (
    echo [2/4] Nap cau truc bang qua migrations (V1 -^> V2 -^> V3 -^> V4)...
    mysql -u root -D "%DB_NAME%" < "db\migrations\V1__initial_schema.sql"
    mysql -u root -D "%DB_NAME%" < "db\migrations\V2__srs_schema_updates.sql"
    mysql -u root -D "%DB_NAME%" < "db\migrations\V3__enforce_bcrypt_passwords.sql"
    if exist "db\migrations\V4__report_and_search_indexes.sql" (
        mysql -u root -D "%DB_NAME%" < "db\migrations\V4__report_and_search_indexes.sql"
    )

    echo Dang nap 221 dau sach va ban sao mau...
    mysql -u root -D "%DB_NAME%" < "db\seed\seed_books.sql"
    if exist "db\seed\seed_copies.sql" (
        mysql -u root -D "%DB_NAME%" < "db\seed\seed_copies.sql"
    )
    if exist "db\seed\seed_demo_loans.sql" (
        mysql -u root -D "%DB_NAME%" < "db\seed\seed_demo_loans.sql"
    )
    echo [OK] Nap migration va du lieu mau hoan tat.
)

rem 6. Thiet lap file cau hinh cuc bo config\db.properties
echo [3/4] Kiem tra file cau hinh ket noi ung dung...
if not exist "%REAL_CONFIG%" (
    copy "%DEMO_CONFIG%" "%REAL_CONFIG%" >nul
    echo [OK] Da tao %REAL_CONFIG% tu %DEMO_CONFIG%.
) else (
    echo [INFO] File %REAL_CONFIG% da ton tai. Giu nguyen cau hinh hien tai, khong ghi de.
)

rem 7. Kiem tra tinh toan ven cuoi cung
echo [4/4] Kiem tra tinh toan ven he thong...
set "FINAL_TABLES=0"
set "FINAL_BOOKS=0"
for /f %%A in ('mysql -u root -D "%DB_NAME%" -N -s -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '%DB_NAME%';"') do (
    set "FINAL_TABLES=%%A"
)
for /f %%A in ('mysql -u root -D "%DB_NAME%" -N -s -e "SELECT COUNT(*) FROM sach WHERE IsDeleted = FALSE;"') do (
    set "FINAL_BOOKS=%%A"
)

echo -^> So bang hien co : %FINAL_TABLES% / 23
echo -^> So sach hien co : %FINAL_BOOKS% / 221

set "MYSQL_PWD="
set "ROOT_PWD="

if "%FINAL_TABLES%"=="23" (
    if "%FINAL_BOOKS%"=="221" (
        echo ===================================================
        echo [OK] THIET LAP HOAN TAT THANH CONG!
        echo Khoi chay ung dung bang: run.bat hoac java -jar dist\XimiLib.jar
        echo ===================================================
        exit /b 0
    )
)

echo [CANH BAO] Kiem tra tinh toan ven chua dat (ky vong 23 bang va 221 sach).
exit /b 1
