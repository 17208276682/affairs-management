package com.affairs.management.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 通知 VO（前端兼容）
 */
@Data
public class NotificationVO {
    private String id;
    private String userId;
    private String title;
    private String content;
    private String type;
    private String relatedTaskId;
    /** 前端使用 boolean 类型，JSON 字段名保持 isRead */
    @JsonProperty("isRead")
    private boolean isRead;
    private String createdAt;
}
