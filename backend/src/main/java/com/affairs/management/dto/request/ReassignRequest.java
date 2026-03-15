package com.affairs.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 向下分派请求
 */
@Data
public class ReassignRequest {
    @NotBlank(message = "执行人不能为空")
    private String executorId;
}
