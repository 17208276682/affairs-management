package com.affairs.management.enums;

import lombok.Getter;

/**
 * 紧急类型枚举（四象限）
 */
@Getter
public enum UrgencyType {
    IMPORTANT_URGENT("important_urgent", "重要且紧急", 3, 3),
    IMPORTANT_NOT_URGENT("important_not_urgent", "重要不紧急", 3, 1),
    NOT_IMPORTANT_URGENT("not_important_urgent", "不重要但紧急", 1, 3),
    NOT_IMPORTANT_NOT_URGENT("not_important_not_urgent", "不重要不紧急", 1, 1);

    private final String code;
    private final String desc;
    private final int importance;
    private final int urgency;

    UrgencyType(String code, String desc, int importance, int urgency) {
        this.code = code;
        this.desc = desc;
        this.importance = importance;
        this.urgency = urgency;
    }

    public static UrgencyType fromCode(String code) {
        for (UrgencyType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown urgency type: " + code);
    }
}
