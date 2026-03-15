package com.affairs.management.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 流程记录-附件关系
 */
@Data
public class RecordAttachment {
    private Long id;
    private String recordId;
    private String attachmentId;
    private LocalDateTime createdAt;
}
