package com.example.culturalxinjiang.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 生成密码哈希值的简单工具
 * 直接运行此工具生成一个新的、已验证的 BCrypt 哈希值
 */
public class GeneratePasswordHash {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        
        // 生成新的哈希值
        String hash = encoder.encode(password);
        
        // 验证
        boolean matches = encoder.matches(password, hash);
        
        System.out.println("=========================================");
        System.out.println("密码哈希生成工具");
        System.out.println("=========================================");
        System.out.println("明文密码: " + password);
        System.out.println("BCrypt 哈希值: " + hash);
        System.out.println("验证结果: " + (matches ? "✓ 成功" : "✗ 失败"));
        System.out.println();
        System.out.println("SQL 更新语句（复制执行）:");
        System.out.println("USE cultural_xinjiang;");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE username = 'admin';");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE username = 'user1';");
        System.out.println("UPDATE users SET password = '" + hash + "' WHERE username = 'user2';");
        System.out.println("=========================================");
        System.out.println();
        System.out.println("⚠️ 重要: 执行 SQL 后必须重启应用！");
    }
}




