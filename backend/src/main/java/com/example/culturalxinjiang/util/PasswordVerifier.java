package com.example.culturalxinjiang.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码验证工具类
 * 用于验证 BCrypt 哈希值是否正确
 */
public class PasswordVerifier {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 测试密码
        String plainPassword = "123456";
        
        // SQL 脚本中使用的哈希值
        String[] testHashes = {
            "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi",
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
        };
        
        System.out.println("=========================================");
        System.out.println("密码验证工具");
        System.out.println("=========================================");
        System.out.println("测试密码: " + plainPassword);
        System.out.println();
        
        // 测试现有的哈希值
        for (int i = 0; i < testHashes.length; i++) {
            String hash = testHashes[i];
            boolean matches = encoder.matches(plainPassword, hash);
            System.out.println("哈希值 " + (i + 1) + ": " + (matches ? "✓ 正确" : "✗ 错误"));
            System.out.println("  " + hash);
        }
        
        System.out.println();
        System.out.println("生成新的哈希值:");
        String newHash = encoder.encode(plainPassword);
        System.out.println(newHash);
        boolean newMatches = encoder.matches(plainPassword, newHash);
        System.out.println("验证结果: " + (newMatches ? "✓ 成功" : "✗ 失败"));
        
        System.out.println();
        System.out.println("SQL 更新语句:");
        System.out.println("UPDATE users SET password = '" + newHash + "' WHERE username = 'admin';");
        System.out.println("UPDATE users SET password = '" + newHash + "' WHERE username = 'user1';");
        System.out.println("UPDATE users SET password = '" + newHash + "' WHERE username = 'user2';");
        System.out.println("=========================================");
    }
}




