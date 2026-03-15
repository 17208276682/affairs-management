package com.affairs.management.enums;

import lombok.Getter;

/**
 * 流程操作类型枚举
 */
@Getter
public enum ProcessAction {
    CREATE("create", "创建"),
    ASSIGN("assign", "分配"),
    ACCEPT("accept", "接收"),
    PROCESS("process", "处理"),
    SUBMIT("submit", "提交"),
    APPROVE("approve", "通过"),
    REJECT("reject", "驳回"),
    REASSIGN("reassign", "转派"),
    CANCEL("cancel", "取消"),
    COMMENT("comment", "评论");

    private final String code;
    private final String desc;

    ProcessAction(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProcessAction fromCode(String code) {
        for (ProcessAction action : values()) {
            if (action.code.equals(code)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown process action: " + code);
    }
}
