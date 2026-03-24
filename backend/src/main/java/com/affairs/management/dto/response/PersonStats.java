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

    // 堆积图新增字段
    private int onTimeCompleted;
    private int overdueCompleted;
    private int failedReview;
    private int todoTasks;
    private int overdueUnfinished;
    private int cancelledTasks;
}
