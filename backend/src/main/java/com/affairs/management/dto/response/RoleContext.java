package com.affairs.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色上下文：描述用户可切换的角色视角
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleContext {
    /** 角色: ceo / director / manager / staff */
    private String role;
    /** 对应部门ID */
    private String deptId;
    /** 部门名称 */
    private String deptName;
    /** 显示标签，如"总经理"或"企划部负责人" */
    private String label;
}
