package com.affairs.management.common;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {
    // 通用
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未认证或 token 已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 认证相关 1xxx
    LOGIN_FAILED(1001, "用户名或密码错误"),
    USER_DISABLED(1002, "账号已被禁用"),
    TOKEN_EXPIRED(1003, "Token 已过期"),
    TOKEN_INVALID(1004, "无效的 Token"),
    PASSWORD_INVALID(1005, "密码不符合要求：8-20位，需包含大小写字母、数字和特殊字符"),
    PHONE_NOT_FOUND(1006, "该手机号未注册"),

    // 用户相关 2xxx
    USER_NOT_FOUND(2001, "用户不存在"),
    USERNAME_EXISTS(2002, "用户名已存在"),
    PHONE_EXISTS(2003, "手机号已被注册"),
    MANAGER_EXISTS(2004, "该部门已有经理角色"),
    USER_HAS_TASKS(2005, "该用户存在关联事务，无法删除"),

    // 部门相关 3xxx
    DEPT_NOT_FOUND(3001, "部门不存在"),
    DEPT_NAME_EXISTS(3002, "同级下部门名称已存在"),
    DEPT_HAS_CHILDREN(3003, "该部门下存在子部门"),
    DEPT_IS_ROOT(3004, "不允许删除顶级部门"),

    // 事务相关 4xxx
    TASK_NOT_FOUND(4001, "事务不存在"),
    TASK_STATUS_INVALID(4002, "当前事务状态不允许此操作"),
    TASK_NO_PERMISSION(4003, "无权操作此事务"),
    TASK_EXECUTOR_INVALID(4004, "执行人不合法"),

    // 文件相关 5xxx
    FILE_UPLOAD_FAILED(5001, "文件上传失败"),
    FILE_NOT_FOUND(5002, "文件不存在"),
    FILE_TOO_LARGE(5003, "文件大小超过限制");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
