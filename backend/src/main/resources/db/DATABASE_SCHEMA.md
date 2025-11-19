# 数据库结构文档

## 📊 数据库概览

**数据库名称**: `cultural_xinjiang`
**数据库类型**: MySQL
**字符集**: utf8mb4
**排序规则**: utf8mb4_unicode_ci
**时区**: 服务器时区

## 📋 表结构说明

### 1. 用户表 (users)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PRIMARY KEY | 用户ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| email | VARCHAR(255) | UNIQUE, NOT NULL | 邮箱 |
| password | VARCHAR(255) | NOT NULL | 密码（BCrypt加密） |
| phone | VARCHAR(20) | | 手机号 |
| avatar | VARCHAR(500) | | 头像URL |
| nickname | VARCHAR(100) | | 昵称 |
| bio | TEXT | | 个人简介 |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'USER' | 角色（USER/ADMIN） |
| enabled | BOOLEAN | NOT NULL, DEFAULT true | 是否启用 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

**索引**:
- `idx_users_username` - 用户名索引
- `idx_users_email` - 邮箱索引
- `idx_users_role` - 角色索引

---

### 2. 文化资源表 (culture_resources)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PRIMARY KEY | 资源ID |
| type | VARCHAR(20) | NOT NULL | 类型（ARTICLE/EXHIBIT/VIDEO/AUDIO） |
| title | VARCHAR(255) | NOT NULL | 标题 |
| description | TEXT | | 描述 |
| cover | VARCHAR(500) | | 封面图URL |
| video_url | VARCHAR(500) | | 视频URL |
| audio_url | VARCHAR(500) | | 音频URL |
| content | TEXT | | 内容 |
| region | VARCHAR(100) | NOT NULL | 地区 |
| location_lat | DOUBLE | | 纬度 |
| location_lng | DOUBLE | | 经度 |
| location_address | VARCHAR(500) | | 地址 |
| views | INTEGER | NOT NULL, DEFAULT 0 | 浏览量 |
| favorites | INTEGER | NOT NULL, DEFAULT 0 | 收藏数 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

**索引**:
- `idx_culture_resources_type` - 类型索引
- `idx_culture_resources_region` - 地区索引
- `idx_culture_resources_views` - 浏览量索引（降序）
- `idx_culture_resources_favorites` - 收藏数索引（降序）
- `idx_culture_resources_created_at` - 创建时间索引（降序）
- `idx_culture_resources_title` - 标题全文搜索索引

**关联表**:
- `culture_resource_images` - 资源图片
- `culture_resource_tags` - 资源标签

---

### 3. 活动表 (events)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PRIMARY KEY | 活动ID |
| title | VARCHAR(255) | NOT NULL | 标题 |
| description | TEXT | | 描述 |
| cover | VARCHAR(500) | | 封面图URL |
| type | VARCHAR(20) | NOT NULL | 类型（EXHIBITION/PERFORMANCE/WORKSHOP/TOUR） |
| start_date | DATE | NOT NULL | 开始日期 |
| end_date | DATE | NOT NULL | 结束日期 |
| location_name | VARCHAR(255) | | 地点名称 |
| location_address | VARCHAR(500) | | 地点地址 |
| location_lat | DOUBLE | | 纬度 |
| location_lng | DOUBLE | | 经度 |
| capacity | INTEGER | | 容量 |
| registered | INTEGER | NOT NULL, DEFAULT 0 | 已报名数 |
| price | DOUBLE | | 价格 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'UPCOMING' | 状态（UPCOMING/ONGOING/PAST） |
| content | TEXT | | 内容 |
| organizer_name | VARCHAR(255) | | 组织者名称 |
| organizer_contact | VARCHAR(255) | | 组织者联系方式 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

**索引**:
- `idx_events_type` - 类型索引
- `idx_events_status` - 状态索引
- `idx_events_start_date` - 开始日期索引
- `idx_events_end_date` - 结束日期索引
- `idx_events_dates` - 日期组合索引

**关联表**:
- `event_images` - 活动图片
- `event_videos` - 活动视频
- `event_schedules` - 活动日程
- `event_requirements` - 活动要求
- `event_registrations` - 活动报名

---

