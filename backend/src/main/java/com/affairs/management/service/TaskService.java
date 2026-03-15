package com.affairs.management.service;

import com.affairs.management.common.PaginatedData;
import com.affairs.management.dto.request.*;
import com.affairs.management.dto.response.ProcessRecordVO;
import com.affairs.management.dto.response.TaskDetailResponse;
import com.affairs.management.dto.response.TaskVO;

import java.util.List;

public interface TaskService {
    TaskVO createTask(CreateTaskRequest request, String currentUserId);
    PaginatedData<TaskVO> getTaskList(TaskListParams params, String currentUserId, String role, List<String> managedDeptIds);
    TaskDetailResponse getTaskDetail(String taskId);
    TaskVO acceptTask(String taskId, String currentUserId);
    ProcessRecordVO addProcessRecord(String taskId, ProcessRequest request, String currentUserId);
    TaskVO submitTask(String taskId, ProcessRequest request, String currentUserId);
    TaskVO approveTask(String taskId, ApproveRequest request, String currentUserId);
    TaskVO rejectTask(String taskId, RejectRequest request, String currentUserId);
    TaskVO reassignTask(String taskId, ReassignRequest request, String currentUserId);
    TaskVO cancelTask(String taskId, CancelRequest request, String currentUserId);
    List<ProcessRecordVO> getProcessRecords(String taskId);
}
