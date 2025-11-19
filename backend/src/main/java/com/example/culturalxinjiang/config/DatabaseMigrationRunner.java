package com.example.culturalxinjiang.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 轻量级数据库补丁：确保 home_recommendations.source 支持文化遗产来源
 * 避免老数据库因 ENUM 未更新导致写入失败。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigrationRunner implements CommandLineRunner {

    private static final String TABLE_NAME = "home_recommendations";
    private static final String COLUMN_NAME = "source";
    private static final String ENUM_SQL =
            "ENUM('CULTURE_RESOURCE', 'COMMUNITY_POST', 'HERITAGE_ITEM')";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            String columnType = jdbcTemplate.queryForObject(
                    "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    String.class,
                    TABLE_NAME,
                    COLUMN_NAME
            );

            if (columnType != null && !columnType.contains("HERITAGE_ITEM")) {
                log.info("Updating {}.{} enum definition to include HERITAGE_ITEM", TABLE_NAME, COLUMN_NAME);
                jdbcTemplate.execute(
                        "ALTER TABLE " + TABLE_NAME + " MODIFY COLUMN " + COLUMN_NAME + " " + ENUM_SQL + " NOT NULL"
                );
                log.info("Column {}.{} updated successfully.", TABLE_NAME, COLUMN_NAME);
            }
        } catch (Exception ex) {
            log.warn("Failed to verify/update {}.{} column definition: {}", TABLE_NAME, COLUMN_NAME, ex.getMessage());
        }
    }
}

