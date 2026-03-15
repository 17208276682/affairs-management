package com.affairs.management.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 流程记录 VO
 */
@Data
public class ProcessRecordVO {
    private String id;
    private String taskId;
    private String operatorId;
    private String operatorName;
    private String operatorAvatar;
    private String action;
    private String content;
    private List<AttachmentVO> attachments;
    private String createdAt;
}
