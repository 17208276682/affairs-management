package com.affairs.management.service;

import com.affairs.management.common.PaginatedData;
import com.affairs.management.dto.request.MemberFormData;
import com.affairs.management.dto.request.MemberListParams;
import com.affairs.management.dto.response.UserVO;

import java.util.List;

public interface MemberService {
    PaginatedData<UserVO> getMemberList(MemberListParams params);
    UserVO createMember(MemberFormData request);
    UserVO updateMember(String id, MemberFormData request);
    boolean deleteMember(String id);
    List<UserVO> getSelectableMembers(List<String> deptIds, String scope, String currentUserId, String currentRole, String currentDeptId);
}
