package com.affairs.management.security;

import com.affairs.management.entity.Department;
import com.affairs.management.mapper.DeptMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * JWT 认证过滤器：从 Authorization 头解析 Bearer token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final DeptMapper deptMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                String userId = jwtTokenProvider.getUserIdFromToken(token);
                SecurityUser userDetails = userDetailsService.loadUserById(userId);
                SecurityUser effectiveUser = resolveEffectiveUser(
                        userDetails,
                        request.getHeader("X-Active-Role"),
                        request.getHeader("X-Active-Dept"));

                if (effectiveUser.isEnabled()) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                            effectiveUser, null, effectiveUser.getAuthorities());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private SecurityUser resolveEffectiveUser(SecurityUser userDetails, String activeRoleHeader) {
        return resolveEffectiveUser(userDetails, activeRoleHeader, null);
    }

    private SecurityUser resolveEffectiveUser(SecurityUser userDetails, String activeRoleHeader, String activeDeptHeader) {
        if (!StringUtils.hasText(activeRoleHeader)) {
            return userDetails;
        }

        String activeRole = activeRoleHeader.trim().toLowerCase();
        String baseRole = userDetails.getRole();

        // 仅允许切换到：本身角色；或 顶层角色(ceo/director)切到 manager 视角
        boolean allowed = activeRole.equals(baseRole)
                || (("ceo".equals(baseRole) || "director".equals(baseRole))
                    && "manager".equals(activeRole)
                    && userDetails.getManagedDeptIds() != null
                    && !userDetails.getManagedDeptIds().isEmpty());

        if (!allowed) {
            return userDetails;
        }

        List<String> effectiveManagedDeptIds = userDetails.getManagedDeptIds();
        String effectiveDeptId = userDetails.getDeptId();

        // 顶层角色切为负责人视角：按指定部门或第一个管理部门收敛
        if (("ceo".equals(baseRole) || "director".equals(baseRole)) && "manager".equals(activeRole)) {
            List<Department> managedLevel1Depts = userDetails.getManagedDeptIds().stream()
                .map(deptMapper::selectById)
                .filter(java.util.Objects::nonNull)
                .filter(dept -> Integer.valueOf(1).equals(dept.getLevel()))
                .sorted(Comparator.comparing(Department::getId))
                .toList();

            effectiveManagedDeptIds = managedLevel1Depts.stream().map(Department::getId).toList();

            // 优先使用前端指定的部门
            if (StringUtils.hasText(activeDeptHeader)
                    && effectiveManagedDeptIds.contains(activeDeptHeader.trim())) {
                effectiveDeptId = activeDeptHeader.trim();
            } else if (!effectiveManagedDeptIds.isEmpty()) {
                effectiveDeptId = effectiveManagedDeptIds.get(0);
            }
        }

        return new SecurityUser(
                userDetails.getUserId(),
                userDetails.getUsername(),
                userDetails.getPassword(),
                activeRole,
                effectiveDeptId,
                effectiveManagedDeptIds,
                userDetails.isEnabled());
    }
}
