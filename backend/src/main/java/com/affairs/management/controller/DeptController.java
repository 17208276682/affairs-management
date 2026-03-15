package com.affairs.management.controller;

import com.affairs.management.common.ApiResponse;
import com.affairs.management.dto.request.DeptFormData;
import com.affairs.management.dto.response.DeptVO;
import com.affairs.management.dto.response.OrgTreeNode;
import com.affairs.management.service.DeptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    @GetMapping("/org/tree")
    public ApiResponse<List<OrgTreeNode>> getOrgTree() {
        return ApiResponse.success(deptService.getOrgTree());
    }

    @GetMapping("/dept/list")
    public ApiResponse<List<DeptVO>> getDeptList(
            @RequestParam(required = false) String parentId) {
        return ApiResponse.success(deptService.getDeptList(parentId));
    }

    @PostMapping("/dept")
    public ApiResponse<DeptVO> createDept(@Valid @RequestBody DeptFormData request) {
        return ApiResponse.success(deptService.createDept(request));
    }

    @PutMapping("/dept/{id}")
    public ApiResponse<DeptVO> updateDept(@PathVariable String id,
                                           @RequestBody DeptFormData request) {
        return ApiResponse.success(deptService.updateDept(id, request));
    }

    @DeleteMapping("/dept/{id}")
    public ApiResponse<Boolean> deleteDept(@PathVariable String id) {
        return ApiResponse.success(deptService.deleteDept(id));
    }
}
