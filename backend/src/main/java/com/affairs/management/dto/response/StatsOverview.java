package com.affairs.management.dto.response;

import lombok.Data;

/**
 * 统计概览
 */
@Data
public class StatsOverview {
    private int totalTasks;
    private int pendingTasks;
    private int inProgressTasks;
    private int completedTasks;
    private int rejectedTasks;
    private int overdueTasks;
    private int cancelledTasks;
    private double overdueRate;
    private double completionRate;
    private double avgResponseHours;
    private double avgCompletionHours;

    // 顶层总经办统计新增字段
    /** 按时完成并审核通过的事务数 */
    private int onTimeCompleted;
    /** 逾期完成并审核通过的事务数 */
    private int overdueCompleted;
    /** 审核未通过的事务数 */
    private int failedReview;
    /** 待办事务数（未到截止时间且未完成） */
    private int todoTasks;
    /** 逾期未完成的事务数 */
    private int overdueUnfinished;
    /** 待审核的事务数 */
    private int submittedTasks;
}
