package com.affairs.management.util;

import java.util.Map;

/**
 * 事务级别计算器
 * 根据重要性(importance)和紧急度(urgency)计算事务级别(A/B/C/D)
 *
 * 矩阵：
 *          urgency=3(紧急)  urgency=2   urgency=1(不紧急)
 * imp=3      A               A            B
 * imp=2      A               B            C
 * imp=1      B               C            D
 */
public class TaskLevelCalculator {

    private static final Map<String, String> LEVEL_MATRIX = Map.of(
            "3_3", "A", "3_2", "A", "3_1", "B",
            "2_3", "A", "2_2", "B", "2_1", "C",
            "1_3", "B", "1_2", "C", "1_1", "D"
    );

    /** 各级别响应时限（小时） */
    public static final Map<String, Integer> RESPONSE_HOURS = Map.of(
            "A", 1, "B", 2, "C", 4, "D", 8
    );

    /** 各级别处理时限（小时） */
    public static final Map<String, Integer> COMPLETION_HOURS = Map.of(
            "A", 4, "B", 8, "C", 24, "D", 48
    );

    /**
     * 根据重要性和紧急度计算级别
     */
    public static String calculate(int importance, int urgency) {
        String key = importance + "_" + urgency;
        String level = LEVEL_MATRIX.get(key);
        if (level == null) {
            throw new IllegalArgumentException(
                    "Invalid importance(" + importance + ") or urgency(" + urgency + ")");
        }
        return level;
    }

    /**
     * 获取响应时限小时数
     */
    public static int getResponseHours(String level) {
        return RESPONSE_HOURS.getOrDefault(level, 8);
    }

    /**
     * 获取完成时限小时数
     */
    public static int getCompletionHours(String level) {
        return COMPLETION_HOURS.getOrDefault(level, 48);
    }

    private TaskLevelCalculator() {}
}
