package com.affairs.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 人员表单
 */
@Data
public class MemberFormData {
    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "账号不能为空")
    private String username;

    /** 密码（新增时必填，编辑时可选） */
    private String password;

    /** 部门ID */
    private String deptId;

    /** 岗位 */
    private String position;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色 */
    @NotBlank(message = "角色不能为空")
    private String role;

    /** 可管理部门ID列表 */
    private List<String> managedDeptIds;
}
