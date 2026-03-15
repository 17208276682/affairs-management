package com.affairs.management.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务流程记录
 */
@Data
public class TaskProcessRecord {
    /** 流程记录ID，如R001 */
    private String id;
    /** 关联任务ID */
    private String taskId;
    /** 操作人ID */
    private String operatorId;
    /** 操作类型 */
    private String action;
    /** 操作内容 */
    private String content;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
