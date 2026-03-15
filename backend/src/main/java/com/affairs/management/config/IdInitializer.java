package com.affairs.management.config;

import com.affairs.management.mapper.*;
import com.affairs.management.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动时初始化 ID 生成器计数器
 * 从数据库读取各表当前最大 ID，避免 ID 冲突
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class IdInitializer implements CommandLineRunner {

    private final IdGenerator idGenerator;
    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final TaskMapper taskMapper;
    private final TaskProcessRecordMapper recordMapper;
    private final AttachmentMapper attachmentMapper;
    private final NotificationMapper notificationMapper;

    @Override
    public void run(String... args) {
        initCounter("U", userMapper.selectMaxId());
        initCounter("D", deptMapper.selectMaxId());
        initCounter("R", recordMapper.selectMaxId());
        initCounter("F", attachmentMapper.selectMaxId());
        initCounter("N", notificationMapper.selectMaxId());

        // Task ID 格式特殊: T + yyyyMMdd + 3位序号，需要按日期前缀初始化
        String maxTaskId = taskMapper.selectMaxId();
        if (maxTaskId != null && maxTaskId.length() >= 11) {
            String datePrefix = maxTaskId.substring(0, 9); // T + yyyyMMdd
            String seqStr = maxTaskId.substring(9);
            try {
                int seq = Integer.parseInt(seqStr);
                idGenerator.initCounter(datePrefix, seq);
            } catch (NumberFormatException e) {
                log.warn("Cannot parse task ID sequence: {}", maxTaskId);
            }
        }

        log.info("ID Generator initialized successfully");
    }

    private void initCounter(String prefix, String maxId) {
        if (maxId != null && maxId.length() > prefix.length()) {
            try {
                int seq = Integer.parseInt(maxId.substring(prefix.length()));
                idGenerator.initCounter(prefix, seq);
                log.debug("Initialized counter for {}: {}", prefix, seq);
            } catch (NumberFormatException e) {
                log.warn("Cannot parse max ID for prefix {}: {}", prefix, maxId);
            }
        }
    }
}
