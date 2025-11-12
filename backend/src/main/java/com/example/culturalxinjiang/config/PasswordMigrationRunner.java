package com.example.culturalxinjiang.config;

import com.example.culturalxinjiang.entity.User;
import com.example.culturalxinjiang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 密码迁移组件
 * 在应用启动时自动检查并修复明文密码
 * 
 * 注意：已禁用，因为系统现在使用明文密码存储
 * 如需启用，请取消下面的 @Component 注解注释
 */
@Slf4j
// @Component  // 已禁用：系统现在使用明文密码，不需要自动迁移
@RequiredArgsConstructor
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("开始检查密码迁移...");
        
        // 查找所有密码不是 BCrypt 格式的用户（BCrypt 哈希值以 $2a$ 开头）
        List<User> usersWithPlainPassword = userRepository.findAll().stream()
                .filter(user -> user.getPassword() != null && !user.getPassword().startsWith("$2a$"))
                .toList();

        if (usersWithPlainPassword.isEmpty()) {
            log.info("✓ 所有用户密码都已正确加密，无需迁移");
            return;
        }

        log.warn("发现 {} 个用户的密码是明文格式，开始迁移...", usersWithPlainPassword.size());

        int migratedCount = 0;
        for (User user : usersWithPlainPassword) {
            String plainPassword = user.getPassword();
            String hashedPassword = passwordEncoder.encode(plainPassword);
            user.setPassword(hashedPassword);
            userRepository.save(user);
            migratedCount++;
            log.info("✓ 已迁移用户: {} (用户名: {})", user.getId(), user.getUsername());
        }

        log.info("密码迁移完成，共迁移 {} 个用户", migratedCount);
    }
}

