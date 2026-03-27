package com.affairs.management.service.impl;

import com.affairs.management.common.BusinessException;
import com.affairs.management.common.ErrorCode;
import com.affairs.management.dto.request.ChangePasswordRequest;
import com.affairs.management.dto.request.LoginRequest;
import com.affairs.management.dto.request.ResetPasswordRequest;
import com.affairs.management.dto.response.LoginResponse;
import com.affairs.management.dto.response.RoleContext;
import com.affairs.management.dto.response.UserVO;
import com.affairs.management.entity.Department;
import com.affairs.management.entity.User;
import com.affairs.management.mapper.DeptMapper;
import com.affairs.management.mapper.UserManagedDeptMapper;
import com.affairs.management.mapper.UserMapper;
import com.affairs.management.security.JwtTokenProvider;
import com.affairs.management.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final UserManagedDeptMapper userManagedDeptMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,20}$");

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            user = userMapper.selectByPhone(request.getUsername());
        }
        if (user == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(toUserVO(user));
        return response;
    }

    @Override
    public UserVO getUserInfo(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return toUserVO(user);
    }

    @Override
    public List<RoleContext> getRoleContexts(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        List<RoleContext> contexts = new ArrayList<>();
        String baseRole = user.getRole();

        // 只返回用户的真实角色（不再允许切换）
        String baseDeptName = "";
        if (user.getDeptId() != null) {
            Department dept = deptMapper.selectById(user.getDeptId());
            baseDeptName = dept != null ? dept.getName() : "";
        }
        String baseLabel = roleLabel(baseRole);
        contexts.add(new RoleContext(baseRole, user.getDeptId(), baseDeptName, baseLabel));

        return contexts;
    }

    private String roleLabel(String role) {
        return switch (role) {
            case "ceo" -> "总经理";
            case "director" -> "副总经理";
            case "manager" -> "负责人";
            case "staff" -> "普通员工";
            case "admin" -> "管理员";
            default -> role;
        };
    }

    @Override
    public boolean resetPassword(ResetPasswordRequest request) {
        User user = userMapper.selectByPhone(request.getPhone());
        if (user == null) {
            throw new BusinessException(ErrorCode.PHONE_NOT_FOUND);
        }
        // 密码强度校验
        if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            throw new BusinessException(ErrorCode.PASSWORD_INVALID,
                    "密码需包含大小写字母、数字和特殊字符，长度8-20位");
        }

        User update = new User();
        update.setId(user.getId());
        update.setPasswordHash(request.getNewPassword());
        userMapper.update(update);
        return true;
    }

    @Override
    public boolean changePassword(String userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        // 验证旧密码
        if (!request.getOldPassword().equals(user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_INVALID, "旧密码不正确");
        }
        // 新密码强度校验
        if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            throw new BusinessException(ErrorCode.PASSWORD_INVALID,
                    "密码需包含大小写字母、数字和特殊字符，长度8-20位");
        }
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPasswordHash(request.getNewPassword());
        userMapper.update(updateUser);
        return true;
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setName(user.getName());
        vo.setAvatar(user.getAvatar());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setDeptId(user.getDeptId());
        vo.setPosition(user.getPosition());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());

        // 获取部门名称
        if (user.getDeptId() != null) {
            Department dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getName());
            }
        }

        // 获取可管理部门ID
        List<String> managedDeptIds = userManagedDeptMapper.selectDeptIdsByUserId(user.getId());
        vo.setManagedDeptIds(managedDeptIds);

        if (user.getCreatedAt() != null) {
            vo.setCreatedAt(user.getCreatedAt().format(DTF));
        }
        if (user.getUpdatedAt() != null) {
            vo.setUpdatedAt(user.getUpdatedAt().format(DTF));
        }
        return vo;
    }
}
