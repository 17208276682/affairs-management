package com.affairs.management.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 趋势统计
 */
@Data
public class TrendData {
    private List<String> dates;
    private List<Integer> created;
    private List<Integer> completed;
}
