-- =====================================================
-- 新疆数字文化平台数据库初始化数据脚本
-- 注意: 此脚本包含测试数据，仅用于开发和测试环境
-- =====================================================

-- 清理现有数据（可选，谨慎使用）
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE favorites;
-- TRUNCATE TABLE post_likes;
-- TRUNCATE TABLE comments;
-- TRUNCATE TABLE post_tags;
-- TRUNCATE TABLE post_images;
-- TRUNCATE TABLE community_posts;
-- TRUNCATE TABLE route_tips;
-- TRUNCATE TABLE route_resources;
-- TRUNCATE TABLE itinerary_meals;
-- TRUNCATE TABLE itinerary_locations;
-- TRUNCATE TABLE itinerary_items;
-- TRUNCATE TABLE routes;
-- TRUNCATE TABLE event_requirements;
-- TRUNCATE TABLE event_schedules;
-- TRUNCATE TABLE event_images;
-- TRUNCATE TABLE event_registrations;
-- TRUNCATE TABLE events;
-- TRUNCATE TABLE culture_resource_tags;
-- TRUNCATE TABLE culture_resource_images;
-- TRUNCATE TABLE culture_resources;
-- TRUNCATE TABLE users;
-- SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 1. 插入测试用户
-- =====================================================
INSERT INTO users (username, email, password, phone, nickname, role, enabled) VALUES
                                                                                  ('admin', 'admin@xinjiang.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800138000', '管理员', 'ADMIN', true),
                                                                                  ('user1', 'user1@xinjiang.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800138001', '张三', 'USER', true),
                                                                                  ('user2', 'user2@xinjiang.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13800138002', '李四', 'USER', true)
ON DUPLICATE KEY UPDATE username = username;

-- 注意: 默认密码都是 'admin123'，需要在实际使用时修改

-- =====================================================
-- 2. 插入文化资源数据
-- =====================================================
INSERT INTO culture_resources (type, title, description, cover, region, location_lat, location_lng, location_address, views, favorites) VALUES
('ARTICLE', '新疆维吾尔族传统音乐', '新疆维吾尔族音乐是中华民族音乐宝库中的瑰宝，具有悠久的历史和独特的风格。', 'https://example.com/images/music.jpg', '乌鲁木齐', 43.8256, 87.6168, '新疆维吾尔自治区乌鲁木齐市', 1250, 89),
('EXHIBIT', '丝绸之路文物展', '展示丝绸之路上的珍贵文物，展现古代东西方文化交流的辉煌历史。', 'https://example.com/images/silkroad.jpg', '吐鲁番', 42.9476, 89.1788, '新疆维吾尔自治区吐鲁番市', 2340, 156),
('VIDEO', '天山天池风光', '天山天池是新疆著名的自然景观，湖水清澈，山峦叠翠，风景如画。', 'https://example.com/images/tianchi.jpg', '昌吉', 43.8833, 88.1333, '新疆维吾尔自治区昌吉回族自治州', 5670, 234),
('AUDIO', '哈萨克族传统民歌', '哈萨克族传统民歌旋律优美，歌词富有民族特色，是哈萨克文化的重要组成部分。', 'https://example.com/images/kazakh.jpg', '伊犁', 43.9169, 81.3179, '新疆维吾尔自治区伊犁哈萨克自治州', 890, 67),
('ARTICLE', '喀什古城的历史文化', '喀什古城拥有2000多年的历史，是新疆最具代表性的历史文化名城之一。', 'https://example.com/images/kashi.jpg', '喀什', 39.4677, 75.9938, '新疆维吾尔自治区喀什地区', 1890, 145),
('EXHIBIT', '和田玉文化展', '和田玉被誉为中国四大名玉之一，具有深厚的历史文化内涵和艺术价值。', 'https://example.com/images/hetianyu.jpg', '和田', 37.1142, 79.9155, '新疆维吾尔自治区和田地区', 3120, 198)
ON DUPLICATE KEY UPDATE id = id;

