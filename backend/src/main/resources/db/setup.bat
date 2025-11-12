@echo off
REM =====================================================
REM 新疆数字文化平台数据库快速安装脚本 (Windows)
REM 数据库: MySQL
REM =====================================================

setlocal enabledelayedexpansion

echo =====================================================
echo 新疆数字文化平台数据库安装脚本
echo =====================================================
echo.

REM 配置变量
set DB_NAME=cultural_xinjiang
set DB_USER=root
set DB_HOST=localhost
set DB_PORT=3306

REM 脚本目录
set SCRIPT_DIR=%~dp0

REM 检查 MySQL 是否安装
where mysql >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo 错误: 未找到 mysql 命令，请先安装 MySQL
    exit /b 1
)

REM 提示输入数据库密码
set /p DBPASSWORD=请输入 MySQL 密码: 

REM 创建数据库
echo 创建数据库: %DB_NAME%
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DBPASSWORD% -e "CREATE DATABASE IF NOT EXISTS %DB_NAME% CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo 数据库创建失败或已存在，继续执行...
)

REM 执行 schema.sql
echo 执行数据库结构创建脚本...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DBPASSWORD% %DB_NAME% < "%SCRIPT_DIR%schema.sql"
if %ERRORLEVEL% EQU 0 (
    echo [OK] 数据库结构创建成功
) else (
    echo [ERROR] 数据库结构创建失败
    exit /b 1
)

REM 询问是否初始化测试数据
echo.
set /p INIT_DATA=是否初始化测试数据? (y/n): 

if /i "%INIT_DATA%"=="y" (
    echo 执行测试数据初始化脚本...
    mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DBPASSWORD% %DB_NAME% < "%SCRIPT_DIR%init-data.sql"
    if %ERRORLEVEL% EQU 0 (
        echo [OK] 测试数据初始化成功
    ) else (
        echo [ERROR] 测试数据初始化失败
        exit /b 1
    )
)

REM 验证数据库
echo.
echo 验证数据库...
for /f "tokens=*" %%i in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DBPASSWORD% %DB_NAME% -s -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '%DB_NAME%';"') do set TABLE_COUNT=%%i
echo [OK] 数据库表数量: %TABLE_COUNT%

for /f "tokens=*" %%i in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DBPASSWORD% %DB_NAME% -s -N -e "SELECT COUNT(*) FROM users;"') do set USER_COUNT=%%i
echo [OK] 用户数量: %USER_COUNT%

echo.
echo =====================================================
echo 数据库安装完成！
echo =====================================================
echo.
echo 测试账号:
echo   用户名: admin
echo   密码: admin123
echo   角色: ADMIN
echo.
echo 数据库连接信息:
echo   主机: %DB_HOST%
echo   端口: %DB_PORT%
echo   数据库: %DB_NAME%
echo   用户: %DB_USER%
echo.

endlocal


