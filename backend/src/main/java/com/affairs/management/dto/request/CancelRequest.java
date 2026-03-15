package com.affairs.management.dto.request;

import lombok.Data;

/**
 * 取消事务请求
 */
@Data
public class CancelRequest {
    /** 取消原因 */
    private String reason;
}
