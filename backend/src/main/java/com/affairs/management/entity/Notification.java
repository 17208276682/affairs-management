package com.affairs.management.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知实体
 */
@Data
public class Notification {
    /** 通知ID，如N001 */
    private String id;
    /** 接收用户ID */
    private String userId;
    /** 通知标题 */
    private String title;
    /** 通知内容 */
    private String content;
    /** 通知类型 */
    private String type;
    /** 关联任务ID */
    private String relatedTaskId;
    /** 是否已读：0否 1是 */
    private Integer isRead;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
