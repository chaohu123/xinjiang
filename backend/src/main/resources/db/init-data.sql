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
-- 4. 调整路线相关表结构以支持长文本
-- =====================================================
ALTER TABLE route_tips
    DROP PRIMARY KEY;
ALTER TABLE route_tips
    MODIFY COLUMN tip TEXT NOT NULL;
ALTER TABLE route_tips
    ADD PRIMARY KEY (route_id, tip(255));

ALTER TABLE itinerary_items
    MODIFY COLUMN accommodation TEXT NULL;

ALTER TABLE itinerary_meals
    DROP PRIMARY KEY;
ALTER TABLE itinerary_meals
    MODIFY COLUMN meal TEXT NOT NULL;
ALTER TABLE itinerary_meals
    ADD PRIMARY KEY (itinerary_item_id, meal(255));
ALTER TABLE itinerary_locations
    MODIFY COLUMN description TEXT NULL;

-- =====================================================
-- 5. 插入路线数据
-- =====================================================
-- 注意：先删除现有路线数据，确保使用新的主题分类
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM route_tips;
DELETE FROM route_resources;
DELETE FROM itinerary_meals;
DELETE FROM itinerary_locations;
DELETE FROM itinerary_items;
DELETE FROM routes;
SET FOREIGN_KEY_CHECKS = 1;
-- 丝绸之路主题 (silkRoad) - 4条
INSERT INTO routes (title, description, cover, theme, duration, distance, start_location, end_location, waypoints, views, favorites, created_at, updated_at) VALUES
                                                                                                                                                                 ('丝绸之路文化之旅', '探索新疆丝绸之路的历史文化，感受千年文明的魅力。从乌鲁木齐出发，途经吐鲁番、库尔勒，最终到达喀什，体验多元文化的交融。', 'https://images.unsplash.com/photo-1518638150340-f706e86654de?w=800', 'silkRoad', 7, 1200.0, '乌鲁木齐', '喀什', 15, 1250, 89, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 30 DAY)),
                                                                                                                                                                 ('古丝绸之路南道探秘', '沿着古丝绸之路南道，从和田出发，穿越塔克拉玛干沙漠边缘，探访千年古城遗址，感受丝路商旅的传奇历史。途经和田、于田、民丰，体验沙漠绿洲的独特魅力。', 'https://images.unsplash.com/photo-1508804185872-d7badad00f7d?w=800', 'silkRoad', 8, 900.0, '和田', '库尔勒', 18, 1680, 125, DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 28 DAY)),
                                                                                                                                                                 ('北丝绸之路文化探索', '从乌鲁木齐出发，沿天山北麓西行，探访昌吉、石河子、奎屯等丝路重镇，了解丝绸之路北道的商贸历史和文化交流。', 'https://images.unsplash.com/photo-1511895426328-dc8714191300?w=800', 'silkRoad', 6, 800.0, '乌鲁木齐', '伊宁', 12, 980, 67, DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 22 DAY)),
                                                                                                                                                                 ('玄奘取经之路追寻', '重走唐代高僧玄奘的西行路线，从哈密到喀什，探访古代佛教遗址，感受宗教文化交流的历史痕迹。', 'https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?w=800', 'silkRoad', 9, 1500.0, '哈密', '喀什', 20, 2340, 178, DATE_SUB(NOW(), INTERVAL 35 DAY), DATE_SUB(NOW(), INTERVAL 35 DAY)),

