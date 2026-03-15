package com.affairs.management.service.impl;

import com.affairs.management.common.BusinessException;
import com.affairs.management.common.ErrorCode;
import com.affairs.management.dto.request.DeptFormData;
import com.affairs.management.dto.response.DeptVO;
import com.affairs.management.dto.response.OrgTreeNode;
import com.affairs.management.entity.Department;
import com.affairs.management.entity.User;
import com.affairs.management.mapper.DeptMapper;
import com.affairs.management.mapper.UserMapper;
import com.affairs.management.mapper.UserManagedDeptMapper;
import com.affairs.management.service.DeptService;
import com.affairs.management.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;
    private final UserMapper userMapper;
    private final UserManagedDeptMapper userManagedDeptMapper;
    private final IdGenerator idGenerator;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<OrgTreeNode> getOrgTree() {
        List<Department> allDepts = deptMapper.selectAll();
        List<User> allUsers = userMapper.selectByDeptIds(
                allDepts.stream().map(Department::getId).collect(Collectors.toList()));

        // 按部门分组用户
        Map<String, List<User>> usersByDept = allUsers.stream()
                .filter(u -> u.getDeptId() != null)
                .collect(Collectors.groupingBy(User::getDeptId));

        // 构建部门节点 map
        Map<String, OrgTreeNode> nodeMap = new LinkedHashMap<>();
        for (Department dept : allDepts) {
            OrgTreeNode node = new OrgTreeNode();
            node.setId(dept.getId());
            node.setLabel(dept.getName());
            node.setType("dept");
            node.setParentId(dept.getParentId());
            node.setMemberCount(dept.getMemberCount());
            node.setChildren(new ArrayList<>());

            // 查负责人名称
            if (dept.getLeaderId() != null) {
                User leader = userMapper.selectById(dept.getLeaderId());
                if (leader != null) {
                    node.setLeaderName(leader.getName());
                }
            }

            // 添加成员子节点
            List<User> deptUsers = usersByDept.getOrDefault(dept.getId(), Collections.emptyList());
            for (User user : deptUsers) {
                OrgTreeNode memberNode = new OrgTreeNode();
                memberNode.setId(user.getId());
                memberNode.setLabel(user.getName());
                memberNode.setType("member");
                memberNode.setParentId(dept.getId());
                memberNode.setPosition(user.getPosition());
                memberNode.setAvatar(user.getAvatar());
                node.getChildren().add(memberNode);
            }

            nodeMap.put(dept.getId(), node);
        }

        // 组装树
        List<OrgTreeNode> roots = new ArrayList<>();
        for (OrgTreeNode node : nodeMap.values()) {
            if (node.getParentId() != null && nodeMap.containsKey(node.getParentId())) {
                // 部门子节点放在成员前面
                OrgTreeNode parent = nodeMap.get(node.getParentId());
                parent.getChildren().add(0, node);
            } else {
                roots.add(node);
            }
        }

        return roots;
    }

    @Override
    public List<DeptVO> getDeptList(String parentId) {
        List<Department> depts;
        if (parentId != null && !parentId.isEmpty()) {
            depts = deptMapper.selectByParentId(parentId);
        } else {
            depts = deptMapper.selectAll();
        }
        return depts.stream().map(this::toDeptVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeptVO createDept(DeptFormData request) {
        // 检查同级重名
        Department existing = deptMapper.selectByNameAndParentId(
                request.getName(), request.getParentId(), null);
        if (existing != null) {
            throw new BusinessException(ErrorCode.DEPT_NAME_EXISTS);
        }

        // 计算层级
        int level = 0;
        if (request.getParentId() != null) {
            Department parent = deptMapper.selectById(request.getParentId());
            if (parent != null) {
                level = parent.getLevel() + 1;
            }
        }

        Department dept = new Department();
        dept.setId(idGenerator.nextDeptId());
        dept.setName(request.getName());
        dept.setParentId(request.getParentId());
        dept.setLeaderId(request.getLeaderId());
        dept.setSortOrder(request.getSort() != null ? request.getSort() : 0);
        dept.setLevel(level);
        dept.setMemberCount(0);
        dept.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        deptMapper.insert(dept);

        return toDeptVO(dept);
    }

    @Override
    @Transactional
    public DeptVO updateDept(String id, DeptFormData request) {
        Department dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ErrorCode.DEPT_NOT_FOUND);
        }

        // 检查同级重名
        if (request.getName() != null) {
            Department existing = deptMapper.selectByNameAndParentId(
                    request.getName(),
                    request.getParentId() != null ? request.getParentId() : dept.getParentId(),
                    id);
            if (existing != null) {
                throw new BusinessException(ErrorCode.DEPT_NAME_EXISTS);
            }
            dept.setName(request.getName());
        }
        if (request.getLeaderId() != null) dept.setLeaderId(request.getLeaderId());
        if (request.getSort() != null) dept.setSortOrder(request.getSort());
        if (request.getStatus() != null) dept.setStatus(request.getStatus());

        deptMapper.update(dept);
        return toDeptVO(dept);
    }

    @Override
    @Transactional
    public boolean deleteDept(String id) {
        Department dept = deptMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ErrorCode.DEPT_NOT_FOUND);
        }
        // 禁止删除顶级部门
        if (dept.getParentId() == null) {
            throw new BusinessException(ErrorCode.DEPT_IS_ROOT);
        }

        // 递归删除所有子部门
        List<String> childIds = deptMapper.selectAllChildIds(id);

        // 将所有相关用户的 dept_id 置空
        List<String> allDeptIds = new ArrayList<>();
        allDeptIds.add(id);
        if (childIds != null) {
            allDeptIds.addAll(childIds);
        }

        // 清理 user_managed_departments
        for (String deptId : allDeptIds) {
            userManagedDeptMapper.deleteByDeptId(deptId);
        }

        // 删除子部门（从叶子到根）
        if (childIds != null) {
            Collections.reverse(childIds);
            for (String childId : childIds) {
                deptMapper.deleteById(childId);
            }
        }
        deptMapper.deleteById(id);

        return true;
    }

    private DeptVO toDeptVO(Department dept) {
        DeptVO vo = new DeptVO();
        vo.setId(dept.getId());
        vo.setName(dept.getName());
        vo.setParentId(dept.getParentId());
        vo.setLeaderId(dept.getLeaderId());
        vo.setSort(dept.getSortOrder());
        vo.setLevel(dept.getLevel());
        vo.setMemberCount(dept.getMemberCount());
        vo.setStatus(dept.getStatus());

        if (dept.getLeaderId() != null) {
            User leader = userMapper.selectById(dept.getLeaderId());
            if (leader != null) {
                vo.setLeaderName(leader.getName());
            }
        }

        if (dept.getCreatedAt() != null) vo.setCreatedAt(dept.getCreatedAt().format(DTF));
        if (dept.getUpdatedAt() != null) vo.setUpdatedAt(dept.getUpdatedAt().format(DTF));

        return vo;
    }
}
