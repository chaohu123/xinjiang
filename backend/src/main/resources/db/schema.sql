-- =====================================================
-- 新疆数字文化平台数据库创建脚本
-- 数据库: MySQL
-- 版本: 1.0.0
-- =====================================================

-- 创建数据库（如果需要）
-- CREATE DATABASE IF NOT EXISTS cultural_xinjiang;
-- USE cultural_xinjiang;

USE cultural_xinjiang;

-- =====================================================
-- 创建索引的存储过程（如果索引不存在则创建）
-- =====================================================
DELIMITER $$
DROP PROCEDURE IF EXISTS create_index_if_not_exists$$
CREATE PROCEDURE create_index_if_not_exists(
    IN p_table_name VARCHAR(128),
    IN p_index_name VARCHAR(128),
    IN p_index_definition TEXT
)
BEGIN
    DECLARE index_count INT DEFAULT 0;

    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
    AND table_name = p_table_name
    AND index_name = p_index_name;

    IF index_count = 0 THEN
        SET @sql = CONCAT('CREATE INDEX ', p_index_name, ' ON ', p_table_name, ' ', p_index_definition);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- =====================================================
-- 1. 用户表 (users)
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    avatar VARCHAR(500),
    nickname VARCHAR(100),
    bio TEXT,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_role CHECK (role IN ('USER', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建用户表索引
CALL create_index_if_not_exists('users', 'idx_users_username', '(username)');
CALL create_index_if_not_exists('users', 'idx_users_email', '(email)');
CALL create_index_if_not_exists('users', 'idx_users_role', '(role)');

-- =====================================================
-- 2. 文化资源表 (culture_resources)
-- =====================================================
CREATE TABLE IF NOT EXISTS culture_resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cover VARCHAR(500),
    video_url VARCHAR(500),
    audio_url VARCHAR(500),
    content TEXT,
    region VARCHAR(100) NOT NULL,
    location_lat DOUBLE,
    location_lng DOUBLE,
    location_address VARCHAR(500),
    views INTEGER NOT NULL DEFAULT 0,
    favorites INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_culture_type CHECK (type IN ('ARTICLE', 'EXHIBIT', 'VIDEO', 'AUDIO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文化资源表';

-- 创建文化资源表索引
CALL create_index_if_not_exists('culture_resources', 'idx_culture_resources_type', '(type)');
CALL create_index_if_not_exists('culture_resources', 'idx_culture_resources_region', '(region)');
CALL create_index_if_not_exists('culture_resources', 'idx_culture_resources_views', '(views DESC)');
CALL create_index_if_not_exists('culture_resources', 'idx_culture_resources_favorites', '(favorites DESC)');
CALL create_index_if_not_exists('culture_resources', 'idx_culture_resources_created_at', '(created_at DESC)');
-- MySQL 全文搜索索引（可选，需要 MyISAM 或 InnoDB 5.6+）
-- CREATE FULLTEXT INDEX idx_culture_resources_title ON culture_resources(title);

-- =====================================================
-- 3. 文化资源图片表 (culture_resource_images)
-- =====================================================
CREATE TABLE IF NOT EXISTS culture_resource_images (
    resource_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (resource_id, image_url),
    CONSTRAINT fk_culture_resource_images_resource
        FOREIGN KEY (resource_id)
        REFERENCES culture_resources(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文化资源图片表';

-- =====================================================
-- 4. 文化资源标签表 (culture_resource_tags)
-- =====================================================
CREATE TABLE IF NOT EXISTS culture_resource_tags (
    resource_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (resource_id, tag),
    CONSTRAINT fk_culture_resource_tags_resource
        FOREIGN KEY (resource_id)
        REFERENCES culture_resources(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文化资源标签表';

-- 创建标签索引
CALL create_index_if_not_exists('culture_resource_tags', 'idx_culture_resource_tags_tag', '(tag)');

-- =====================================================
-- 5. 非遗内容表 (heritage_items)
-- =====================================================
CREATE TABLE IF NOT EXISTS heritage_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    region VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL,
    cover VARCHAR(500),
    description TEXT,
    content LONGTEXT,
    video_url VARCHAR(500),
    heritage_level VARCHAR(50) NOT NULL,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    views INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='非遗内容表';

-- =====================================================
-- 5.1 非遗图片表 (heritage_item_images)
-- =====================================================
CREATE TABLE IF NOT EXISTS heritage_item_images (
    heritage_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (heritage_id, image_url),
    CONSTRAINT fk_heritage_images_item
        FOREIGN KEY (heritage_id)
        REFERENCES heritage_items(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='非遗图片表';

-- =====================================================
-- 5.2 非遗标签表 (heritage_item_tags)
-- =====================================================
CREATE TABLE IF NOT EXISTS heritage_item_tags (
    heritage_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (heritage_id, tag),
    CONSTRAINT fk_heritage_tags_item
        FOREIGN KEY (heritage_id)
        REFERENCES heritage_items(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='非遗标签表';

CALL create_index_if_not_exists('heritage_items', 'idx_heritage_region', '(region)');
CALL create_index_if_not_exists('heritage_items', 'idx_heritage_category', '(category)');
CALL create_index_if_not_exists('heritage_items', 'idx_heritage_level', '(heritage_level)');
CALL create_index_if_not_exists('heritage_items', 'idx_heritage_views', '(views DESC)');
CALL create_index_if_not_exists('heritage_item_tags', 'idx_heritage_tags_tag', '(tag)');

-- =====================================================
-- 5. 活动表 (events)
-- =====================================================
CREATE TABLE IF NOT EXISTS events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cover VARCHAR(500),
    type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    location_name VARCHAR(255),
    location_address VARCHAR(500),
    location_lat DOUBLE,
    location_lng DOUBLE,
    capacity INTEGER,
    registered INTEGER NOT NULL DEFAULT 0,
    price DOUBLE,
    status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
    content TEXT,
    organizer_name VARCHAR(255),
    organizer_contact VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_event_type CHECK (type IN ('EXHIBITION', 'PERFORMANCE', 'WORKSHOP', 'TOUR')),
    CONSTRAINT chk_event_status CHECK (status IN ('UPCOMING', 'ONGOING', 'PAST')),
    CONSTRAINT chk_event_dates CHECK (end_date >= start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动表';

-- 创建活动表索引
CALL create_index_if_not_exists('events', 'idx_events_type', '(type)');
CALL create_index_if_not_exists('events', 'idx_events_status', '(status)');
CALL create_index_if_not_exists('events', 'idx_events_start_date', '(start_date)');
CALL create_index_if_not_exists('events', 'idx_events_end_date', '(end_date)');
CALL create_index_if_not_exists('events', 'idx_events_dates', '(start_date, end_date)');

-- =====================================================
-- 6. 活动图片表 (event_images)
-- =====================================================
CREATE TABLE IF NOT EXISTS event_images (
    event_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (event_id, image_url),
    CONSTRAINT fk_event_images_event
        FOREIGN KEY (event_id)
        REFERENCES events(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动图片表';

-- =====================================================
-- 7. 活动视频表 (event_videos)
-- =====================================================
CREATE TABLE IF NOT EXISTS event_videos (
    event_id BIGINT NOT NULL,
    video_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (event_id, video_url),
    CONSTRAINT fk_event_videos_event
        FOREIGN KEY (event_id)
        REFERENCES events(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动视频表';

-- =====================================================
-- 8. 活动日程表 (event_schedules)
-- =====================================================
CREATE TABLE IF NOT EXISTS event_schedules (
    event_id BIGINT NOT NULL,
    time VARCHAR(50) NOT NULL,
    activity VARCHAR(255) NOT NULL,
    PRIMARY KEY (event_id, time, activity),
    CONSTRAINT fk_event_schedules_event
        FOREIGN KEY (event_id)
        REFERENCES events(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动日程表';

-- =====================================================
-- 9. 活动要求表 (event_requirements)
-- =====================================================
CREATE TABLE IF NOT EXISTS event_requirements (
    event_id BIGINT NOT NULL,
    requirement VARCHAR(255) NOT NULL,
    PRIMARY KEY (event_id, requirement),
    CONSTRAINT fk_event_requirements_event
        FOREIGN KEY (event_id)
        REFERENCES events(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动要求表';

-- =====================================================
-- 10. 活动报名表 (event_registrations)
-- =====================================================
CREATE TABLE IF NOT EXISTS event_registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    remark VARCHAR(500),
    processed_by BIGINT NULL,
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE (user_id, event_id),
    CONSTRAINT fk_event_registrations_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_event_registrations_event
        FOREIGN KEY (event_id)
        REFERENCES events(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_event_registrations_processed_user
        FOREIGN KEY (processed_by)
        REFERENCES users(id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- 创建活动报名表索引
CALL create_index_if_not_exists('event_registrations', 'idx_event_registrations_user', '(user_id)');
CALL create_index_if_not_exists('event_registrations', 'idx_event_registrations_event', '(event_id)');

-- =====================================================
-- 10. 路线表 (routes)
-- =====================================================
CREATE TABLE IF NOT EXISTS routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cover VARCHAR(500),
    theme VARCHAR(100) NOT NULL,
    duration INTEGER NOT NULL,
    distance DOUBLE NOT NULL,
    start_location VARCHAR(255) NOT NULL,
    end_location VARCHAR(255) NOT NULL,
    waypoints INTEGER NOT NULL DEFAULT 0,
    views INTEGER NOT NULL DEFAULT 0,
    favorites INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='路线表';

-- 创建路线表索引
CALL create_index_if_not_exists('routes', 'idx_routes_theme', '(theme)');
CALL create_index_if_not_exists('routes', 'idx_routes_views', '(views DESC)');
CALL create_index_if_not_exists('routes', 'idx_routes_favorites', '(favorites DESC)');
CALL create_index_if_not_exists('routes', 'idx_routes_created_at', '(created_at DESC)');

-- =====================================================
-- 11. 路线行程项表 (itinerary_items)
-- =====================================================
CREATE TABLE IF NOT EXISTS itinerary_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL,
    day INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    accommodation VARCHAR(255),
    CONSTRAINT fk_itinerary_items_route
        FOREIGN KEY (route_id)
        REFERENCES routes(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='路线行程项表';

-- 创建行程项索引
CALL create_index_if_not_exists('itinerary_items', 'idx_itinerary_items_route', '(route_id)');
CALL create_index_if_not_exists('itinerary_items', 'idx_itinerary_items_day', '(route_id, day)');

-- =====================================================
-- 12. 行程地点表 (itinerary_locations)
-- =====================================================
CREATE TABLE IF NOT EXISTS itinerary_locations (
    itinerary_item_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    lat DOUBLE NOT NULL,
    lng DOUBLE NOT NULL,
    description VARCHAR(500),
    PRIMARY KEY (itinerary_item_id, name, lat, lng),
    CONSTRAINT fk_itinerary_locations_item
        FOREIGN KEY (itinerary_item_id)
        REFERENCES itinerary_items(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行程地点表';

-- =====================================================
-- 13. 行程餐饮表 (itinerary_meals)
-- =====================================================
CREATE TABLE IF NOT EXISTS itinerary_meals (
    itinerary_item_id BIGINT NOT NULL,
    meal VARCHAR(255) NOT NULL,
    PRIMARY KEY (itinerary_item_id, meal),
    CONSTRAINT fk_itinerary_meals_item
        FOREIGN KEY (itinerary_item_id)
        REFERENCES itinerary_items(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行程餐饮表';

-- =====================================================
-- 14. 路线资源关联表 (route_resources)
-- =====================================================
CREATE TABLE IF NOT EXISTS route_resources (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL,
    culture_resource_id BIGINT NOT NULL,
    `order` INTEGER NOT NULL,
    CONSTRAINT fk_route_resources_route
        FOREIGN KEY (route_id)
        REFERENCES routes(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_route_resources_resource
        FOREIGN KEY (culture_resource_id)
        REFERENCES culture_resources(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='路线资源关联表';

-- 创建路线资源索引
CALL create_index_if_not_exists('route_resources', 'idx_route_resources_route', '(route_id)');
CALL create_index_if_not_exists('route_resources', 'idx_route_resources_resource', '(culture_resource_id)');
CALL create_index_if_not_exists('route_resources', 'idx_route_resources_order', '(route_id, `order`)');

-- =====================================================
-- 15. 路线提示表 (route_tips)
-- =====================================================
CREATE TABLE IF NOT EXISTS route_tips (
    route_id BIGINT NOT NULL,
    tip VARCHAR(500) NOT NULL,
    PRIMARY KEY (route_id, tip),
    CONSTRAINT fk_route_tips_route
        FOREIGN KEY (route_id)
        REFERENCES routes(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='路线提示表';

-- =====================================================
-- 16. 社区帖子表 (community_posts)
-- =====================================================
CREATE TABLE IF NOT EXISTS community_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    likes INTEGER NOT NULL DEFAULT 0,
    comments INTEGER NOT NULL DEFAULT 0,
    views INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_community_posts_author
        FOREIGN KEY (author_id)
        REFERENCES users(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区帖子表';

-- 创建社区帖子表索引
CALL create_index_if_not_exists('community_posts', 'idx_community_posts_author', '(author_id)');
CALL create_index_if_not_exists('community_posts', 'idx_community_posts_likes', '(likes DESC)');
CALL create_index_if_not_exists('community_posts', 'idx_community_posts_comments', '(comments DESC)');
CALL create_index_if_not_exists('community_posts', 'idx_community_posts_created_at', '(created_at DESC)');
CALL create_index_if_not_exists('community_posts', 'idx_community_posts_status', '(status)');
-- MySQL 全文搜索索引（可选，需要 InnoDB 5.6+）
-- CREATE FULLTEXT INDEX idx_community_posts_title ON community_posts(title);

-- =====================================================
-- 17. 帖子图片表 (post_images)
-- =====================================================
CREATE TABLE IF NOT EXISTS post_images (
    post_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (post_id, image_url),
    CONSTRAINT fk_post_images_post
        FOREIGN KEY (post_id)
        REFERENCES community_posts(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子图片表';

-- =====================================================
-- 18. 帖子标签表 (post_tags)
-- =====================================================
CREATE TABLE IF NOT EXISTS post_tags (
    post_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (post_id, tag),
    CONSTRAINT fk_post_tags_post
        FOREIGN KEY (post_id)
        REFERENCES community_posts(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签表';

-- 创建帖子标签索引
CALL create_index_if_not_exists('post_tags', 'idx_post_tags_tag', '(tag)');

-- =====================================================
-- 19. 评论表 (comments)
-- =====================================================
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    parent_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_comments_post
        FOREIGN KEY (post_id)
        REFERENCES community_posts(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent
        FOREIGN KEY (parent_id)
        REFERENCES comments(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 创建评论表索引
CALL create_index_if_not_exists('comments', 'idx_comments_author', '(author_id)');
CALL create_index_if_not_exists('comments', 'idx_comments_post', '(post_id)');
CALL create_index_if_not_exists('comments', 'idx_comments_parent', '(parent_id)');
CALL create_index_if_not_exists('comments', 'idx_comments_created_at', '(post_id, created_at)');

-- =====================================================
-- 20. 帖子点赞表 (post_likes)
-- =====================================================
CREATE TABLE IF NOT EXISTS post_likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, post_id),
    CONSTRAINT fk_post_likes_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_post_likes_post
        FOREIGN KEY (post_id)
        REFERENCES community_posts(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子点赞表';

-- 创建帖子点赞索引
CALL create_index_if_not_exists('post_likes', 'idx_post_likes_user', '(user_id)');
CALL create_index_if_not_exists('post_likes', 'idx_post_likes_post', '(post_id)');

CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resource_type VARCHAR(20) NOT NULL,
    resource_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, resource_type, resource_id),
    CONSTRAINT fk_favorites_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_favorite_resource_type CHECK (resource_type IN ('CULTURE', 'ROUTE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 创建收藏表索引
CALL create_index_if_not_exists('favorites', 'idx_favorites_user', '(user_id)');
CALL create_index_if_not_exists('favorites', 'idx_favorites_resource', '(resource_type, resource_id)');
CALL create_index_if_not_exists('favorites', 'idx_favorites_created_at', '(user_id, created_at DESC)');

-- =====================================================
-- 22. 首页推荐表 (home_recommendations)
-- =====================================================
CREATE TABLE IF NOT EXISTS home_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type ENUM('FEATURED', 'HOT') NOT NULL,
    resource_id BIGINT NOT NULL,
    source ENUM('CULTURE_RESOURCE', 'COMMUNITY_POST', 'HERITAGE_ITEM') NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页推荐配置表';

-- 为首页推荐表创建索引
CALL create_index_if_not_exists('home_recommendations', 'idx_home_recommendations_type_order', '(type, display_order)');
CALL create_index_if_not_exists('home_recommendations', 'idx_home_recommendations_enabled', '(enabled)');

-- 兼容已有环境，更新 source 枚举，确保包含文化遗产
ALTER TABLE home_recommendations
    MODIFY COLUMN source ENUM('CULTURE_RESOURCE', 'COMMUNITY_POST', 'HERITAGE_ITEM') NOT NULL;

-- =====================================================
-- 创建更新时间触发器
-- =====================================================
-- 注意: MySQL 使用 ON UPDATE CURRENT_TIMESTAMP 自动更新 updated_at 字段
-- 以下触发器仅在需要更复杂的更新逻辑时使用
-- 由于我们已经使用了 ON UPDATE CURRENT_TIMESTAMP，这些触发器可以省略

-- 如果需要使用触发器，可以使用以下语法（MySQL 5.7+）:
/*
DELIMITER $$
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$
DELIMITER ;
*/

-- =====================================================
-- 初始化数据（可选）
-- =====================================================

-- 创建默认管理员用户（密码: admin123，需要在实际使用时修改）
-- INSERT INTO users (username, email, password, role, enabled)
-- VALUES ('admin', 'admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', TRUE)
-- ON DUPLICATE KEY UPDATE username = username;
ALTER TABLE favorites MODIFY COLUMN resource_type VARCHAR(20) NOT NULL;
ALTER TABLE favorites DROP CHECK chk_favorite_resource_type;
ALTER TABLE favorites ADD CONSTRAINT chk_favorite_resource_type CHECK (resource_type IN ('CULTURE', 'ROUTE', 'POST'));
-- =====================================================
-- 清理临时存储过程
-- =====================================================
DROP PROCEDURE IF EXISTS create_index_if_not_exists;

-- =====================================================
-- 完成
-- =====================================================
-- 注意: MySQL 中的表注释已在表定义中使用 COMMENT 关键字添加
-- 数据库注释需要在创建数据库时添加:
-- CREATE DATABASE cultural_xinjiang CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '新疆数字文化平台数据库';

