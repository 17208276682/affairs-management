package com.affairs.management.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 创建事务请求
 */
@Data
public class CreateTaskRequest {
    /** 事务描述 */
    @NotBlank(message = "事务描述不能为空")
    private String description;

    /** 紧急类型（四象限） */
    @NotBlank(message = "紧急类型不能为空")
    private String urgencyType;

    /** 完成截止时间 (ISO 格式) */
    private String completionDeadline;

    /** 执行人 ID */
    @NotBlank(message = "执行人不能为空")
    private String executorId;

    /** 附件 ID 列表 */
    private List<String> attachmentIds;

    /**
     * 兼容前端发送 attachments 字段（对象数组或字符串ID数组）
     */
    @JsonProperty("attachments")
    public void setAttachments(List<Object> attachments) {
        if (attachments == null || attachments.isEmpty()) return;
        if (this.attachmentIds == null) {
            this.attachmentIds = new ArrayList<>();
        }
        for (Object item : attachments) {
            if (item instanceof String) {
                this.attachmentIds.add((String) item);
            } else if (item instanceof Map) {
                Object id = ((Map<?, ?>) item).get("id");
                if (id != null) {
                    this.attachmentIds.add(id.toString());
                }
            }
        }
    }
}
