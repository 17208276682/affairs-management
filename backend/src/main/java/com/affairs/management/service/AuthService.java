package com.affairs.management.service;

import com.affairs.management.dto.request.ChangePasswordRequest;
import com.affairs.management.dto.request.LoginRequest;
import com.affairs.management.dto.request.ResetPasswordRequest;
import com.affairs.management.dto.response.LoginResponse;
import com.affairs.management.dto.response.UserVO;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    UserVO getUserInfo(String userId);
    boolean resetPassword(ResetPasswordRequest request);
    boolean changePassword(String userId, ChangePasswordRequest request);
}
