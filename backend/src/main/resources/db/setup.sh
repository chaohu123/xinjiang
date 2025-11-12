#!/bin/bash

# =====================================================
# 新疆数字文化平台数据库快速安装脚本
# 数据库: MySQL
# =====================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量
DB_NAME="cultural_xinjiang"
DB_USER="root"
DB_HOST="localhost"
DB_PORT="3306"

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${GREEN}=====================================================${NC}"
echo -e "${GREEN}新疆数字文化平台数据库安装脚本${NC}"
echo -e "${GREEN}=====================================================${NC}"
echo ""

# 检查 MySQL 是否安装
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}错误: 未找到 mysql 命令，请先安装 MySQL${NC}"
    exit 1
fi

# 提示输入数据库密码
echo -e "${YELLOW}请输入 MySQL 密码:${NC}"
read -s DBPASSWORD
export MYSQL_PWD="$DBPASSWORD"

# 创建数据库
echo -e "${GREEN}创建数据库: ${DB_NAME}${NC}"
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || echo "数据库创建失败或已存在，继续执行..."

# 执行 schema.sql
echo -e "${GREEN}执行数据库结构创建脚本...${NC}"
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER $DB_NAME < "$SCRIPT_DIR/schema.sql"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ 数据库结构创建成功${NC}"
else
    echo -e "${RED}✗ 数据库结构创建失败${NC}"
    exit 1
fi

# 询问是否初始化测试数据
echo ""
echo -e "${YELLOW}是否初始化测试数据? (y/n)${NC}"
read -r INIT_DATA

if [ "$INIT_DATA" = "y" ] || [ "$INIT_DATA" = "Y" ]; then
    echo -e "${GREEN}执行测试数据初始化脚本...${NC}"
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER $DB_NAME < "$SCRIPT_DIR/init-data.sql"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ 测试数据初始化成功${NC}"
    else
        echo -e "${RED}✗ 测试数据初始化失败${NC}"
        exit 1
    fi
fi

# 验证数据库
echo ""
echo -e "${GREEN}验证数据库...${NC}"
TABLE_COUNT=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER $DB_NAME -s -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME';")
echo -e "${GREEN}✓ 数据库表数量: $TABLE_COUNT${NC}"

USER_COUNT=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER $DB_NAME -s -N -e "SELECT COUNT(*) FROM users;")
echo -e "${GREEN}✓ 用户数量: $USER_COUNT${NC}"

echo ""
echo -e "${GREEN}=====================================================${NC}"
echo -e "${GREEN}数据库安装完成！${NC}"
echo -e "${GREEN}=====================================================${NC}"
echo ""
echo -e "${YELLOW}测试账号:${NC}"
echo -e "  用户名: admin"
echo -e "  密码: admin123"
echo -e "  角色: ADMIN"
echo ""
echo -e "${YELLOW}数据库连接信息:${NC}"
echo -e "  主机: $DB_HOST"
echo -e "  端口: $DB_PORT"
echo -e "  数据库: $DB_NAME"
echo -e "  用户: $DB_USER"
echo ""

# 清理环境变量
unset MYSQL_PWD


