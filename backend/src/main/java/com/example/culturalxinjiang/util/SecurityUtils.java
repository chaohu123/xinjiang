package com.example.culturalxinjiang.util;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * 安全相关的工具类，封装当前登录用户的获取逻辑
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户的用户名
     */
    public static Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.ofNullable(authentication.getName());
    }

    /**
     * 获取当前登录用户的用户名，如果未登录则抛出异常
     */
    public static String getRequiredUsername() {
        return getCurrentUsername()
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("未登录或登录已过期"));
    }
}
















