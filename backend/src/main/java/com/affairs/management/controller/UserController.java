package com.affairs.management.controller;

import com.affairs.management.common.ApiResponse;
import com.affairs.management.dto.request.UpdateProfileRequest;
import com.affairs.management.dto.response.UserVO;
import com.affairs.management.security.SecurityUtils;
import com.affairs.management.service.AuthService;
import com.affairs.management.entity.User;
import com.affairs.management.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final AuthService authService;

    @PutMapping("/profile")
    public ApiResponse<UserVO> updateProfile(@RequestBody UpdateProfileRequest request) {
        String userId = SecurityUtils.getCurrentUserId();

        User update = new User();
        update.setId(userId);
        if (request.getName() != null) update.setName(request.getName());
        if (request.getPhone() != null) update.setPhone(request.getPhone());
        if (request.getEmail() != null) update.setEmail(request.getEmail());
        if (request.getAvatar() != null) update.setAvatar(request.getAvatar());

        userMapper.update(update);
        return ApiResponse.success(authService.getUserInfo(userId));
    }
}
