package com.affairs.management.dto.response;

import lombok.Data;

/**
 * 最近动态
 */
@Data
public class ActivityVO {
    private String id;
    private String type;
    private String content;
    private String time;
    /** 分类：superior(上级事务) / dept(部门事务)，仅负责人使用 */
    private String category;
}
