package com.affairs.management.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 附件元数据
 */
@Data
public class Attachment {
    /** 附件ID，如F001 */
    private String id;
    /** 文件名 */
    private String fileName;
    /** 文件访问地址 */
    private String fileUrl;
    /** 文件大小（字节） */
    private Long fileSize;
    /** MIME类型 */
    private String mimeType;
    /** 上传人ID */
    private String uploadedBy;
    /** 上传时间 */
    private LocalDateTime uploadedAt;
    /** 创建时间 */
    private LocalDateTime createdAt;
}
