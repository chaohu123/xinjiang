# 数据库脚本使用说明

本目录包含新疆数字文化平台数据库的创建、初始化和删除脚本。

## 📁 文件说明

- `schema.sql` - 数据库结构创建脚本（表、索引、触发器、约束等）
- `init-data.sql` - 初始化测试数据脚本
- `drop-schema.sql` - 删除所有表和数据的脚本（谨慎使用）

## 🚀 快速开始

### 1. 创建数据库

```bash
# 连接到 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE cultural_xinjiang CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 使用数据库
USE cultural_xinjiang;
```

### 2. 执行数据库结构创建脚本

```bash
# 方式1: 使用 mysql 命令行
mysql -u root -p cultural_xinjiang < schema.sql

# 方式2: 在 mysql 中执行
mysql -u root -p
USE cultural_xinjiang;
SOURCE schema.sql;
```

### 3. 初始化测试数据（可选）

```bash
# 方式1: 使用 mysql 命令行
mysql -u root -p cultural_xinjiang < init-data.sql

# 方式2: 在 mysql 中执行
mysql -u root -p
USE cultural_xinjiang;
SOURCE init-data.sql;
```

### 4. 验证数据库

```sql
-- 查看所有表
SHOW TABLES;

-- 查看用户数量
SELECT COUNT(*) FROM users;

-- 查看文化资源数量
SELECT COUNT(*) FROM culture_resources;

-- 查看活动数量
SELECT COUNT(*) FROM events;
```

## 📊 数据库结构

### 主要表

1. **users** - 用户表
2. **culture_resources** - 文化资源表
3. **events** - 活动表
4. **routes** - 路线表
5. **community_posts** - 社区帖子表
6. **comments** - 评论表
7. **favorites** - 收藏表
8. **event_registrations** - 活动报名表
9. **post_likes** - 帖子点赞表

### 关联表

- `culture_resource_images` - 文化资源图片
- `culture_resource_tags` - 文化资源标签
- `event_images` - 活动图片
- `event_schedules` - 活动日程
- `event_requirements` - 活动要求
- `itinerary_items` - 路线行程项
- `itinerary_locations` - 行程地点
- `itinerary_meals` - 行程餐饮
- `route_resources` - 路线资源关联
- `route_tips` - 路线提示
- `post_images` - 帖子图片
- `post_tags` - 帖子标签

## 🔧 使用 Docker

### 使用 docker-compose

```bash
# 启动 MySQL 容器
docker-compose up -d db

# 等待数据库启动后，执行脚本
docker exec -i cultural_xinjiang_db mysql -u root -p cultural_xinjiang < schema.sql
docker exec -i cultural_xinjiang_db mysql -u root -p cultural_xinjiang < init-data.sql
```

## 🗑️ 清理数据库

### 删除所有数据（保留表结构）

```sql
-- 在 mysql 中执行
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE favorites;
TRUNCATE TABLE post_likes;
TRUNCATE TABLE comments;
TRUNCATE TABLE post_tags;
TRUNCATE TABLE post_images;
TRUNCATE TABLE community_posts;
TRUNCATE TABLE route_tips;
TRUNCATE TABLE route_resources;
TRUNCATE TABLE itinerary_meals;
TRUNCATE TABLE itinerary_locations;
TRUNCATE TABLE itinerary_items;
TRUNCATE TABLE routes;
TRUNCATE TABLE event_requirements;
TRUNCATE TABLE event_schedules;
TRUNCATE TABLE event_images;
TRUNCATE TABLE event_registrations;
TRUNCATE TABLE events;
TRUNCATE TABLE culture_resource_tags;
TRUNCATE TABLE culture_resource_images;
TRUNCATE TABLE culture_resources;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;
```

### 删除所有表和数据

```bash
# 警告: 这将删除所有数据！
mysql -u root -p cultural_xinjiang < drop-schema.sql
```

## 📝 测试账号

初始化脚本会创建以下测试账号：

| 用户名 | 密码 | 角色 | 邮箱 |
|--------|------|------|------|
| admin | admin123 | ADMIN | admin@xinjiang.com |
| user1 | admin123 | USER | user1@xinjiang.com |
| user2 | admin123 | USER | user2@xinjiang.com |

**注意**: 默认密码是 `admin123`，在生产环境中必须修改！

## 🔐 密码说明

所有测试用户的密码都是 `admin123`，对应的 BCrypt 哈希值为：
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

## 📋 索引说明

数据库脚本创建了以下索引以优化查询性能：

- 用户表：username, email, role
- 文化资源表：type, region, views, favorites, created_at, title (全文搜索)
- 活动表：type, status, start_date, end_date
- 路线表：theme, views, favorites, created_at
- 社区帖子表：author_id, likes, comments, created_at, title (全文搜索)
- 评论表：author_id, post_id, parent_id, created_at
- 收藏表：user_id, resource_type, resource_id, created_at
- 活动报名表：user_id, event_id
- 帖子点赞表：user_id, post_id

## 🔄 触发器说明

数据库脚本创建了自动更新 `updated_at` 字段的触发器，当记录更新时自动更新时间戳。

## 🐛 故障排查

### 问题1: 表已存在错误

如果表已存在，可以：
1. 先执行 `drop-schema.sql` 删除所有表
2. 然后重新执行 `schema.sql`

### 问题2: 外键约束错误

确保按照正确的顺序创建表，或者先创建所有表，再创建外键约束。

### 问题3: 权限错误

确保数据库用户有创建表、索引和触发器的权限：

```sql
GRANT ALL PRIVILEGES ON cultural_xinjiang.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

## 📚 相关文档

- [MySQL 官方文档](https://dev.mysql.com/doc/)
- [Spring Data JPA 文档](https://spring.io/projects/spring-data-jpa)

## ⚠️ 注意事项

1. **生产环境**: 不要在生产环境中使用初始化数据脚本
2. **密码安全**: 生产环境必须修改默认密码
3. **数据备份**: 在执行删除脚本前，请确保已备份重要数据
4. **索引维护**: 定期检查和优化索引性能
5. **数据迁移**: 使用 Flyway 或 Liquibase 进行数据库版本管理

## 🔄 版本管理

建议使用 Flyway 或 Liquibase 进行数据库版本管理，而不是直接执行 SQL 脚本。

### 使用 Flyway

1. 将 SQL 脚本放在 `src/main/resources/db/migration/` 目录下
2. 按照 Flyway 命名规范命名文件：`V1__Create_schema.sql`
3. 启动应用时，Flyway 会自动执行迁移脚本

## 📞 支持

如有问题，请联系项目维护者。


