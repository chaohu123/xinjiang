-- =====================================================
-- 新疆数字文化平台数据库删除脚本
-- 数据库: MySQL
-- 警告: 此脚本将删除所有表和数据，请谨慎使用！
-- =====================================================

-- 禁用外键检查（MySQL）
SET FOREIGN_KEY_CHECKS = 0;

-- 删除表（按依赖关系顺序删除）
-- 注意: MySQL 不需要 CASCADE，会自动处理外键约束
DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS post_likes;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS post_tags;
DROP TABLE IF EXISTS post_images;
DROP TABLE IF EXISTS community_posts;
DROP TABLE IF EXISTS route_tips;
DROP TABLE IF EXISTS route_resources;
DROP TABLE IF EXISTS itinerary_meals;
DROP TABLE IF EXISTS itinerary_locations;
DROP TABLE IF EXISTS itinerary_items;
DROP TABLE IF EXISTS routes;
DROP TABLE IF EXISTS event_requirements;
DROP TABLE IF EXISTS event_schedules;
DROP TABLE IF EXISTS event_images;
DROP TABLE IF EXISTS event_videos;
DROP TABLE IF EXISTS event_registrations;
DROP TABLE IF EXISTS events;
DROP TABLE IF EXISTS culture_resource_tags;
DROP TABLE IF EXISTS culture_resource_images;
DROP TABLE IF EXISTS culture_resources;
DROP TABLE IF EXISTS users;

-- 删除存储过程
DROP PROCEDURE IF EXISTS create_index_if_not_exists;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 完成
-- =====================================================


