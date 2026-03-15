package com.affairs.management.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 任务-附件关系
 */
@Data
public class TaskAttachment {
    private Long id;
    private String taskId;
    private String attachmentId;
    private LocalDateTime createdAt;
}
