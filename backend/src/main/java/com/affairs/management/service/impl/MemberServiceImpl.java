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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final UserManagedDeptMapper userManagedDeptMapper;
    private final IdGenerator idGenerator;

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
        if (request.getDeptId() == null || request.getDeptId().isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "所属部门不能为空");
        }
        validateRoleByDept(request.getRole(), request.getDeptId());

        String loginAccount = (request.getPhone() != null && !request.getPhone().isBlank())
                ? request.getPhone().trim()
                : request.getUsername();
        if (loginAccount == null || loginAccount.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "手机号不能为空");
        }

        // 唯一性校验
        if (userMapper.selectByUsername(loginAccount) != null) {
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
        user.setUsername(loginAccount);
        user.setPasswordHash(request.getPassword());
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setDeptId(request.getDeptId());
        user.setPosition(request.getPosition());
        user.setRole(request.getRole());
        user.setStatus(1);
        userMapper.insert(user);

        List<String> targetManagedDeptIds = normalizeManagedDeptIds(request.getManagedDeptIds());
        validateManagedDeptAssignment(user.getId(), request.getRole(), targetManagedDeptIds);

        // 顶层角色可兼任第二层部门负责人
        if (!targetManagedDeptIds.isEmpty()) {
            userManagedDeptMapper.batchInsert(user.getId(), targetManagedDeptIds);
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

        String targetUsername = request.getPhone() != null && !request.getPhone().isBlank()
                ? request.getPhone().trim()
                : request.getUsername();

        // 登录账号唯一性（手机号优先）
        if (targetUsername != null && !targetUsername.equals(existing.getUsername())) {
            if (userMapper.selectByUsername(targetUsername) != null) {
                throw new BusinessException(ErrorCode.USERNAME_EXISTS);
            }
        }
        // 手机号唯一性
        if (request.getPhone() != null && !request.getPhone().equals(existing.getPhone())) {
            if (userMapper.selectByPhone(request.getPhone()) != null) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS);
            }
        }
        String targetRole = request.getRole() != null ? request.getRole() : existing.getRole();
        String targetDeptId = request.getDeptId() != null ? request.getDeptId() : existing.getDeptId();
        validateRoleByDept(targetRole, targetDeptId);

        // manager 唯一性
        if ("manager".equals(targetRole) && targetDeptId != null) {
            User existingMgr = userMapper.selectManagerByDeptId(targetDeptId, id);
            if (existingMgr != null) {
                throw new BusinessException(ErrorCode.MANAGER_EXISTS);
            }
        }

        User update = new User();
        update.setId(id);
        if (targetUsername != null) update.setUsername(targetUsername);
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            update.setPasswordHash(request.getPassword());
        }
        if (request.getName() != null) update.setName(request.getName());
        if (request.getPhone() != null) update.setPhone(request.getPhone());
        if (request.getEmail() != null) update.setEmail(request.getEmail());
        if (request.getDeptId() != null) update.setDeptId(request.getDeptId());
        if (request.getPosition() != null) update.setPosition(request.getPosition());
        if (request.getRole() != null) update.setRole(request.getRole());
        userMapper.update(update);

        List<String> targetManagedDeptIds;
        if (request.getManagedDeptIds() != null) {
            targetManagedDeptIds = normalizeManagedDeptIds(request.getManagedDeptIds());
        } else if ("ceo".equals(targetRole) || "director".equals(targetRole)) {
            targetManagedDeptIds = userManagedDeptMapper.selectDeptIdsByUserId(id);
        } else {
            targetManagedDeptIds = Collections.emptyList();
        }
        validateManagedDeptAssignment(id, targetRole, targetManagedDeptIds);

        // 更新可管理部门（角色变更为非顶层时自动清空）
        if (request.getManagedDeptIds() != null || (!"ceo".equals(targetRole) && !"director".equals(targetRole))) {
            userManagedDeptMapper.deleteByUserId(id);
            if (!targetManagedDeptIds.isEmpty()) {
                userManagedDeptMapper.batchInsert(id, targetManagedDeptIds);
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

        // 逻辑删除：保留用户记录以满足任务流程记录等外键约束
        User update = new User();
        update.setId(id);
        update.setStatus(0);
        userMapper.update(update);

        userManagedDeptMapper.deleteByUserId(id);

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
            if (currentDeptId == null) {
                users = Collections.emptyList();
            } else if ("ceo".equals(currentRole) || "director".equals(currentRole)) {
                // 总经办：合并"下一级部门负责人" + "兼任部门的下级"
                users = resolveTopLevelSubordinates(currentUserId, currentRole, currentDeptId);
            } else if ("manager".equals(currentRole)) {
                Map<String, User> selected = new LinkedHashMap<>();

                List<Department> childDepts = deptMapper.selectChildren(currentDeptId);
                if (!childDepts.isEmpty()) {
                    List<String> childDeptIds = childDepts.stream()
                            .map(Department::getId)
                            .collect(Collectors.toList());
                    userMapper.selectByDeptIds(childDeptIds).stream()
                            .filter(u -> !u.getId().equals(currentUserId))
                            .filter(u -> "manager".equals(u.getRole()))
                            .forEach(u -> selected.put(u.getId(), u));
                } else {
                    userMapper.selectByDeptId(currentDeptId).stream()
                            .filter(u -> !u.getId().equals(currentUserId))
                            .filter(u -> "staff".equals(u.getRole()))
                            .forEach(u -> selected.put(u.getId(), u));
                }

                users = new ArrayList<>(selected.values());
            } else {
                users = Collections.emptyList();
            }
        } else if (deptIds != null && !deptIds.isEmpty()) {
            users = userMapper.selectByDeptIds(deptIds);
            users = users.stream()
                    .filter(u -> !u.getId().equals(currentUserId))
                    .filter(u -> !"ceo".equals(u.getRole()) && !"admin".equals(u.getRole())
                                 && !"director".equals(u.getRole()))
                    .collect(Collectors.toList());
        } else {
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

    private void validateRoleByDept(String role, String deptId) {
        if (role == null || role.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不能为空");
        }
        if (deptId == null || deptId.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "所属部门不能为空");
        }

        Department dept = deptMapper.selectById(deptId);
        if (dept == null) {
            throw new BusinessException(ErrorCode.DEPT_NOT_FOUND);
        }

        boolean isTopLevel = Objects.equals(dept.getLevel(), 0) || dept.getParentId() == null;
        if (isTopLevel) {
            if (!"ceo".equals(role) && !"director".equals(role)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "顶层部门角色仅支持总经理或副总经理");
            }
        } else if (!"manager".equals(role) && !"staff".equals(role)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非顶层部门角色仅支持负责人或普通员工");
        }
    }

    private List<String> normalizeManagedDeptIds(List<String> managedDeptIds) {
        if (managedDeptIds == null) {
            return Collections.emptyList();
        }
        return managedDeptIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private void validateManagedDeptAssignment(String userId, String role, List<String> managedDeptIds) {
        if (managedDeptIds == null || managedDeptIds.isEmpty()) {
            return;
        }
        if (!"ceo".equals(role) && !"director".equals(role)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅总经理或副总经理可兼任第二层负责人");
        }

        for (String managedDeptId : managedDeptIds) {
            Department managedDept = deptMapper.selectById(managedDeptId);
            if (managedDept == null) {
                throw new BusinessException(ErrorCode.DEPT_NOT_FOUND);
            }
            if (!Objects.equals(managedDept.getLevel(), 1)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "仅可兼任第二层部门负责人");
            }

            User manager = userMapper.selectManagerByDeptId(managedDeptId, userId);
            if (manager != null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "部门【" + managedDept.getName() + "】已存在负责人");
            }

            List<String> assignedUsers = userManagedDeptMapper.selectUserIdsByDeptId(managedDeptId);
            boolean ownedByOther = assignedUsers.stream().anyMatch(assignedUserId -> !assignedUserId.equals(userId));
            if (ownedByOther) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "部门【" + managedDept.getName() + "】已存在负责人");
            }
        }
    }

    private List<User> resolveTopLevelSubordinates(String currentUserId, String currentRole, String currentDeptId) {
        Map<String, User> selected = new LinkedHashMap<>();
        List<String> assignerManagedDeptIds = userManagedDeptMapper.selectDeptIdsByUserId(currentUserId);

        // 1. 作为 ceo/director，显示下一级部门的负责人（排除 ceo/director 角色）
        List<Department> childDepts = deptMapper.selectChildren(currentDeptId);
        for (Department childDept : childDepts) {
            if (assignerManagedDeptIds.contains(childDept.getId())) {
                continue; // 自己兼任的部门，由路径2处理
            }
            addDeptHeads(childDept.getId(), currentUserId, selected);
        }

        // 2. 作为兼任部门负责人，显示管理部门的下级
        for (String managedDeptId : assignerManagedDeptIds) {
            List<Department> managedChildDepts = deptMapper.selectChildren(managedDeptId);
            if (!managedChildDepts.isEmpty()) {
                // 有子部门 → 显示子部门负责人（排除 ceo/director）
                for (Department managedChildDept : managedChildDepts) {
                    addDeptHeads(managedChildDept.getId(), currentUserId, selected);
                }
            } else {
                // 没有子部门 → 显示本部门 staff
                userMapper.selectByDeptId(managedDeptId).stream()
                        .filter(u -> !u.getId().equals(currentUserId))
                        .filter(u -> "staff".equals(u.getRole()))
                        .forEach(u -> selected.put(u.getId(), u));
            }
        }

        // 过滤掉 ceo/director 角色（不能给总经办下发任务）
        selected.values().removeIf(u -> "ceo".equals(u.getRole()) || "director".equals(u.getRole()));

        return new ArrayList<>(selected.values());
    }

    /**
     * 添加某部门的负责人到候选列表（包含 manager 和兼任负责人的 ceo/director）
     */
    private void addDeptHeads(String deptId, String excludeUserId, Map<String, User> selected) {
        // 查找角色为 manager 的专职负责人
        User manager = userMapper.selectManagerByDeptId(deptId, null);
        if (manager != null && !manager.getId().equals(excludeUserId)
                && manager.getStatus() != null && manager.getStatus() == 1) {
            selected.put(manager.getId(), manager);
        }
        // 查找通过 managed_departments 兼任负责人的 ceo/director
        List<String> managingUserIds = userManagedDeptMapper.selectUserIdsByDeptId(deptId);
        for (String uid : managingUserIds) {
            if (!uid.equals(excludeUserId)) {
                User u = userMapper.selectById(uid);
                if (u != null && u.getStatus() != null && u.getStatus() == 1
                        && ("ceo".equals(u.getRole()) || "director".equals(u.getRole()))) {
                    selected.put(u.getId(), u);
                }
            }
        }
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