### 4. 路线表 (routes)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PRIMARY KEY | 路线ID |
| title | VARCHAR(255) | NOT NULL | 标题 |
| description | TEXT | | 描述 |
| cover | VARCHAR(500) | | 封面图URL |
| theme | VARCHAR(100) | NOT NULL | 主题 |
| duration | INTEGER | NOT NULL | 天数 |
| distance | DOUBLE | NOT NULL | 距离（公里） |
| start_location | VARCHAR(255) | NOT NULL | 起点 |
| end_location | VARCHAR(255) | NOT NULL | 终点 |
| waypoints | INTEGER | NOT NULL, DEFAULT 0 | 途经点数量 |
| views | INTEGER | NOT NULL, DEFAULT 0 | 浏览量 |
| favorites | INTEGER | NOT NULL, DEFAULT 0 | 收藏数 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

**索引**:
- `idx_routes_theme` - 主题索引
- `idx_routes_views` - 浏览量索引（降序）
- `idx_routes_favorites` - 收藏数索引（降序）
- `idx_routes_created_at` - 创建时间索引（降序）

**关联表**:
- `itinerary_items` - 行程项
- `route_resources` - 路线资源关联
- `route_tips` - 路线提示

---

### 5. 社区帖子表 (community_posts)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PRIMARY KEY | 帖子ID |
| title | VARCHAR(255) | NOT NULL | 标题 |
| content | TEXT | NOT NULL | 内容 |
| author_id | BIGINT | NOT NULL | 作者ID（外键） |
| likes | INTEGER | NOT NULL, DEFAULT 0 | 点赞数 |
| comments | INTEGER | NOT NULL, DEFAULT 0 | 评论数 |
| views | INTEGER | NOT NULL, DEFAULT 0 | 浏览量 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

**索引**:
- `idx_community_posts_author` - 作者索引
- `idx_community_posts_likes` - 点赞数索引（降序）
- `idx_community_posts_comments` - 评论数索引（降序）
- `idx_community_posts_created_at` - 创建时间索引（降序）
- `idx_community_posts_title` - 标题全文搜索索引

**外键**:
- `author_id` -> `users.id`

**关联表**:
- `post_images` - 帖子图片
- `post_tags` - 帖子标签
- `comments` - 评论
- `post_likes` - 帖子点赞

---

### 6. 评论表 (comments)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PRIMARY KEY | 评论ID |
| content | TEXT | NOT NULL | 内容 |
| author_id | BIGINT | NOT NULL | 作者ID（外键） |
| post_id | BIGINT | NOT NULL | 帖子ID（外键） |
| parent_id | BIGINT | | 父评论ID（外键，支持回复） |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

**索引**:
- `idx_comments_author` - 作者索引
- `idx_comments_post` - 帖子索引
- `idx_comments_parent` - 父评论索引
- `idx_comments_created_at` - 创建时间索引

**外键**:
- `author_id` -> `users.id`
- `post_id` -> `community_posts.id`
- `parent_id` -> `comments.id`

---

### 7. 收藏表 (favorites)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PRIMARY KEY | 收藏ID |
| user_id | BIGINT | NOT NULL | 用户ID（外键） |
| resource_type | VARCHAR(20) | NOT NULL | 资源类型（CULTURE/ROUTE） |
| resource_id | BIGINT | NOT NULL | 资源ID |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |

**索引**:
- `idx_favorites_user` - 用户索引
- `idx_favorites_resource` - 资源索引
- `idx_favorites_created_at` - 创建时间索引

**唯一约束**:
- `(user_id, resource_type, resource_id)` - 防止重复收藏

**外键**:
- `user_id` -> `users.id`

---

### 8. 首页推荐表 (home_recommendations)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PRIMARY KEY | 推荐配置ID |
| type | ENUM('FEATURED','HOT') | NOT NULL | 推荐类型 |
| resource_id | BIGINT | NOT NULL | 资源ID（根据 source 指向不同表） |
| source | ENUM('CULTURE_RESOURCE','COMMUNITY_POST','HERITAGE_ITEM') | NOT NULL | 资源来源 |
| display_order | INTEGER | NOT NULL, DEFAULT 0 | 显示顺序 |
| enabled | BOOLEAN | NOT NULL, DEFAULT TRUE | 是否启用 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

**索引**:
- `idx_home_recommendations_type_order` - 类型与排序索引
- `idx_home_recommendations_enabled` - 启用状态索引

---

