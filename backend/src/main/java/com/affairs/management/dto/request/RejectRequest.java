package com.affairs.management.dto.request;

import lombok.Data;

/**
 * 审核驳回请求
 */
@Data
public class RejectRequest {
    /** 审核评语 */
    private String comment;
    /** 驳回原因 */
    private String reason;
}
