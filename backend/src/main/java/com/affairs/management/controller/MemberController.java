package com.affairs.management.controller;

import com.affairs.management.common.ApiResponse;
import com.affairs.management.common.PaginatedData;
import com.affairs.management.dto.request.MemberFormData;
import com.affairs.management.dto.request.MemberListParams;
import com.affairs.management.dto.response.UserVO;
import com.affairs.management.security.SecurityUtils;
import com.affairs.management.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/list")
    public ApiResponse<PaginatedData<UserVO>> getMemberList(MemberListParams params) {
        return ApiResponse.success(memberService.getMemberList(params));
    }

    @PostMapping
    public ApiResponse<UserVO> createMember(@Valid @RequestBody MemberFormData request) {
        return ApiResponse.success(memberService.createMember(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserVO> updateMember(@PathVariable String id,
                                             @RequestBody MemberFormData request) {
        return ApiResponse.success(memberService.updateMember(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteMember(@PathVariable String id) {
        return ApiResponse.success(memberService.deleteMember(id));
    }

    @GetMapping("/selectable")
    public ApiResponse<List<UserVO>> getSelectableMembers(
            @RequestParam(required = false) List<String> deptIds,
            @RequestParam(required = false) String scope) {
        var user = SecurityUtils.getCurrentUser();
        return ApiResponse.success(
                memberService.getSelectableMembers(
                        deptIds, scope,
                        user.getUserId(), user.getRole(), user.getDeptId()));
    }
}