-- 自然风光主题 (nature) - 6条
                                                                                                                                                                 ('天山天池自然风光之旅', '领略天山天池的壮美景色，感受大自然的鬼斧神工。适合喜欢自然风光的旅行者，包含徒步、摄影等多种体验。', 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800', 'nature', 3, 150.0, '乌鲁木齐', '天山天池', 8, 980, 67, DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
                                                                                                                                                                 ('喀纳斯湖仙境之旅', '探访神秘的喀纳斯湖，欣赏湖光山色，体验图瓦人的独特文化，感受大自然的宁静与美丽。', 'https://images.unsplash.com/photo-1501594907352-04cda38ebc29?w=800', 'nature', 6, 800.0, '乌鲁木齐', '喀纳斯', 14, 2100, 145, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY)),
                                                                                                                                                                 ('吐鲁番火焰山探险', '挑战火焰山的高温，参观葡萄沟、坎儿井等著名景点，了解吐鲁番独特的地理环境和历史文化。', 'https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800', 'nature', 2, 200.0, '吐鲁番', '吐鲁番', 6, 750, 48, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
                                                                                                                                                                 ('塔克拉玛干沙漠穿越', '挑战中国最大的沙漠，体验沙漠越野的刺激，观赏壮观的沙漠日出日落，探访沙漠中的绿洲，感受大漠孤烟直的壮美景象。', 'https://images.unsplash.com/photo-1542407236-85bb69a6d1c1?w=800', 'nature', 5, 1200.0, '库尔勒', '和田', 16, 1890, 156, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY)),
                                                                                                                                                                 ('巴音布鲁克草原之旅', '探访中国第二大草原巴音布鲁克，欣赏九曲十八弯的壮丽景色，观赏天鹅湖的优雅天鹅，体验蒙古族的草原文化。', 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800', 'nature', 4, 600.0, '库尔勒', '巴音布鲁克', 10, 1560, 112, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),
                                                                                                                                                                 ('帕米尔高原风光摄影', '前往世界屋脊帕米尔高原，拍摄慕士塔格峰、公格尔九别峰等雪山美景，体验塔吉克族的独特文化。', 'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800', 'nature', 7, 1000.0, '喀什', '塔什库尔干', 15, 1980, 134, DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY)),

