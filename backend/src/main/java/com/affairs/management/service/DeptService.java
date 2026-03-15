package com.affairs.management.service;

import com.affairs.management.dto.request.DeptFormData;
import com.affairs.management.dto.response.DeptVO;
import com.affairs.management.dto.response.OrgTreeNode;
import com.affairs.management.entity.Department;

import java.util.List;

public interface DeptService {
    List<OrgTreeNode> getOrgTree();
    List<DeptVO> getDeptList(String parentId);
    DeptVO createDept(DeptFormData request);
    DeptVO updateDept(String id, DeptFormData request);
    boolean deleteDept(String id);
}
