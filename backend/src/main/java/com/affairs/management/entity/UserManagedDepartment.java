package com.affairs.management.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户可管理部门关系
 */
@Data
public class UserManagedDepartment {
    /** 主键 */
    private Long id;
    /** 用户ID */
    private String userId;
    /** 可管理部门ID */
    private String deptId;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
