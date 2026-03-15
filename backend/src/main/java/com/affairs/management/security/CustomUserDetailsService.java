package com.affairs.management.security;

import com.affairs.management.entity.User;
import com.affairs.management.mapper.UserManagedDeptMapper;
import com.affairs.management.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自定义 UserDetailsService 实现
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final UserManagedDeptMapper userManagedDeptMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        List<String> managedDeptIds = userManagedDeptMapper.selectDeptIdsByUserId(user.getId());
        return new SecurityUser(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getRole(),
                user.getDeptId(),
                managedDeptIds,
                user.getStatus() == 1
        );
    }

    /**
     * 通过用户ID加载（用于JWT过滤器）
     */
    public SecurityUser loadUserById(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("User not found by id: " + userId);
        }
        List<String> managedDeptIds = userManagedDeptMapper.selectDeptIdsByUserId(userId);
        return new SecurityUser(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getRole(),
                user.getDeptId(),
                managedDeptIds,
                user.getStatus() == 1
        );
    }
}