-- 插入文化资源图片
INSERT INTO culture_resource_images (resource_id, image_url) VALUES
(1, 'https://example.com/images/music1.jpg'),
(1, 'https://example.com/images/music2.jpg'),
(2, 'https://example.com/images/silkroad1.jpg'),
(2, 'https://example.com/images/silkroad2.jpg'),
(2, 'https://example.com/images/silkroad3.jpg'),
(3, 'https://example.com/images/tianchi1.jpg'),
(3, 'https://example.com/images/tianchi2.jpg'),
(5, 'https://example.com/images/kashi1.jpg'),
(5, 'https://example.com/images/kashi2.jpg'),
(6, 'https://example.com/images/hetianyu1.jpg'),
(6, 'https://example.com/images/hetianyu2.jpg')
ON DUPLICATE KEY UPDATE resource_id = resource_id;

-- 插入文化资源标签
INSERT INTO culture_resource_tags (resource_id, tag) VALUES
(1, '音乐'),
(1, '维吾尔族'),
(1, '传统'),
(2, '文物'),
(2, '丝绸之路'),
(2, '历史'),
(3, '自然风光'),
(3, '天山'),
(3, '旅游'),
(4, '音乐'),
(4, '哈萨克族'),
(4, '民歌'),
(5, '历史文化'),
(5, '古城'),
(5, '喀什'),
(6, '玉石'),
(6, '和田'),
(6, '文化')
ON DUPLICATE KEY UPDATE resource_id = resource_id;

-- =====================================================
-- 3. 插入活动数据
-- =====================================================
INSERT INTO events (title, description, cover, type, start_date, end_date, location_name, location_address, location_lat, location_lng, capacity, registered, price, status, organizer_name, organizer_contact) VALUES
('新疆民族文化节', '为期一周的新疆民族文化节，展示各民族的传统文化、歌舞表演、手工艺品等。', 'https://example.com/images/festival.jpg', 'EXHIBITION', '2024-06-01', '2024-06-07', '新疆国际会展中心', '新疆维吾尔自治区乌鲁木齐市', 43.8256, 87.6168, 5000, 1234, 0, 'UPCOMING', '新疆文化厅', '0991-12345678'),
('维吾尔族传统歌舞表演', '精彩的维吾尔族传统歌舞表演，展现浓郁的民族风情。', 'https://example.com/images/dance.jpg', 'PERFORMANCE', '2024-05-15', '2024-05-15', '新疆大剧院', '新疆维吾尔自治区乌鲁木齐市', 43.8256, 87.6168, 800, 456, 50, 'UPCOMING', '新疆大剧院', '0991-87654321'),
('传统手工艺体验工坊', '学习制作维吾尔族传统手工艺品，体验民族文化的魅力。', 'https://example.com/images/workshop.jpg', 'WORKSHOP', '2024-05-20', '2024-05-20', '文化创意园', '新疆维吾尔自治区乌鲁木齐市', 43.8256, 87.6168, 30, 18, 100, 'UPCOMING', '文化创意园', '0991-11223344'),
('天山天池一日游', '游览天山天池，欣赏美丽的自然风光，体验新疆的自然之美。', 'https://example.com/images/tour.jpg', 'TOUR', '2024-05-25', '2024-05-25', '天山天池', '新疆维吾尔自治区昌吉回族自治州', 43.8833, 88.1333, 50, 32, 200, 'UPCOMING', '新疆旅游公司', '0991-55667788')
ON DUPLICATE KEY UPDATE id = id;

-- 插入活动图片
INSERT INTO event_images (event_id, image_url) VALUES
(1, 'https://example.com/images/festival1.jpg'),
(1, 'https://example.com/images/festival2.jpg'),
(2, 'https://example.com/images/dance1.jpg'),
(2, 'https://example.com/images/dance2.jpg'),
(3, 'https://example.com/images/workshop1.jpg'),
(4, 'https://example.com/images/tour1.jpg'),
(4, 'https://example.com/images/tour2.jpg')
ON DUPLICATE KEY UPDATE event_id = event_id;

-- 插入活动日程
INSERT INTO event_schedules (event_id, time, activity) VALUES
(1, '09:00', '开幕式'),
(1, '10:00', '民族歌舞表演'),
(1, '14:00', '手工艺品展览'),
(1, '16:00', '传统美食体验'),
(2, '19:00', '维吾尔族传统歌舞表演'),
(3, '10:00', '手工艺制作教学'),
(3, '14:00', '作品展示与交流'),
(4, '08:00', '集合出发'),
(4, '10:00', '到达天山天池'),
(4, '12:00', '午餐'),
(4, '14:00', '自由游览'),
(4, '17:00', '返程')
ON DUPLICATE KEY UPDATE event_id = event_id;

