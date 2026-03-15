package com.affairs.management.dto.request;

import lombok.Data;

/**
 * 审核通过请求
 */
@Data
public class ApproveRequest {
    /** 审核评语 */
    private String comment;
}
