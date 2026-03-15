package com.affairs.management.enums;

import lombok.Getter;

/**
 * 事务状态枚举
 */
@Getter
public enum TaskStatus {
    PENDING("pending", "待接收"),
    ACCEPTED("accepted", "已接收"),
    IN_PROGRESS("in_progress", "处理中"),
    SUBMITTED("submitted", "已提交"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已驳回"),
    COMPLETED("completed", "已完成"),
    OVERDUE("overdue", "已逾期"),
    CANCELLED("cancelled", "已取消");

    private final String code;
    private final String desc;

    TaskStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TaskStatus fromCode(String code) {
        for (TaskStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown task status: " + code);
    }
}
