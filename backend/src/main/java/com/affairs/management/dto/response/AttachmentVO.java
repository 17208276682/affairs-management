package com.affairs.management.dto.response;

import lombok.Data;

/**
 * 附件 VO
 */
@Data
public class AttachmentVO {
    private String id;
    private String name;
    private String url;
    private String type;
    private Long size;
    private String uploadedAt;
}
