package com.affairs.management.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 处理记录 / 提交成果请求
 */
@Data
public class ProcessRequest {
    /** 操作内容 */
    private String content;
    /** 附件 ID 列表 */
    private List<String> attachmentIds;

    /**
     * 兼容前端发送 attachments 字段
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
