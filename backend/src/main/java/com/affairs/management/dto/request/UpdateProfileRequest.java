package com.affairs.management.dto.request;

import lombok.Data;

/**
 * 更新个人资料请求
 */
@Data
public class UpdateProfileRequest {
    private String name;
    private String phone;
    private String email;
    private String avatar;
}
