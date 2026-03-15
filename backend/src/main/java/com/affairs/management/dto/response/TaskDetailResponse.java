package com.affairs.management.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 事务详情响应
 */
@Data
public class TaskDetailResponse {
    private TaskVO task;
    private List<ProcessRecordVO> processRecords;
}
