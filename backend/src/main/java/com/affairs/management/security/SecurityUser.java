package com.affairs.management.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Spring Security 用户实现，携带业务字段
 */
@Getter
public class SecurityUser implements UserDetails {

    private final String userId;
    private final String username;
    private final String password;
    private final String role;
    private final String deptId;
    private final List<String> managedDeptIds;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUser(String userId, String username, String password,
                        String role, String deptId, List<String> managedDeptIds,
                        boolean enabled) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.deptId = deptId;
        this.managedDeptIds = managedDeptIds != null ? managedDeptIds : Collections.emptyList();
        this.enabled = enabled;
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
