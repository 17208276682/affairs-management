package com.affairs.management.controller;

import com.affairs.management.common.ApiResponse;
import com.affairs.management.common.PaginatedData;
import com.affairs.management.dto.request.*;
import com.affairs.management.dto.response.ProcessRecordVO;
import com.affairs.management.dto.response.TaskDetailResponse;
import com.affairs.management.dto.response.TaskVO;
import com.affairs.management.security.SecurityUtils;
import com.affairs.management.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ApiResponse<TaskVO> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ApiResponse.success(
                taskService.createTask(request, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/list")
    public ApiResponse<PaginatedData<TaskVO>> getTaskList(TaskListParams params) {
        var user = SecurityUtils.getCurrentUser();
        return ApiResponse.success(
                taskService.getTaskList(params, user.getUserId(), user.getRole(), user.getManagedDeptIds()));
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskDetailResponse> getTaskDetail(@PathVariable String id) {
        return ApiResponse.success(taskService.getTaskDetail(id));
    }

    @PutMapping("/{id}/accept")
    public ApiResponse<TaskVO> acceptTask(@PathVariable String id) {
        return ApiResponse.success(
                taskService.acceptTask(id, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{id}/process")
    public ApiResponse<ProcessRecordVO> addProcessRecord(
            @PathVariable String id, @RequestBody ProcessRequest request) {
        return ApiResponse.success(
                taskService.addProcessRecord(id, request, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{id}/submit")
    public ApiResponse<TaskVO> submitTask(
            @PathVariable String id, @RequestBody ProcessRequest request) {
        return ApiResponse.success(
                taskService.submitTask(id, request, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<TaskVO> approveTask(
            @PathVariable String id, @RequestBody ApproveRequest request) {
        return ApiResponse.success(
                taskService.approveTask(id, request, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<TaskVO> rejectTask(
            @PathVariable String id, @RequestBody RejectRequest request) {
        return ApiResponse.success(
                taskService.rejectTask(id, request, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/{id}/reassign")
    public ApiResponse<TaskVO> reassignTask(
            @PathVariable String id, @Valid @RequestBody ReassignRequest request) {
        return ApiResponse.success(
                taskService.reassignTask(id, request, SecurityUtils.getCurrentUserId()));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<TaskVO> cancelTask(
            @PathVariable String id, @RequestBody CancelRequest request) {
        return ApiResponse.success(
                taskService.cancelTask(id, request, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/{id}/records")
    public ApiResponse<List<ProcessRecordVO>> getProcessRecords(@PathVariable String id) {
        return ApiResponse.success(taskService.getProcessRecords(id));
    }
}