-- 文化体验主题 (culture) - 5条
                                                                                                                                                                 ('维吾尔文化深度体验', '深入了解维吾尔族的文化传统，参观博物馆、体验手工艺制作、品尝地道美食，感受浓郁的民族风情。', 'https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=800', 'culture', 5, 600.0, '乌鲁木齐', '吐鲁番', 12, 1560, 112, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY)),
                                                                                                                                                                 ('哈萨克族文化深度游', '深入体验哈萨克族的游牧文化，观看传统赛马、叼羊比赛，品尝马奶酒和手抓肉，入住哈萨克毡房，感受草原民族的豪迈与热情。', 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800', 'culture', 4, 500.0, '伊犁', '那拉提', 10, 1320, 98, DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 18 DAY)),
                                                                                                                                                                 ('塔吉克族文化探索', '探访帕米尔高原的塔吉克族村落，了解这个高原民族的独特生活方式和文化传统，体验鹰笛、鹰舞等非物质文化遗产。', 'https://images.unsplash.com/photo-1469474968028-56623f02e42e?w=800', 'culture', 3, 300.0, '塔什库尔干', '塔什库尔干', 8, 890, 56, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
                                                                                                                                                                 ('蒙古族草原文化体验', '在巴音布鲁克草原体验蒙古族的游牧文化，学习蒙古族歌舞，品尝奶茶和手抓肉，感受草原民族的热情好客。', 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=800', 'culture', 4, 450.0, '和静', '巴音布鲁克', 9, 1120, 78, DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 14 DAY)),
                                                                                                                                                                 ('回族文化美食之旅', '探访新疆回族聚居区，了解回族的历史文化，品尝地道的回族美食，参观清真寺，体验伊斯兰文化的独特魅力。', 'https://images.unsplash.com/photo-1518638150340-f706e86654de?w=800', 'culture', 3, 350.0, '昌吉', '乌鲁木齐', 7, 980, 65, DATE_SUB(NOW(), INTERVAL 16 DAY), DATE_SUB(NOW(), INTERVAL 16 DAY)),

-- 美食之旅主题 (food) - 5条
                                                                                                                                                                 ('新疆美食探索之旅', '品尝新疆最地道的美食，从大盘鸡到烤羊肉串，从手抓饭到馕，体验舌尖上的新疆。', 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800', 'food', 4, 400.0, '乌鲁木齐', '伊犁', 10, 890, 54, DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY)),
                                                                                                                                                                 ('南疆美食寻味之旅', '从喀什出发，一路品尝南疆最地道的美食。烤包子、拉条子、抓饭、烤全羊、馕坑肉...每一站都是味蕾的盛宴，体验新疆美食的丰富多样和独特风味。', 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=800', 'food', 5, 650.0, '喀什', '阿克苏', 12, 1450, 108, DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY)),
                                                                                                                                                                 ('北疆特色美食之旅', '探索北疆各地的特色美食，从乌鲁木齐的拌面到伊犁的马肠子，从阿勒泰的冷水鱼到塔城的风干肉，体验北疆多元的美食文化。', 'https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=800', 'food', 6, 800.0, '乌鲁木齐', '阿勒泰', 14, 1230, 89, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY)),
                                                                                                                                                                 ('丝绸之路美食文化之旅', '沿着丝绸之路品尝历史悠久的传统美食，了解各种美食的历史渊源和文化背景，体验美食与文化的完美结合。', 'https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=800', 'food', 7, 1000.0, '哈密', '喀什', 16, 1670, 123, DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY)),
                                                                                                                                                                 ('夜市美食探索之旅', '专门探索新疆各地的夜市美食，从乌鲁木齐的五一夜市到喀什的汗巴扎夜市，体验新疆夜市的独特魅力和丰富美食。', 'https://images.unsplash.com/photo-1559329007-40df8a9345d8?w=800', 'food', 4, 300.0, '乌鲁木齐', '喀什', 8, 1340, 96, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY))

ON DUPLICATE KEY UPDATE title = VALUES(title), description = VALUES(description), cover = VALUES(cover), updated_at = NOW();

-- 为第一条路线"丝绸之路文化之旅"添加示例行程数据
-- 注意：这里使用 LAST_INSERT_ID() 获取刚插入的第一条路线的ID
-- 由于我们使用 INSERT ... VALUES 一次性插入多条，需要先获取第一条路线的ID
SET @route1_id = (SELECT id FROM routes WHERE title = '丝绸之路文化之旅' LIMIT 1);

-- 插入行程项
INSERT INTO itinerary_items (route_id, day, title, description, accommodation) VALUES
(@route1_id, 1, '抵达乌鲁木齐', '抵达乌鲁木齐，入住酒店，自由活动。可以前往国际大巴扎感受新疆的市井文化，品尝当地特色小吃。', '乌鲁木齐酒店'),
(@route1_id, 2, '乌鲁木齐 - 吐鲁番', '前往吐鲁番，参观火焰山、葡萄沟，了解吐鲁番独特的地理环境和历史文化。体验维吾尔族传统民居，品尝葡萄干和哈密瓜。', '吐鲁番酒店'),
(@route1_id, 3, '吐鲁番 - 库尔勒', '前往库尔勒，参观库尔勒博物馆，了解丝路历史。游览孔雀河，感受库尔勒的城市魅力。', '库尔勒酒店'),
(@route1_id, 4, '库尔勒 - 轮台', '前往轮台，探访轮台古城遗址，感受古代丝路重镇的历史风貌。参观塔里木河胡杨林，欣赏千年胡杨的壮美。', '轮台酒店'),
(@route1_id, 5, '轮台 - 库车', '前往库车，参观库车大寺和库车王府，了解古代龟兹文化。游览天山神秘大峡谷，感受大自然的鬼斧神工。', '库车酒店'),
(@route1_id, 6, '库车 - 阿克苏', '前往阿克苏，参观温宿大峡谷，欣赏丹霞地貌的壮丽景色。体验阿克苏的苹果文化，品尝当地特色美食。', '阿克苏酒店'),
(@route1_id, 7, '阿克苏 - 喀什', '前往喀什，抵达后参观艾提尕尔清真寺和喀什古城，感受浓郁的维吾尔族文化氛围，结束愉快的丝路之旅。', '喀什酒店')
ON DUPLICATE KEY UPDATE id = id;

-- 获取行程项ID并插入地点信息
SET @item1_id = (SELECT id FROM itinerary_items WHERE route_id = @route1_id AND day = 1 LIMIT 1);
SET @item2_id = (SELECT id FROM itinerary_items WHERE route_id = @route1_id AND day = 2 LIMIT 1);
SET @item3_id = (SELECT id FROM itinerary_items WHERE route_id = @route1_id AND day = 3 LIMIT 1);
SET @item4_id = (SELECT id FROM itinerary_items WHERE route_id = @route1_id AND day = 4 LIMIT 1);
SET @item5_id = (SELECT id FROM itinerary_items WHERE route_id = @route1_id AND day = 5 LIMIT 1);
SET @item6_id = (SELECT id FROM itinerary_items WHERE route_id = @route1_id AND day = 6 LIMIT 1);
SET @item7_id = (SELECT id FROM itinerary_items WHERE route_id = @route1_id AND day = 7 LIMIT 1);

-- 插入行程地点
INSERT INTO itinerary_locations (itinerary_item_id, name, lat, lng, description) VALUES
(@item1_id, '国际大巴扎', 43.8256, 87.6168, '新疆最大的巴扎，体验当地市井文化'),
(@item2_id, '火焰山', 42.9476, 89.1788, '西游记中的火焰山，感受高温奇观'),
(@item2_id, '葡萄沟', 42.9500, 89.1800, '吐鲁番著名的葡萄种植区，品尝新鲜葡萄'),
(@item3_id, '库尔勒博物馆', 41.7592, 86.1369, '了解丝路历史和库尔勒文化'),
(@item3_id, '孔雀河', 41.7600, 86.1400, '库尔勒的母亲河，欣赏城市风光'),
(@item4_id, '轮台古城遗址', 41.7833, 84.2500, '古代丝路重镇，感受历史沧桑'),
(@item4_id, '塔里木河胡杨林', 41.8000, 84.3000, '千年胡杨林，欣赏胡杨的壮美'),
(@item5_id, '库车大寺', 41.7167, 82.9500, '新疆第二大清真寺，了解伊斯兰文化'),
(@item5_id, '天山神秘大峡谷', 41.7500, 83.0000, '丹霞地貌奇观，感受大自然的鬼斧神工'),
(@item6_id, '温宿大峡谷', 41.2667, 80.2333, '壮丽的丹霞地貌，摄影爱好者的天堂'),
(@item7_id, '艾提尕尔清真寺', 39.4677, 75.9938, '新疆最大的清真寺，感受宗教文化'),
(@item7_id, '喀什古城', 39.4700, 75.9950, '千年古城，体验浓郁的维吾尔族文化')
ON DUPLICATE KEY UPDATE itinerary_item_id = itinerary_item_id;

-- 插入行程餐饮
INSERT INTO itinerary_meals (itinerary_item_id, meal) VALUES
(@item1_id, '晚餐'),
(@item2_id, '早餐'),
(@item2_id, '午餐'),
(@item2_id, '晚餐'),
(@item3_id, '早餐'),
(@item3_id, '午餐'),
(@item3_id, '晚餐'),
(@item4_id, '早餐'),
(@item4_id, '午餐'),
(@item4_id, '晚餐'),
(@item5_id, '早餐'),
(@item5_id, '午餐'),
(@item5_id, '晚餐'),
(@item6_id, '早餐'),
(@item6_id, '午餐'),
(@item6_id, '晚餐'),
(@item7_id, '早餐'),
(@item7_id, '午餐')
ON DUPLICATE KEY UPDATE itinerary_item_id = itinerary_item_id;

-- =====================================================
-- 6. 插入社区帖子数据
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
-- 7. 插入评论数据
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
-- 8. 插入收藏数据
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
-- 9. 插入帖子点赞数据
-- =====================================================
INSERT INTO post_likes (user_id, post_id) VALUES
(2, 2),
(2, 3),
(3, 1),
(3, 2),
(3, 4)
ON DUPLICATE KEY UPDATE id = id;

-- =====================================================
-- 10. 插入活动报名数据
-- =====================================================
INSERT INTO event_registrations (user_id, event_id, status) VALUES
(2, 1, 'APPROVED'),
(2, 3, 'APPROVED'),
(3, 1, 'APPROVED'),
(3, 2, 'PENDING'),
(3, 4, 'REJECTED')
ON DUPLICATE KEY UPDATE id = id;
ALTER TABLE event_registrations
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' AFTER event_id;

-- =====================================================
-- 插入非遗数据内容
-- 1. 新疆维吾尔木卡姆艺术
INSERT INTO heritage_items (title, region, category, cover, description, content, video_url, heritage_level, featured, views) VALUES
    ('新疆维吾尔木卡姆艺术', '新疆', '表演艺术', '/covers/xinjiang_muqam.jpg', '维吾尔木卡姆是集歌、舞、乐为一体的大型综合艺术形式，被誉为"东方音乐的明珠"',
     '新疆维吾尔木卡姆艺术是一种广泛流传于新疆各维吾尔族聚居区的各种木卡姆的总称，是集歌、舞、乐为一体的大型综合艺术形式。木卡姆音乐现象分布在中亚、南亚、西亚、北非等19个国家和地区，新疆处于这些国家和地区的最东端。维吾尔木卡姆作为东、西方乐舞文化交流的结晶，记录和印证了不同人群乐舞文化之间相互传播、交融的历史。',
     'https://example.com/videos/xinjiang_muqam.mp4', '世界级', TRUE, 2540);

INSERT INTO heritage_item_images (heritage_id, image_url) VALUES
                                                              (LAST_INSERT_ID(), '/images/muqam1.jpg'),
                                                              (LAST_INSERT_ID(), '/images/muqam2.jpg'),
                                                              (LAST_INSERT_ID(), '/images/muqam3.jpg');

INSERT INTO heritage_item_tags (heritage_id, tag) VALUES
                                                      (LAST_INSERT_ID(), '音乐'), (LAST_INSERT_ID(), '舞蹈'), (LAST_INSERT_ID(), '维吾尔族');

-- 2. 玛纳斯
INSERT INTO heritage_items (title, region, category, cover, description, content, video_url, heritage_level, featured, views) VALUES
    ('玛纳斯', '新疆克孜勒苏柯尔克孜自治州', '口头传统', '/covers/manas.jpg', '柯尔克孜族英雄史诗《玛纳斯》，是世界著名的史诗之一',
     '《玛纳斯》是柯尔克孜族的英雄史诗，与藏族史诗《格萨尔王传》、蒙古族史诗《江格尔》并称中国三大史诗。全诗共分八部，长达23万余行，通过动人的情节和优美的语言，生动描绘了玛纳斯家族八代英雄为维护部落利益而进行艰苦卓绝斗争的故事。',
     'https://example.com/videos/manas.mp4', '国家级', TRUE, 1870);

INSERT INTO heritage_item_images (heritage_id, image_url) VALUES
                                                              (LAST_INSERT_ID(), '/images/manas1.jpg'),
                                                              (LAST_INSERT_ID(), '/images/manas2.jpg');

INSERT INTO heritage_item_tags (heritage_id, tag) VALUES
                                                      (LAST_INSERT_ID(), '史诗'), (LAST_INSERT_ID(), '柯尔克孜族'), (LAST_INSERT_ID(), '口头文学');

-- 3. 维吾尔族模制法土陶烧制技艺
INSERT INTO heritage_items (title, region, category, cover, description, content, video_url, heritage_level, featured, views) VALUES
    ('维吾尔族模制法土陶烧制技艺', '新疆喀什', '传统技艺', '/covers/pottery.jpg', '新疆喀什地区古老的土陶制作技艺，具有鲜明的民族特色',
     '维吾尔族模制法土陶烧制技艺主要流传于新疆喀什地区，已有两千多年的历史。这种技艺采用当地特有的陶土为原料，通过手工模制、彩绘、烧制等工序制作出各种生活用具和工艺品。其造型古朴、色彩鲜艳，具有很高的艺术价值和实用价值。',
     'https://example.com/videos/pottery.mp4', '国家级', FALSE, 920);

INSERT INTO heritage_item_images (heritage_id, image_url) VALUES
                                                              (LAST_INSERT_ID(), '/images/pottery1.jpg'),
                                                              (LAST_INSERT_ID(), '/images/pottery2.jpg'),
                                                              (LAST_INSERT_ID(), '/images/pottery3.jpg');

INSERT INTO heritage_item_tags (heritage_id, tag) VALUES
                                                      (LAST_INSERT_ID(), '陶瓷'), (LAST_INSERT_ID(), '手工艺'), (LAST_INSERT_ID(), '维吾尔族');

-- 4. 哈萨克族毡房营造技艺
INSERT INTO heritage_items (title, region, category, cover, description, content, video_url, heritage_level, featured, views) VALUES
    ('哈萨克族毡房营造技艺', '新疆伊犁', '传统技艺', '/covers/yurt.jpg', '哈萨克族传统的移动民居建造技艺，适应游牧生活',
     '哈萨克族毡房是哈萨克族牧民的传统住房，具有便于拆卸、携带、安装的特点，非常适合游牧生活。毡房的营造技艺包括木架制作、毛毡加工、绳索编织等多个环节，体现了哈萨克族人民的智慧和适应自然的能力。',
     'https://example.com/videos/yurt.mp4', '国家级', FALSE, 1560);

INSERT INTO heritage_item_images (heritage_id, image_url) VALUES
                                                              (LAST_INSERT_ID(), '/images/yurt1.jpg'),
                                                              (LAST_INSERT_ID(), '/images/yurt2.jpg');

INSERT INTO heritage_item_tags (heritage_id, tag) VALUES
                                                      (LAST_INSERT_ID(), '建筑'), (LAST_INSERT_ID(), '哈萨克族'), (LAST_INSERT_ID(), '游牧文化');

-- 5. 塔吉克族鹰舞
INSERT INTO heritage_items (title, region, category, cover, description, content, video_url, heritage_level, featured, views) VALUES
    ('塔吉克族鹰舞', '新疆塔什库尔干', '表演艺术', '/covers/eagle_dance.jpg', '塔吉克族传统舞蹈，模仿雄鹰的动作，展现民族精神',
     '塔吉克族鹰舞是塔吉克族传统的民间舞蹈，舞者通过模仿雄鹰的各种动作和姿态，展现塔吉克族人民勇敢、豪迈的民族性格。舞蹈节奏鲜明，动作刚劲有力，具有很高的艺术观赏价值和文化研究价值。',
     'https://example.com/videos/eagle_dance.mp4', '自治区级', FALSE, 680);

INSERT INTO heritage_item_images (heritage_id, image_url) VALUES
                                                              (LAST_INSERT_ID(), '/images/eagle_dance1.jpg'),
                                                              (LAST_INSERT_ID(), '/images/eagle_dance2.jpg');

INSERT INTO heritage_item_tags (heritage_id, tag) VALUES
                                                      (LAST_INSERT_ID(), '舞蹈'), (LAST_INSERT_ID(), '塔吉克族'), (LAST_INSERT_ID(), '民间艺术');

-- 6. 艾德莱斯绸织染技艺
INSERT INTO heritage_items (title, region, category, cover, description, content, video_url, heritage_level, featured, views) VALUES
    ('艾德莱斯绸织染技艺', '新疆和田', '传统技艺', '/covers/atlas_silk.jpg', '新疆特有的丝绸织造技艺，以色彩鲜艳、图案独特著称',
     '艾德莱斯绸是新疆特有的传统丝绸织品，主要产于和田、喀什等地。其织造工艺复杂，采用古老的扎经染色法，图案具有抽象、浪漫的特点，色彩对比强烈，富有浓郁的民族风格。艾德莱斯绸被誉为"二十一世纪最后的丝绸手工艺"。',
     'https://example.com/videos/atlas_silk.mp4', '国家级', TRUE, 2130);

INSERT INTO heritage_item_images (heritage_id, image_url) VALUES
                                                              (LAST_INSERT_ID(), '/images/atlas_silk1.jpg'),
                                                              (LAST_INSERT_ID(), '/images/atlas_silk2.jpg'),
                                                              (LAST_INSERT_ID(), '/images/atlas_silk3.jpg');

INSERT INTO heritage_item_tags (heritage_id, tag) VALUES
                                                      (LAST_INSERT_ID(), '纺织'), (LAST_INSERT_ID(), '丝绸'), (LAST_INSERT_ID(), '维吾尔族');
-- =====================================================
-- 完成
-- =====================================================