-- 插入活动要求
INSERT INTO event_requirements (event_id, requirement) VALUES
(1, '携带身份证'),
(1, '遵守活动秩序'),
(2, '提前30分钟入场'),
(3, '自备材料费用'),
(4, '身体健康'),
(4, '穿着舒适的运动鞋'),
(4, '携带防晒用品')
ON DUPLICATE KEY UPDATE event_id = event_id;

-- =====================================================
-- 4. 插入路线数据
-- =====================================================
INSERT INTO routes (title, description, cover, theme, duration, distance, start_location, end_location, waypoints, views, favorites) VALUES
('新疆经典7日游', '游览新疆最著名的景点，包括天山天池、喀纳斯湖、吐鲁番等，体验新疆的自然风光和民族文化。', 'https://example.com/images/route1.jpg', '自然风光', 7, 2500, '乌鲁木齐', '乌鲁木齐', 8, 2340, 189),
('丝绸之路文化之旅', '沿着古丝绸之路，探访历史名城，了解东西方文化交流的历史。', 'https://example.com/images/route2.jpg', '历史文化', 5, 1800, '乌鲁木齐', '喀什', 6, 1890, 156),
('天山天池深度游', '深度游览天山天池及周边景点，体验新疆的自然之美。', 'https://example.com/images/route3.jpg', '自然风光', 3, 500, '乌鲁木齐', '昌吉', 4, 1560, 123)
ON DUPLICATE KEY UPDATE id = id;

-- 插入路线行程项
INSERT INTO itinerary_items (route_id, day, title, description, accommodation) VALUES
(1, 1, '抵达乌鲁木齐', '抵达乌鲁木齐，入住酒店，自由活动。', '乌鲁木齐酒店'),
(1, 2, '天山天池', '游览天山天池，欣赏美丽的自然风光。', '天山天池附近酒店'),
(1, 3, '前往喀纳斯', '乘车前往喀纳斯，沿途欣赏风景。', '喀纳斯酒店'),
(2, 1, '乌鲁木齐-吐鲁番', '前往吐鲁番，参观火焰山、葡萄沟。', '吐鲁番酒店'),
(2, 2, '吐鲁番-库尔勒', '前往库尔勒，参观库尔勒博物馆。', '库尔勒酒店'),
(3, 1, '前往天山天池', '前往天山天池，开始游览。', '天山天池附近酒店'),
(3, 2, '天山天池深度游', '全天游览天山天池及周边景点。', '天山天池附近酒店')
ON DUPLICATE KEY UPDATE id = id;

-- 插入行程地点
INSERT INTO itinerary_locations (itinerary_item_id, name, lat, lng, description) VALUES
(2, '天山天池', 43.8833, 88.1333, '天山天池主景区'),
(2, '天池观景台', 43.8900, 88.1400, '最佳观景位置'),
(4, '火焰山', 42.9476, 89.1788, '西游记中的火焰山'),
(4, '葡萄沟', 42.9500, 89.1800, '吐鲁番著名的葡萄种植区'),
(6, '天山天池', 43.8833, 88.1333, '天山天池主景区'),
(7, '天山天池', 43.8833, 88.1333, '天山天池主景区'),
(7, '天池观景台', 43.8900, 88.1400, '最佳观景位置')
ON DUPLICATE KEY UPDATE itinerary_item_id = itinerary_item_id;

-- 插入行程餐饮
INSERT INTO itinerary_meals (itinerary_item_id, meal) VALUES
(1, '晚餐'),
(2, '早餐'),
(2, '午餐'),
(2, '晚餐'),
(3, '早餐'),
(4, '午餐'),
(4, '晚餐'),
(6, '午餐'),
(7, '早餐'),
(7, '午餐')
ON DUPLICATE KEY UPDATE itinerary_item_id = itinerary_item_id;

