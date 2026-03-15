package com.affairs.management.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 用户信息 VO（对外输出，不含密码）
 */
@Data
public class UserVO {
    private String id;
    private String username;
    private String name;
    private String avatar;
    private String phone;
    private String email;
    private String deptId;
    private String deptName;
    private String position;
    private String role;
    private List<String> managedDeptIds;
    private Integer status;
    private String createdAt;
    private String updatedAt;
}
