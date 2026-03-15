package com.affairs.management.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 事务 VO（列表/详情输出）
 */
@Data
public class TaskVO {
    private String id;
    private String title;
    private String description;
    private String assignerId;
    private String assignerName;
    private String assignerDept;
    private String assignerPosition;
    private String executorId;
    private String executorName;
    private String executorDept;
    private Integer importance;
    private Integer urgency;
    private String level;
    private String responseDeadline;
    private String completionDeadline;
    private String responseTime;
    private String completionTime;
    private String status;
    private List<AttachmentVO> attachments;
    private String parentTaskId;
    private List<String> childTaskIds;
    private String createdAt;
    private String updatedAt;
}