-- 插入路线资源关联
INSERT INTO route_resources (route_id, culture_resource_id, `order`) VALUES
(1, 3, 1),
(1, 5, 2),
(2, 2, 1),
(2, 5, 2),
(3, 3, 1)
ON DUPLICATE KEY UPDATE id = id;

-- 插入路线提示
INSERT INTO route_tips (route_id, tip) VALUES
(1, '建议提前预订酒店'),
(1, '注意防晒，携带防晒用品'),
(1, '尊重当地民族风俗'),
(2, '注意天气变化，携带适当衣物'),
(2, '保护文物，不要触摸展品'),
(3, '穿着舒适的运动鞋'),
(3, '携带相机记录美景')
ON DUPLICATE KEY UPDATE route_id = route_id;

-- =====================================================
-- 5. 插入社区帖子数据
-- =====================================================
INSERT INTO community_posts (title, content, author_id, likes, comments, views) VALUES
('天山天池美景分享', '今天去了天山天池，风景真的太美了！湖水清澈见底，山峦叠翠，简直是人间仙境。强烈推荐大家去游玩！', 2, 45, 12, 234),
('新疆美食推荐', '来新疆一定要尝尝这些美食：大盘鸡、手抓饭、羊肉串、馕...每一道都是经典！', 3, 67, 23, 456),
('喀什古城游记', '喀什古城有着2000多年的历史，漫步在古城中，仿佛穿越回了古代。这里的建筑、文化都很有特色。', 2, 34, 8, 189),
('维吾尔族音乐欣赏', '今天听了一场维吾尔族传统音乐表演，旋律优美，节奏明快，非常有民族特色。', 3, 28, 15, 167)
ON DUPLICATE KEY UPDATE id = id;

-- 插入帖子图片
INSERT INTO post_images (post_id, image_url) VALUES
(1, 'https://example.com/images/post1.jpg'),
(1, 'https://example.com/images/post2.jpg'),
(2, 'https://example.com/images/food1.jpg'),
(3, 'https://example.com/images/kashi_post1.jpg'),
(4, 'https://example.com/images/music_post1.jpg')
ON DUPLICATE KEY UPDATE post_id = post_id;

-- 插入帖子标签
INSERT INTO post_tags (post_id, tag) VALUES
(1, '旅游'),
(1, '天山'),
(1, '自然风光'),
(2, '美食'),
(2, '新疆'),
(3, '旅游'),
(3, '历史文化'),
(3, '喀什'),
(4, '音乐'),
(4, '维吾尔族')
ON DUPLICATE KEY UPDATE post_id = post_id;

-- =====================================================
-- 6. 插入评论数据
-- =====================================================
INSERT INTO comments (content, author_id, post_id, parent_id) VALUES
('真的很美！我也想去看看。', 3, 1, NULL),
('照片拍得真好！', 2, 1, NULL),
('谢谢分享，已经加入行程了！', 3, 1, 1),
('这些美食我都吃过，确实很好吃！', 2, 2, NULL),
('我最喜欢吃大盘鸡！', 3, 2, 4),
('喀什古城确实很有特色，值得一去。', 3, 3, NULL)
ON DUPLICATE KEY UPDATE id = id;

-- =====================================================
-- 7. 插入收藏数据
-- =====================================================
INSERT INTO favorites (user_id, resource_type, resource_id) VALUES
(2, 'CULTURE', 1),
(2, 'CULTURE', 3),
(2, 'ROUTE', 1),
(3, 'CULTURE', 2),
(3, 'CULTURE', 5),
(3, 'ROUTE', 2)
ON DUPLICATE KEY UPDATE id = id;

-- =====================================================
-- 8. 插入帖子点赞数据
-- =====================================================
INSERT INTO post_likes (user_id, post_id) VALUES
(2, 2),
(2, 3),
(3, 1),
(3, 2),
(3, 4)
ON DUPLICATE KEY UPDATE id = id;

-- =====================================================
-- 9. 插入活动报名数据
-- =====================================================
INSERT INTO event_registrations (user_id, event_id) VALUES
(2, 1),
(2, 3),
(3, 1),
(3, 2),
(3, 4)
ON DUPLICATE KEY UPDATE id = id;

-- =====================================================
-- 完成
-- =====================================================


