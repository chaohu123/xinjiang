package com.example.culturalxinjiang.security;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 明文密码编码器
 * 直接进行字符串比较，不进行加密
 * 注意：仅用于开发环境，生产环境应使用BCrypt等加密方式
 */
public class PlainTextPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        // 直接返回明文，不进行加密
        return rawPassword.toString();
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        // 直接进行字符串比较
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return rawPassword.toString().equals(encodedPassword);
    }
}




