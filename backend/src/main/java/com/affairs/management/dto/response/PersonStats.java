package com.affairs.management.dto.response;

import lombok.Data;

/**
 * 个人统计
 */
@Data
public class PersonStats {
    private String userId;
    private String name;
    private String deptId;
    private String deptName;
    private int totalTasks;
    private int completedTasks;
    private int overdueTasks;
    private double completionRate;
    private double avgResponseHours;
    private double avgCompletionHours;
}
