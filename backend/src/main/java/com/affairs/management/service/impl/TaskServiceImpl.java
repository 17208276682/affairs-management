package com.affairs.management.service.impl;

import com.affairs.management.common.BusinessException;
import com.affairs.management.common.ErrorCode;
import com.affairs.management.common.PaginatedData;
import com.affairs.management.dto.request.*;
import com.affairs.management.dto.response.AttachmentVO;
import com.affairs.management.dto.response.ProcessRecordVO;
import com.affairs.management.dto.response.TaskDetailResponse;
import com.affairs.management.dto.response.TaskVO;
import com.affairs.management.entity.*;
import com.affairs.management.enums.UrgencyType;
import com.affairs.management.mapper.*;
import com.affairs.management.service.NotificationService;
import com.affairs.management.service.TaskService;
import com.affairs.management.mapper.UserManagedDeptMapper;
import com.affairs.management.util.IdGenerator;
import com.affairs.management.util.TaskLevelCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final TaskProcessRecordMapper recordMapper;
    private final TaskAttachmentMapper taskAttachmentMapper;
    private final RecordAttachmentMapper recordAttachmentMapper;
    private final AttachmentMapper attachmentMapper;
    private final UserMapper userMapper;
    private final DeptMapper deptMapper;
    private final UserManagedDeptMapper userManagedDeptMapper;
    private final NotificationService notificationService;
    private final IdGenerator idGenerator;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public TaskVO createTask(CreateTaskRequest request, String currentUserId, String currentRole, String currentDeptId) {
        User assigner = userMapper.selectById(currentUserId);
        User executor = userMapper.selectById(request.getExecutorId());
        if (executor == null) {
            throw new BusinessException(ErrorCode.TASK_EXECUTOR_INVALID);
        }
        validateAssignableExecutor(assigner, executor, currentRole, currentDeptId);

        // 解析紧急类型
        UrgencyType urgencyType = UrgencyType.fromCode(request.getUrgencyType());
        int importance = urgencyType.getImportance();
        int urgency = urgencyType.getUrgency();
        String level = TaskLevelCalculator.calculate(importance, urgency);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime responseDeadline = now.plusHours(TaskLevelCalculator.getResponseHours(level));
        LocalDateTime completionDeadline;
        if (request.getCompletionDeadline() != null && !request.getCompletionDeadline().isEmpty()) {
            String raw = request.getCompletionDeadline().trim();
            // 兼容 ISO 格式 (带T) 和普通格式 (带空格)
            if (raw.contains("T")) {
                completionDeadline = LocalDateTime.parse(raw);
            } else {
                completionDeadline = LocalDateTime.parse(raw, DTF);
            }
        } else {
            completionDeadline = now.plusHours(TaskLevelCalculator.getCompletionHours(level));
        }

        // 生成标题
        String title = request.getDescription();
        if (title.length() > 50) {
            title = title.substring(0, 50) + "...";
        }

        // 创建任务
        Task task = new Task();
        task.setId(idGenerator.nextTaskId());
        task.setTitle(title);
        task.setDescription(request.getDescription());
        task.setAssignerId(currentUserId);
        task.setExecutorId(request.getExecutorId());
        task.setImportance(importance);
        task.setUrgency(urgency);
        task.setLevel(level);
        task.setResponseDeadline(responseDeadline);
        task.setCompletionDeadline(completionDeadline);
        task.setStatus("pending");
        task.setAssignerRole(currentRole);
        task.setAssignerDeptId(currentDeptId);
        task.setExecutorContextDeptId(resolveExecutorContextDeptId(executor, currentDeptId));
        taskMapper.insert(task);

        // 关联附件
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            taskAttachmentMapper.batchInsert(task.getId(), request.getAttachmentIds());
        }

        // 创建流程记录
        TaskProcessRecord record = new TaskProcessRecord();
        record.setId(idGenerator.nextRecordId());
        record.setTaskId(task.getId());
        record.setOperatorId(currentUserId);
        record.setAction("create");
        record.setContent("下发给 " + executor.getName());
        recordMapper.insert(record);

        // 通知执行人
        notificationService.createNotification(
                executor.getId(),
                "新事务指派",
                assigner.getName() + " 给您指派了新事务：" + title,
                "task_assigned",
                task.getId()
        );

        return toTaskVO(task);
    }

    @Override
    public PaginatedData<TaskVO> getTaskList(TaskListParams params, String currentUserId,
                                              String role, String currentDeptId, List<String> managedDeptIds) {
        // 获取管辖范围内的用户IDs（用于 scope 查询）
        List<String> managedUserIds = null;
        if ("scope".equals(params.getType()) && managedDeptIds != null && !managedDeptIds.isEmpty()) {
            List<User> managedUsers = userMapper.selectByDeptIds(managedDeptIds);
            managedUserIds = managedUsers.stream().map(User::getId).collect(Collectors.toList());
        }

        int offset = (params.getPage() - 1) * params.getPageSize();
        List<Task> tasks = taskMapper.selectTaskList(
            params.getType(), currentUserId, role, currentDeptId, managedUserIds,
                params.getStatus(), params.getLevel(), params.getKeyword(),
                offset, params.getPageSize());

        long total = taskMapper.countTaskList(
            params.getType(), currentUserId, role, currentDeptId, managedUserIds,
                params.getStatus(), params.getLevel(), params.getKeyword());

        List<TaskVO> voList = tasks.stream().map(this::toTaskVO).collect(Collectors.toList());
        return PaginatedData.of(voList, total, params.getPage(), params.getPageSize());
    }

    @Override
    public TaskDetailResponse getTaskDetail(String taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        List<ProcessRecordVO> processRecords = getProcessRecords(taskId);
        List<String> descendantIds = taskMapper.selectAllDescendantIds(taskId);
        if (descendantIds != null && !descendantIds.isEmpty()) {
            List<ProcessRecordVO> childRecords = recordMapper.selectByTaskIds(descendantIds).stream()
                    .filter(r -> !"create".equals(r.getAction()))
                    .map(this::toRecordVO)
                    .collect(Collectors.toList());
            processRecords.addAll(childRecords);
            processRecords.sort(Comparator.comparing(ProcessRecordVO::getCreatedAt));
        }

        TaskDetailResponse response = new TaskDetailResponse();
        response.setTask(toTaskVO(task));
        response.setProcessRecords(processRecords);
        return response;
    }

    @Override
    @Transactional
    public TaskVO acceptTask(String taskId, String currentUserId) {
        Task task = taskMapper.selectById(taskId);
        validateTaskOperation(task, currentUserId, true, "pending");

        task.setStatus("accepted");
        task.setResponseTime(LocalDateTime.now());
        taskMapper.update(task);

        // 流程记录
        createRecord(taskId, currentUserId, "accept", "已接收事务");

        // 通知下达人
        User executor = userMapper.selectById(currentUserId);
        notificationService.createNotification(
                task.getAssignerId(), "事务已接收",
                executor.getName() + " 已接收事务：" + task.getTitle(),
                "task_accepted", taskId);

        return toTaskVO(task);
    }

    @Override
    @Transactional
    public ProcessRecordVO addProcessRecord(String taskId, ProcessRequest request, String currentUserId) {
        Task task = taskMapper.selectById(taskId);
        validateTaskOperation(task, currentUserId, true, "pending", "accepted", "in_progress", "overdue");

        // 若为 pending 或 accepted 状态，自动推进到 in_progress
        if ("pending".equals(task.getStatus()) || "accepted".equals(task.getStatus())) {
            if ("pending".equals(task.getStatus())) {
                task.setResponseTime(LocalDateTime.now());
            }
            task.setStatus("in_progress");
            taskMapper.update(task);
        }

        String recordId = idGenerator.nextRecordId();
        TaskProcessRecord record = new TaskProcessRecord();
        record.setId(recordId);
        record.setTaskId(taskId);
        record.setOperatorId(currentUserId);
        record.setAction("process");
        record.setContent(request.getContent());
        recordMapper.insert(record);

        // 关联附件
        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            recordAttachmentMapper.batchInsert(recordId, request.getAttachmentIds());
        }

        return toRecordVO(record);
    }

    @Override
    @Transactional
    public TaskVO submitTask(String taskId, ProcessRequest request, String currentUserId) {
        Task task = taskMapper.selectById(taskId);
        validateTaskOperation(task, currentUserId, true, "pending", "accepted", "in_progress", "overdue");

        // 检查子任务状态 - 所有直接子任务必须已提交
        List<String> childTaskIds = taskMapper.selectChildTaskIds(taskId);
        if (childTaskIds != null && !childTaskIds.isEmpty()) {
            for (String childId : childTaskIds) {
                Task childTask = taskMapper.selectById(childId);
                if (childTask != null && !"submitted".equals(childTask.getStatus())
                        && !"approved".equals(childTask.getStatus())) {
                    throw new BusinessException(ErrorCode.TASK_STATUS_INVALID,
                            "存在未提交的子任务，请等待所有子任务提交后再操作");
                }
            }
        }

        // 如果是 pending 状态直接提交，自动记录接收时间
        if ("pending".equals(task.getStatus())) {
            task.setResponseTime(LocalDateTime.now());
        }
        task.setStatus("submitted");
        taskMapper.update(task);

        String recordId = idGenerator.nextRecordId();
        TaskProcessRecord record = new TaskProcessRecord();
        record.setId(recordId);
        record.setTaskId(taskId);
        record.setOperatorId(currentUserId);
        record.setAction("submit");
        User submitTarget = userMapper.selectById(task.getAssignerId());
        record.setContent(request.getContent() != null && !request.getContent().isBlank() ? request.getContent() : "提交给 " + (submitTarget != null ? submitTarget.getName() : "上级"));
        recordMapper.insert(record);

        if (request.getAttachmentIds() != null && !request.getAttachmentIds().isEmpty()) {
            recordAttachmentMapper.batchInsert(recordId, request.getAttachmentIds());
        }
        User executor = userMapper.selectById(currentUserId);
        notificationService.createNotification(
                task.getAssignerId(), "事务成果已提交",
                executor.getName() + " 提交了事务成果：" + task.getTitle(),
                "task_submitted", taskId);

        return toTaskVO(task);
    }

    @Override
    @Transactional
    public TaskVO approveTask(String taskId, ApproveRequest request, String currentUserId) {
        Task task = taskMapper.selectById(taskId);
        validateTaskOperation(task, currentUserId, false, "submitted");
        if (task.getParentTaskId() != null) {
            throw new BusinessException(ErrorCode.TASK_STATUS_INVALID, "转交子任务不能直接审核，请逐级提交后由根任务审核");
        }

        task.setStatus("approved");
        task.setCompletionTime(LocalDateTime.now());
        taskMapper.update(task);

        createRecord(taskId, currentUserId, "approve",
                request.getComment() != null ? request.getComment() : "审核通过");

        // 级联通过子任务
        cascadeStatusChange(taskId, "approved", currentUserId, "approve",
                request.getComment() != null ? request.getComment() : "审核通过");

        // 通知执行人
        notificationService.createNotification(
                task.getExecutorId(), "事务已通过审核",
                "您的事务已通过审核：" + task.getTitle(),
                "task_approved", taskId);

        return toTaskVO(task);
    }

    @Override
    @Transactional
    public TaskVO rejectTask(String taskId, RejectRequest request, String currentUserId) {
        Task task = taskMapper.selectById(taskId);
        validateTaskOperation(task, currentUserId, false, "submitted");
        if (task.getParentTaskId() != null) {
            throw new BusinessException(ErrorCode.TASK_STATUS_INVALID, "转交子任务不能直接审核，请逐级提交后由根任务审核");
        }

        task.setStatus("rejected");
        taskMapper.update(task);

        String content = "驳回";
        if (request.getReason() != null) {
            content += "，原因：" + request.getReason();
        }
        if (request.getComment() != null) {
            content += "，评语：" + request.getComment();
        }
        createRecord(taskId, currentUserId, "reject", content);

        // 级联驳回子任务
        cascadeStatusChange(taskId, "rejected", currentUserId, "reject", content);

        // 通知执行人
        notificationService.createNotification(
                task.getExecutorId(), "事务被驳回",
                "您的事务被驳回：" + task.getTitle() + (request.getReason() != null ? "，原因：" + request.getReason() : ""),
                "task_rejected", taskId);

        return toTaskVO(task);
    }

    @Override
    @Transactional
    public TaskVO reassignTask(String taskId, ReassignRequest request, String currentUserId, String currentRole, String currentDeptId) {
        Task parentTask = taskMapper.selectById(taskId);
        if (parentTask == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }

        User assigner = userMapper.selectById(currentUserId);
        User newExecutor = userMapper.selectById(request.getExecutorId());
        if (newExecutor == null) {
            throw new BusinessException(ErrorCode.TASK_EXECUTOR_INVALID);
        }
        validateAssignableExecutor(assigner, newExecutor, currentRole, currentDeptId);

        // 创建子任务
        Task childTask = new Task();
        childTask.setId(idGenerator.nextTaskId());
        childTask.setTitle(parentTask.getTitle());
        childTask.setDescription(parentTask.getDescription());
        childTask.setAssignerId(currentUserId);
        childTask.setExecutorId(request.getExecutorId());
        childTask.setImportance(parentTask.getImportance());
        childTask.setUrgency(parentTask.getUrgency());
        childTask.setLevel(parentTask.getLevel());
        childTask.setResponseDeadline(parentTask.getResponseDeadline());
        childTask.setCompletionDeadline(parentTask.getCompletionDeadline());
        childTask.setStatus("pending");
        childTask.setParentTaskId(taskId);
        childTask.setAssignerRole(currentRole);
        childTask.setAssignerDeptId(currentDeptId);
        childTask.setExecutorContextDeptId(resolveExecutorContextDeptId(newExecutor, currentDeptId));
        taskMapper.insert(childTask);

        // 复制父任务的附件到子任务
        List<String> parentAttachmentIds = taskAttachmentMapper.selectAttachmentIdsByTaskId(taskId);
        if (parentAttachmentIds != null && !parentAttachmentIds.isEmpty()) {
            taskAttachmentMapper.batchInsert(childTask.getId(), parentAttachmentIds);
        }

        // 流程记录（父任务）
        createRecord(taskId, currentUserId, "reassign",
                "转交给 " + newExecutor.getName());

        // 流程记录（子任务，让执行人能看到下发记录）
        createRecord(childTask.getId(), currentUserId, "create",
                "下发给 " + newExecutor.getName());

        // 通知新执行人
        notificationService.createNotification(
                request.getExecutorId(), "新事务指派",
                assigner.getName() + " 给您指派了新事务：" + childTask.getTitle(),
                "task_assigned", childTask.getId());

        return toTaskVO(childTask);
    }

    @Override
    @Transactional
    public TaskVO cancelTask(String taskId, CancelRequest request, String currentUserId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        if (!task.getAssignerId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.TASK_NO_PERMISSION);
        }

        task.setStatus("cancelled");
        taskMapper.update(task);

        String content = "取消事务";
        if (request.getReason() != null) {
            content += "，原因：" + request.getReason();
        }
        createRecord(taskId, currentUserId, "cancel", content);

        // 级联取消子任务
        cascadeStatusChange(taskId, "cancelled", currentUserId, "cancel", content);

        return toTaskVO(task);
    }

    @Override
    public List<ProcessRecordVO> getProcessRecords(String taskId) {
        List<TaskProcessRecord> records = recordMapper.selectByTaskId(taskId);
        return records.stream().map(this::toRecordVO).collect(Collectors.toList());
    }

    // ========== Private Helper Methods ==========

    private void validateTaskOperation(Task task, String userId, boolean isExecutor, String... validStatuses) {
        if (task == null) {
            throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        }
        if (isExecutor && !task.getExecutorId().equals(userId)) {
            throw new BusinessException(ErrorCode.TASK_NO_PERMISSION, "只有执行人可以执行此操作");
        }
        if (!isExecutor && !task.getAssignerId().equals(userId)) {
            throw new BusinessException(ErrorCode.TASK_NO_PERMISSION, "只有下达人可以执行此操作");
        }
        boolean statusValid = false;
        for (String s : validStatuses) {
            if (s.equals(task.getStatus())) {
                statusValid = true;
                break;
            }
        }
        if (!statusValid) {
            throw new BusinessException(ErrorCode.TASK_STATUS_INVALID,
                    "当前状态 [" + task.getStatus() + "] 不允许此操作");
        }
    }

    private void cascadeStatusChange(String parentTaskId, String newStatus, String operatorId, String action, String content) {
        List<String> descendantIds = taskMapper.selectAllDescendantIds(parentTaskId);
        if (descendantIds != null && !descendantIds.isEmpty()) {
            taskMapper.batchUpdateStatus(descendantIds, newStatus);
            // 在每个子孙任务上创建审核记录，确保下级执行人能看到最终审核结果
            for (String childId : descendantIds) {
                createRecord(childId, operatorId, action, content);
            }
        }
    }

    private void createRecord(String taskId, String operatorId, String action, String content) {
        TaskProcessRecord record = new TaskProcessRecord();
        record.setId(idGenerator.nextRecordId());
        record.setTaskId(taskId);
        record.setOperatorId(operatorId);
        record.setAction(action);
        record.setContent(content);
        recordMapper.insert(record);
    }

    private void validateAssignableExecutor(User assigner, User executor, String currentRole, String currentDeptId) {
        if (assigner == null || executor == null) {
            throw new BusinessException(ErrorCode.TASK_EXECUTOR_INVALID);
        }
        if (assigner.getId().equals(executor.getId())) {
            throw new BusinessException(ErrorCode.TASK_EXECUTOR_INVALID, "不能给自己下发任务");
        }
        if (currentRole == null || currentRole.isBlank()) {
            throw new BusinessException(ErrorCode.TASK_NO_PERMISSION, "当前角色无下发权限");
        }
        if (currentDeptId == null || currentDeptId.isBlank()) {
            throw new BusinessException(ErrorCode.TASK_EXECUTOR_INVALID, "当前角色未绑定所属部门");
        }

        // 不能下发给总经办（ceo/director）
        if ("ceo".equals(executor.getRole()) || "director".equals(executor.getRole())) {
            throw new BusinessException(ErrorCode.TASK_EXECUTOR_INVALID, "不能给总经办人员下发任务");
        }

        List<String> assignerManagedDeptIds = userManagedDeptMapper.selectDeptIdsByUserId(assigner.getId());
        boolean valid = false;

        if ("ceo".equals(currentRole) || "director".equals(currentRole)) {
            // 路径1: 作为 ceo/director，下发给下一级部门的负责人
            List<Department> childDepts = deptMapper.selectChildren(currentDeptId);
            for (Department childDept : childDepts) {
                if (isDeptHead(executor, childDept.getId())) {
                    valid = true;
                    break;
                }
            }

            // 路径2: 作为兼任部门负责人，下发给管理部门的下级
            if (!valid) {
                for (String managedDeptId : assignerManagedDeptIds) {
                    if (isValidNextLevelDispatch(assigner, executor, managedDeptId)) {
                        valid = true;
                        break;
                    }
                }
            }
        } else if ("manager".equals(currentRole)) {
            // 路径3: 作为 manager，下发给本部门下级
            valid = isValidNextLevelDispatch(assigner, executor, currentDeptId);
        }

        if (!valid) {
            throw new BusinessException(ErrorCode.TASK_EXECUTOR_INVALID, "只能下发给直接下一级人员");
        }
    }

    /**
     * 判断用户是否是某部门的负责人
     */
    private boolean isDeptHead(User user, String deptId) {
        // 方式1: role=manager 且直接属于该部门
        if ("manager".equals(user.getRole()) && deptId.equals(user.getDeptId())) {
            return true;
        }
        // 方式2: ceo/director 通过 user_managed_departments 兼任负责人
        if ("ceo".equals(user.getRole()) || "director".equals(user.getRole())) {
            List<String> managedDepts = userManagedDeptMapper.selectDeptIdsByUserId(user.getId());
            return managedDepts.contains(deptId);
        }
        return false;
    }

    /**
     * 判断从某部门向下派发是否合法（只能一层）
     */
    private boolean isValidNextLevelDispatch(User assigner, User executor, String deptId) {
        List<Department> childDepts = deptMapper.selectChildren(deptId);
        if (!childDepts.isEmpty()) {
            // 有子部门 → 只能下发给子部门负责人
            for (Department childDept : childDepts) {
                if (isDeptHead(executor, childDept.getId())) {
                    return true;
                }
            }
        } else {
            // 没有子部门 → 只能下发给本部门 staff
            if ("staff".equals(executor.getRole()) && deptId.equals(executor.getDeptId())
                    && !executor.getId().equals(assigner.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 确定执行人应在哪个部门上下文下看到此任务。
     * 对于 staff/manager：直接取其所属部门。
     * 对于 ceo/director 兼任部门负责人：找出其管理的、且是下达人上下文部门子部门的那个部门。
     */
    private String resolveExecutorContextDeptId(User executor, String assignerDeptId) {
        if ("staff".equals(executor.getRole()) || "manager".equals(executor.getRole())) {
            return executor.getDeptId();
        }
        // ceo/director 作为执行人：找到他管理的、属于 assignerDeptId 的子部门
        List<String> managedDepts = userManagedDeptMapper.selectDeptIdsByUserId(executor.getId());
        List<Department> children = deptMapper.selectChildren(assignerDeptId);
        for (Department child : children) {
            if (managedDepts.contains(child.getId())) {
                return child.getId();
            }
        }
        return executor.getDeptId();
    }

    private TaskVO toTaskVO(Task task) {
        TaskVO vo = new TaskVO();
        vo.setId(task.getId());
        vo.setTitle(task.getTitle());
        vo.setDescription(task.getDescription());
        vo.setAssignerId(task.getAssignerId());
        vo.setExecutorId(task.getExecutorId());
        vo.setImportance(task.getImportance());
        vo.setUrgency(task.getUrgency());
        vo.setLevel(task.getLevel());
        vo.setStatus(task.getStatus());
        vo.setParentTaskId(task.getParentTaskId());

        // 下达人信息
        User assigner = userMapper.selectById(task.getAssignerId());
        if (assigner != null) {
            vo.setAssignerName(assigner.getName());
            vo.setAssignerPosition(assigner.getPosition());
            if (assigner.getDeptId() != null) {
                Department dept = deptMapper.selectById(assigner.getDeptId());
                vo.setAssignerDept(dept != null ? dept.getName() : null);
            }
        }
        // 执行人信息
        User executor = userMapper.selectById(task.getExecutorId());
        if (executor != null) {
            vo.setExecutorName(executor.getName());
            if (executor.getDeptId() != null) {
                Department dept = deptMapper.selectById(executor.getDeptId());
                vo.setExecutorDept(dept != null ? dept.getName() : null);
            }
        }

        // 时间格式化
        if (task.getResponseDeadline() != null) vo.setResponseDeadline(task.getResponseDeadline().format(DTF));
        if (task.getCompletionDeadline() != null) vo.setCompletionDeadline(task.getCompletionDeadline().format(DTF));
        if (task.getResponseTime() != null) vo.setResponseTime(task.getResponseTime().format(DTF));
        if (task.getCompletionTime() != null) vo.setCompletionTime(task.getCompletionTime().format(DTF));
        if (task.getCreatedAt() != null) vo.setCreatedAt(task.getCreatedAt().format(DTF));
        if (task.getUpdatedAt() != null) vo.setUpdatedAt(task.getUpdatedAt().format(DTF));

        // 附件
        List<Attachment> attachments = attachmentMapper.selectByTaskId(task.getId());
        vo.setAttachments(attachments.stream().map(this::toAttachmentVO).collect(Collectors.toList()));

        // 子任务IDs
        List<String> childIds = taskMapper.selectChildTaskIds(task.getId());
        vo.setChildTaskIds(childIds != null ? childIds : Collections.emptyList());

        return vo;
    }

    private ProcessRecordVO toRecordVO(TaskProcessRecord record) {
        ProcessRecordVO vo = new ProcessRecordVO();
        vo.setId(record.getId());
        vo.setTaskId(record.getTaskId());
        vo.setOperatorId(record.getOperatorId());
        vo.setAction(record.getAction());
        vo.setContent(record.getContent());
        if (record.getCreatedAt() != null) {
            vo.setCreatedAt(record.getCreatedAt().format(DTF));
        }

        User operator = userMapper.selectById(record.getOperatorId());
        if (operator != null) {
            vo.setOperatorName(operator.getName());
            vo.setOperatorAvatar(operator.getAvatar());
        }

        // 附件
        List<Attachment> attachments = attachmentMapper.selectByRecordId(record.getId());
        vo.setAttachments(attachments.stream().map(this::toAttachmentVO).collect(Collectors.toList()));

        return vo;
    }

    private AttachmentVO toAttachmentVO(Attachment att) {
        AttachmentVO vo = new AttachmentVO();
        vo.setId(att.getId());
        vo.setName(att.getFileName());
        vo.setUrl("/api/files/" + att.getId() + "/preview");
        vo.setType(att.getMimeType());
        vo.setSize(att.getFileSize());
        if (att.getUploadedAt() != null) {
            vo.setUploadedAt(att.getUploadedAt().format(DTF));
        }
        return vo;
    }
}
