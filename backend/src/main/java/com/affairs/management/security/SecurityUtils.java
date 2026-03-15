package com.affairs.management.security;

import com.affairs.management.common.BusinessException;
import com.affairs.management.common.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

/**
 * 安全工具类：获取当前登录用户信息
 */
public class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 获取当前登录用户
     */
    public static SecurityUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser) {
            return (SecurityUser) authentication.getPrincipal();
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    /**
     * 获取当前用户 ID
     */
    public static String getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentRole() {
        return getCurrentUser().getRole();
    }

    /**
     * 获取当前用户部门 ID
     */
    public static String getCurrentDeptId() {
        return getCurrentUser().getDeptId();
    }

    /**
     * 获取当前用户管辖部门 ID 列表
     */
    public static List<String> getCurrentManagedDeptIds() {
        return getCurrentUser().getManagedDeptIds();
    }

    /**
     * 判断当前用户是否是管理员
     */
    public static boolean isAdmin() {
        return "admin".equals(getCurrentRole());
    }

    /**
     * 判断当前用户是否是总经办
     */
    public static boolean isDirector() {
        return "director".equals(getCurrentRole());
    }

    /**
     * 判断当前用户是否是经理（含总经办）
     */
    public static boolean isManager() {
        String role = getCurrentRole();
        return "manager".equals(role) || "director".equals(role);
    }
}
