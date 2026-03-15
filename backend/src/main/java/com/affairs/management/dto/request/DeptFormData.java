package com.affairs.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 部门表单
 */
@Data
public class DeptFormData {
    @NotBlank(message = "部门名称不能为空")
    private String name;
    /** 上级部门ID */
    private String parentId;
    /** 负责人ID */
    private String leaderId;
    /** 排序 */
    private Integer sort;
    /** 状态 */
    private Integer status;
}
