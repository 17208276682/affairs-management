package com.affairs.management.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
public class User {
    /** 用户ID，如U001 */
    private String id;
    /** 登录账号 */
    private String username;
    /** 密码哈希 */
    private String passwordHash;
    /** 姓名 */
    private String name;
    /** 头像URL */
    private String avatar;
    /** 手机号 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 所属部门ID */
    private String deptId;
    /** 岗位 */
    private String position;
    /** 角色：admin/director/manager/staff */
    private String role;
    /** 状态：0=离职 1=在职 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
