package com.example.oldcaresystem.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用户上下文工具类
 */
public class UserContext {

    /**
     * 获取当前登录用户详情
     */
    public static JwtUserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof JwtUserDetails) {
            return (JwtUserDetails) authentication.getDetails();
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getUserId() {
        JwtUserDetails details = getUserDetails();
        return details != null ? details.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getUsername() {
        JwtUserDetails details = getUserDetails();
        return details != null ? details.getUsername() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getRole() {
        JwtUserDetails details = getUserDetails();
        return details != null ? details.getRole() : null;
    }
}