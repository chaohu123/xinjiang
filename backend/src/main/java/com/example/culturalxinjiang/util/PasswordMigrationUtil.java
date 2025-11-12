package com.example.culturalxinjiang.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码迁移工具类
 * 用于将明文密码转换为 BCrypt 加密密码
 */
public class PasswordMigrationUtil {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成 "123456" 的 BCrypt 哈希值
        String password = "123456";
        String hashedPassword = encoder.encode(password);
        
        System.out.println("=========================================");
        System.out.println("密码迁移工具");
        System.out.println("=========================================");
        System.out.println("明文密码: " + password);
        System.out.println("BCrypt 哈希值: " + hashedPassword);
        System.out.println();
        System.out.println("SQL 更新语句:");
        System.out.println("UPDATE users SET password = '" + hashedPassword + "' WHERE password = '123456';");
        System.out.println();
        System.out.println("或者更新特定用户:");
        System.out.println("UPDATE users SET password = '" + hashedPassword + "' WHERE username = 'admin';");
        System.out.println("UPDATE users SET password = '" + hashedPassword + "' WHERE username = 'user1';");
        System.out.println("UPDATE users SET password = '" + hashedPassword + "' WHERE username = 'user2';");
        System.out.println("=========================================");
        
        // 验证生成的哈希值
        boolean matches = encoder.matches(password, hashedPassword);
        System.out.println("验证结果: " + (matches ? "✓ 成功" : "✗ 失败"));
    }
}




