package com.affairs.management.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 事务实体
 */
@Data
public class Task {
    /** 事务ID，如T20260315001 */
    private String id;
    /** 事务标题 */
    private String title;
    /** 事务描述 */
    private String description;
    /** 下达人ID */
    private String assignerId;
    /** 执行人ID */
    private String executorId;
    /** 重要性：1-3 */
    private Integer importance;
    /** 紧急度：1-3 */
    private Integer urgency;
    /** 级别：A/B/C/D */
    private String level;
    /** 响应截止时间 */
    private LocalDateTime responseDeadline;
    /** 完成截止时间 */
    private LocalDateTime completionDeadline;
    /** 实际响应时间 */
    private LocalDateTime responseTime;
    /** 实际完成时间 */
    private LocalDateTime completionTime;
    /** 状态 */
    private String status;
    /** 父任务ID（向下分派） */
    private String parentTaskId;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
