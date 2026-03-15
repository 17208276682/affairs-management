package com.affairs.management.enums;

import lombok.Getter;

/**
 * 通知类型枚举
 */
@Getter
public enum NotificationType {
    TASK_ASSIGNED("task_assigned", "事务指派"),
    TASK_ACCEPTED("task_accepted", "事务接收"),
    TASK_RESPONDED("task_responded", "事务响应"),
    TASK_COMPLETED("task_completed", "事务完成"),
    TASK_SUBMITTED("task_submitted", "事务提交"),
    TASK_APPROVED("task_approved", "事务通过"),
    TASK_REJECTED("task_rejected", "事务驳回"),
    SYSTEM("system", "系统通知");

    private final String code;
    private final String desc;

    NotificationType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
