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
    private int overdueTasks;
    private double overdueRate;
    private double completionRate;
    private double avgResponseHours;
    private double avgCompletionHours;
}
