package com.affairs.management.controller;

import com.affairs.management.common.ApiResponse;
import com.affairs.management.dto.request.ChangePasswordRequest;
import com.affairs.management.dto.request.LoginRequest;
import com.affairs.management.dto.request.ResetPasswordRequest;
import com.affairs.management.dto.response.LoginResponse;
import com.affairs.management.dto.response.UserVO;
import com.affairs.management.security.SecurityUtils;
import com.affairs.management.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @GetMapping("/userinfo")
    public ApiResponse<UserVO> getUserInfo() {
        String userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(authService.getUserInfo(userId));
    }

    @PostMapping("/reset-password")
    public ApiResponse<Boolean> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ApiResponse.success(authService.resetPassword(request));
    }

    @PostMapping("/change-password")
    public ApiResponse<Boolean> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(authService.changePassword(userId, request));
    }
}
