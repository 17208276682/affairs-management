package com.affairs.management.dto.response;

import lombok.Data;

/**
 * 级别统计
 */
@Data
public class LevelStats {
    private String level;
    private int total;
    private int completed;
    private double avgHours;
}
