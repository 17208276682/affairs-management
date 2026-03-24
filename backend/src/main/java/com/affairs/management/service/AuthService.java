package com.affairs.management.service;

import com.affairs.management.dto.request.ChangePasswordRequest;
import com.affairs.management.dto.request.LoginRequest;
import com.affairs.management.dto.request.ResetPasswordRequest;
import com.affairs.management.dto.response.LoginResponse;
import com.affairs.management.dto.response.RoleContext;
import com.affairs.management.dto.response.UserVO;

import java.util.List;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    UserVO getUserInfo(String userId);
    List<RoleContext> getRoleContexts(String userId);
    boolean resetPassword(ResetPasswordRequest request);
    boolean changePassword(String userId, ChangePasswordRequest request);
}
