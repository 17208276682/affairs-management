package com.affairs.management.dto.response;

import lombok.Data;

/**
 * 部门统计
 */
@Data
public class DeptStats {
    private String deptId;
    private String deptName;
    private Integer level;
    private int total;
    private int completed;
    private int overdue;
    private double avgHours;
}
