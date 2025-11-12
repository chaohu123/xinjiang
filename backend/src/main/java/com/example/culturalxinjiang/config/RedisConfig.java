package com.example.culturalxinjiang.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Redis 配置类
 * 如果没有使用 Redis，可以通过设置 spring.autoconfigure.exclude 来排除 Redis 自动配置
 * 或者在开发环境配置中排除：org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
 */
@Configuration
@ConditionalOnProperty(name = "spring.data.redis.host")
public class RedisConfig {
    // Redis 配置可以在这里添加
    // 如果项目中不使用 Redis，可以在 application-dev.yml 中排除 Redis 自动配置
}





