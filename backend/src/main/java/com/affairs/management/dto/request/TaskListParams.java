package com.affairs.management.dto.request;

import lombok.Data;

/**
 * 事务列表查询参数
 */
@Data
public class TaskListParams {
    /** 列表类型: assigned/received/all/scope/todo */
    private String type = "all";
    /** 状态筛选 */
    private String status;
    /** 级别筛选 */
    private String level;
    /** 关键词搜索 */
    private String keyword;
    /** 页码 */
    private Integer page = 1;
    /** 页大小 */
    private Integer pageSize = 20;
}