### 9. 活动报名表 (event_registrations)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PRIMARY KEY | 报名ID |
| user_id | BIGINT | NOT NULL | 用户ID（外键） |
| event_id | BIGINT | NOT NULL | 活动ID（外键） |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | 报名状态（PENDING/APPROVED/REJECTED） |
| remark | VARCHAR(500) |  | 审核备注 |
| processed_by | BIGINT |  | 审核人（外键，管理员） |
| processed_at | TIMESTAMP |  | 审核时间 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| updated_at | TIMESTAMP | NOT NULL | 更新时间 |

**索引**:
- `idx_event_registrations_user` - 用户索引
- `idx_event_registrations_event` - 活动索引

**唯一约束**:
- `(user_id, event_id)` - 防止重复报名

**外键**:
- `user_id` -> `users.id`
- `event_id` -> `events.id`
- `processed_by` -> `users.id` (审核人，可为空)

---

### 10. 帖子点赞表 (post_likes)

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT AUTO_INCREMENT | PRIMARY KEY | 点赞ID |
| user_id | BIGINT | NOT NULL | 用户ID（外键） |
| post_id | BIGINT | NOT NULL | 帖子ID（外键） |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |

**索引**:
- `idx_post_likes_user` - 用户索引
- `idx_post_likes_post` - 帖子索引

**唯一约束**:
- `(user_id, post_id)` - 防止重复点赞

**外键**:
- `user_id` -> `users.id`
- `post_id` -> `community_posts.id`

---

## 🔗 表关系图

```
users
  ├── community_posts (author_id)
  ├── comments (author_id)
  ├── favorites (user_id)
  ├── event_registrations (user_id)
  └── post_likes (user_id)

culture_resources
  ├── culture_resource_images
  ├── culture_resource_tags
  └── route_resources (culture_resource_id)

events
  ├── event_images
  ├── event_videos
  ├── event_schedules
  ├── event_requirements
  └── event_registrations (event_id)

routes
  ├── itinerary_items (route_id)
  │   ├── itinerary_locations
  │   └── itinerary_meals
  ├── route_resources (route_id)
  └── route_tips

community_posts
  ├── post_images
  ├── post_tags
  ├── comments (post_id)
  └── post_likes (post_id)

comments
  └── comments (parent_id) [自引用]
```

## 🔄 自动更新时间戳

所有表都配置了自动更新 `updated_at` 字段的功能：
- 使用 `ON UPDATE CURRENT_TIMESTAMP` 自动更新 `updated_at` 字段
- `created_at` 字段使用 `DEFAULT CURRENT_TIMESTAMP` 自动设置创建时间
- MySQL 5.7+ 支持在同一个表的不同字段上使用不同的时间戳默认值和更新行为

## 📊 数据统计

预计数据量：
- 用户表: 1,000 - 10,000 条
- 文化资源表: 1,000 - 5,000 条
- 活动表: 100 - 500 条
- 路线表: 50 - 200 条
- 社区帖子表: 5,000 - 50,000 条
- 评论表: 10,000 - 100,000 条

## 🔒 安全建议

1. **密码加密**: 所有密码使用 BCrypt 加密存储
2. **SQL注入防护**: 使用参数化查询
3. **权限控制**: 使用数据库用户权限控制
4. **数据备份**: 定期备份数据库
5. **审计日志**: 记录重要操作日志

## 🚀 性能优化

1. **索引优化**: 为常用查询字段创建索引
2. **分区表**: 对于大数据量表考虑分区
3. **缓存策略**: 使用 Redis 缓存热点数据
4. **查询优化**: 使用 EXPLAIN 分析查询计划
5. **连接池**: 配置合适的数据库连接池

## 📝 注意事项

1. **时区**: 确保数据库和应用使用相同的时区
2. **字符集**: 使用 utf8mb4 字符集支持多语言（包括 emoji）
3. **外键约束**: 确保外键关系正确，MySQL 默认使用 InnoDB 引擎支持外键
4. **级联删除**: 注意级联删除的影响，MySQL 会自动处理外键级联
5. **数据迁移**: 使用 Flyway 或 Liquibase 管理数据库版本
6. **存储引擎**: 使用 InnoDB 引擎支持事务和外键约束
7. **自增主键**: MySQL 使用 AUTO_INCREMENT 实现自增主键


