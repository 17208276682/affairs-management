package com.affairs.management.enums;

import lombok.Getter;

/**
 * 事务级别枚举
 */
@Getter
public enum TaskLevel {
    A("A", "特急"),
    B("B", "紧急"),
    C("C", "一般"),
    D("D", "普通");

    private final String code;
    private final String desc;

    TaskLevel(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TaskLevel fromCode(String code) {
        for (TaskLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown task level: " + code);
    }
}
