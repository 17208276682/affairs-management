package com.affairs.management.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ID 生成器：生成符合前端格式的 ID
 * - 部门：D001, D002 ...
 * - 用户：U001, U002 ...
 * - 事务：T20260315001, T20260315002 ...
 * - 流程记录：R001, R002 ...
 * - 附件：F001, F002 ...
 * - 通知：N001, N002 ...
 */
@Component
public class IdGenerator {

    private final ConcurrentHashMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    /**
     * 初始化计数器（应在启动时从数据库读取最大ID后调用）
     */
    public void initCounter(String prefix, int currentMax) {
        counters.put(prefix, new AtomicInteger(currentMax));
    }

    /**
     * 生成部门 ID: D + 3位序号
     */
    public String nextDeptId() {
        return generateWithPrefix("D", 3);
    }

    /**
     * 生成用户 ID: U + 3位序号
     */
    public String nextUserId() {
        return generateWithPrefix("U", 3);
    }

    /**
     * 生成事务 ID: T + yyyyMMdd + 3位序号
     */
    public String nextTaskId() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "T" + dateStr;
        int seq = counters.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
        return key + String.format("%03d", seq);
    }

    /**
     * 生成流程记录 ID: R + 3位序号
     */
    public String nextRecordId() {
        return generateWithPrefix("R", 3);
    }

    /**
     * 生成附件 ID: F + 3位序号
     */
    public String nextAttachmentId() {
        return generateWithPrefix("F", 3);
    }

    /**
     * 生成通知 ID: N + 3位序号
     */
    public String nextNotificationId() {
        return generateWithPrefix("N", 3);
    }

    private String generateWithPrefix(String prefix, int digits) {
        int seq = counters.computeIfAbsent(prefix, k -> new AtomicInteger(0)).incrementAndGet();
        return prefix + String.format("%0" + digits + "d", seq);
    }
}
