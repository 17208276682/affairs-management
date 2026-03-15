package com.affairs.management.dto.request;

import lombok.Data;

/**
 * 人员列表查询参数
 */
@Data
public class MemberListParams {
    /** 部门ID */
    private String deptId;
    /** 关键词（姓名/账号） */
    private String keyword;
    /** 职位 */
    private String position;
    /** 页码 */
    private Integer page = 1;
    /** 页大小 */
    private Integer pageSize = 20;
}
