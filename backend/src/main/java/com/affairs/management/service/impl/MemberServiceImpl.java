package com.affairs.management.service.impl;

import com.affairs.management.common.BusinessException;
import com.affairs.management.common.ErrorCode;
import com.affairs.management.common.PaginatedData;
import com.affairs.management.dto.request.MemberFormData;
import com.affairs.management.dto.request.MemberListParams;
import com.affairs.management.dto.response.UserVO;
import com.affairs.management.entity.Department;
import com.affairs.management.entity.User;
import com.affairs.management.mapper.DeptMapper;
import com.affairs.management.mapper.UserManagedDeptMapper;
import com.affairs.management.mapper.UserMapper;
import com.affairs.management.service.MemberService;
import com.affairs.management.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final UserManagedDeptMapper userManagedDeptMapper;
    private final IdGenerator idGenerator;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public PaginatedData<UserVO> getMemberList(MemberListParams params) {
        int offset = (params.getPage() - 1) * params.getPageSize();
        List<User> users = userMapper.selectMemberList(
                params.getDeptId(), params.getKeyword(), params.getPosition(),
                offset, params.getPageSize());
        long total = userMapper.countMembers(
                params.getDeptId(), params.getKeyword(), params.getPosition());

        List<UserVO> voList = users.stream().map(this::toUserVO).collect(Collectors.toList());
        return PaginatedData.of(voList, total, params.getPage(), params.getPageSize());
    }

    @Override
    @Transactional
    public UserVO createMember(MemberFormData request) {
        // 唯一性校验
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (userMapper.selectByPhone(request.getPhone()) != null) {
            throw new BusinessException(ErrorCode.PHONE_EXISTS);
        }
        // 同部门 manager 唯一性
        if ("manager".equals(request.getRole()) && request.getDeptId() != null) {
            User existingMgr = userMapper.selectManagerByDeptId(request.getDeptId(), null);
            if (existingMgr != null) {
                throw new BusinessException(ErrorCode.MANAGER_EXISTS);
            }
        }

        User user = new User();
        user.setId(idGenerator.nextUserId());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDeptId(request.getDeptId());
        user.setPosition(request.getPosition());
        user.setRole(request.getRole());
        user.setStatus(1);
        userMapper.insert(user);

        // 可管理部门
        if (request.getManagedDeptIds() != null && !request.getManagedDeptIds().isEmpty()) {
            userManagedDeptMapper.batchInsert(user.getId(), request.getManagedDeptIds());
        }

        // 更新部门成员数量
        if (request.getDeptId() != null) {
            updateDeptMemberCount(request.getDeptId());
        }

        return toUserVO(user);
    }

    @Override
    @Transactional
    public UserVO updateMember(String id, MemberFormData request) {
        User existing = userMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 用户名唯一性（如果修改了）
        if (request.getUsername() != null && !request.getUsername().equals(existing.getUsername())) {
            if (userMapper.selectByUsername(request.getUsername()) != null) {
                throw new BusinessException(ErrorCode.USERNAME_EXISTS);
            }
        }
        // 手机号唯一性
        if (request.getPhone() != null && !request.getPhone().equals(existing.getPhone())) {
            if (userMapper.selectByPhone(request.getPhone()) != null) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS);
            }
        }
        // manager 唯一性
        String targetDeptId = request.getDeptId() != null ? request.getDeptId() : existing.getDeptId();
        if ("manager".equals(request.getRole()) && targetDeptId != null) {
            User existingMgr = userMapper.selectManagerByDeptId(targetDeptId, id);
            if (existingMgr != null) {
                throw new BusinessException(ErrorCode.MANAGER_EXISTS);
            }
        }

        User update = new User();
        update.setId(id);
        if (request.getUsername() != null) update.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            update.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getName() != null) update.setName(request.getName());
        if (request.getPhone() != null) update.setPhone(request.getPhone());
        if (request.getEmail() != null) update.setEmail(request.getEmail());
        if (request.getDeptId() != null) update.setDeptId(request.getDeptId());
        if (request.getPosition() != null) update.setPosition(request.getPosition());
        if (request.getRole() != null) update.setRole(request.getRole());
        userMapper.update(update);

        // 更新可管理部门
        if (request.getManagedDeptIds() != null) {
            userManagedDeptMapper.deleteByUserId(id);
            if (!request.getManagedDeptIds().isEmpty()) {
                userManagedDeptMapper.batchInsert(id, request.getManagedDeptIds());
            }
        }

        // 更新部门成员数量
        String oldDeptId = existing.getDeptId();
        String newDeptId = request.getDeptId();
        if (newDeptId != null && !newDeptId.equals(oldDeptId)) {
            if (oldDeptId != null) updateDeptMemberCount(oldDeptId);
            updateDeptMemberCount(newDeptId);
        }

        return toUserVO(userMapper.selectById(id));
    }

    @Override
    @Transactional
    public boolean deleteMember(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        // 检查关联事务
        int taskCount = userMapper.countTasksByUserId(id);
        if (taskCount > 0) {
            throw new BusinessException(ErrorCode.USER_HAS_TASKS);
        }

        userManagedDeptMapper.deleteByUserId(id);
        userMapper.deleteById(id);

        if (user.getDeptId() != null) {
            updateDeptMemberCount(user.getDeptId());
        }
        return true;
    }

    @Override
    public List<UserVO> getSelectableMembers(List<String> deptIds, String scope,
                                              String currentUserId, String currentRole,
                                              String currentDeptId) {
        List<User> users;
        if ("subordinates".equals(scope)) {
            // director: 仅可选择下一级部门的 manager
            // manager: 仅可选择本部门 staff
            if (currentDeptId == null) {
                users = Collections.emptyList();
            } else if ("manager".equals(currentRole)) {
                users = userMapper.selectByDeptId(currentDeptId).stream()
                        .filter(u -> !u.getId().equals(currentUserId))
                        .filter(u -> "staff".equals(u.getRole()))
                        .collect(Collectors.toList());
            } else {
                List<String> childDeptIds = deptMapper.selectChildren(currentDeptId)
                        .stream()
                        .map(Department::getId)
                        .collect(Collectors.toList());
                if (childDeptIds.isEmpty()) {
                    users = Collections.emptyList();
                } else {
                    users = userMapper.selectByDeptIds(childDeptIds).stream()
                            .filter(u -> !u.getId().equals(currentUserId))
                            .filter(u -> "manager".equals(u.getRole()))
                            .collect(Collectors.toList());
                }
            }
        } else if (deptIds != null && !deptIds.isEmpty()) {
            // 指定部门的成员
            users = userMapper.selectByDeptIds(deptIds);
            users = users.stream()
                    .filter(u -> !u.getId().equals(currentUserId))
                    .filter(u -> !"admin".equals(u.getRole()))
                    .collect(Collectors.toList());
        } else {
            // 默认：管辖部门负责人列表
            List<String> managedDeptIds = userManagedDeptMapper.selectDeptIdsByUserId(currentUserId);
            if (managedDeptIds.isEmpty()) {
                return Collections.emptyList();
            }
            users = new ArrayList<>();
            for (String deptId : managedDeptIds) {
                Department dept = deptMapper.selectById(deptId);
                if (dept != null && dept.getLeaderId() != null
                        && !dept.getLeaderId().equals(currentUserId)) {
                    User leader = userMapper.selectById(dept.getLeaderId());
                    if (leader != null && leader.getStatus() == 1) {
                        users.add(leader);
                    }
                }
            }
        }

        return users.stream().map(this::toUserVO).collect(Collectors.toList());
    }

    private void updateDeptMemberCount(String deptId) {
        List<User> members = userMapper.selectByDeptId(deptId);
        deptMapper.updateMemberCount(deptId, members.size());
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

        if (user.getDeptId() != null) {
            Department dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getName());
            }
        }
        List<String> managedDeptIds = userManagedDeptMapper.selectDeptIdsByUserId(user.getId());
        vo.setManagedDeptIds(managedDeptIds);

        if (user.getCreatedAt() != null) vo.setCreatedAt(user.getCreatedAt().format(DTF));
        if (user.getUpdatedAt() != null) vo.setUpdatedAt(user.getUpdatedAt().format(DTF));
        return vo;
    }
}
