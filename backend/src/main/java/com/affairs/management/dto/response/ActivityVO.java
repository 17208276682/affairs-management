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
}
