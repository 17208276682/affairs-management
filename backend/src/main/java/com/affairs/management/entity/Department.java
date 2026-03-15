package com.affairs.management.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 部门实体
 */
@Data
public class Department {
    /** 部门ID，如D001 */
    private String id;
    /** 部门名称 */
    private String name;
    /** 上级部门ID */
    private String parentId;
    /** 部门负责人用户ID */
    private String leaderId;
    /** 排序 */
    private Integer sortOrder;
    /** 层级 */
    private Integer level;
    /** 成员数量（冗余） */
    private Integer memberCount;
    /** 状态：0=停用 1=启用 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
