package com.affairs.management.config;

import com.affairs.management.entity.User;
import com.affairs.management.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 启动时校验并修正初始用户密码哈希
 * 仅在密码验证失败时才更新（即 SQL 中的哈希无效时自动修正）
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class PasswordHashFixer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /** 预设账号和明文密码 */
    private static final Map<String, String> PRESET_PASSWORDS = new LinkedHashMap<>();
    static {
        PRESET_PASSWORDS.put("admin", "Admin123");
        PRESET_PASSWORDS.put("zhaoyang", "Abc@1234");
        PRESET_PASSWORDS.put("wangjh", "Abc@1234");
        PRESET_PASSWORDS.put("zhangsan", "Abc@1234");
        PRESET_PASSWORDS.put("lisi", "Abc@1234");
        PRESET_PASSWORDS.put("wangcheng", "Abc@1234");
        PRESET_PASSWORDS.put("wangfang", "Abc@1234");
        PRESET_PASSWORDS.put("huangxl", "Abc@1234");
    }

    @Override
    public void run(String... args) {
        int fixed = 0;
        for (Map.Entry<String, String> entry : PRESET_PASSWORDS.entrySet()) {
            try {
                User user = userMapper.selectByUsername(entry.getKey());
                if (user == null) continue;

                // 如果当前哈希能通过验证，跳过
                if (user.getPasswordHash() != null
                        && passwordEncoder.matches(entry.getValue(), user.getPasswordHash())) {
                    continue;
                }

                // 哈希无效，重新生成
                String newHash = passwordEncoder.encode(entry.getValue());
                User update = new User();
                update.setId(user.getId());
                update.setPasswordHash(newHash);
                userMapper.update(update);
                fixed++;
                log.info("Fixed password hash for user: {}", entry.getKey());
            } catch (Exception e) {
                log.warn("Failed to check/fix password for user: {}, reason: {}",
                        entry.getKey(), e.getMessage());
            }
        }
        if (fixed > 0) {
            log.info("Password hash fixer: corrected {} user(s)", fixed);
        } else {
            log.info("Password hash fixer: all passwords OK");
        }
    }
}
