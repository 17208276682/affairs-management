// ============================================
// 事务 API
// ============================================
import { get, post, put } from './request'
import type {
  Task, TaskProcessRecord, CreateTaskRequest,
  TaskListParams, PaginatedData,
} from '@/types'

/** 创建事务 */
export function createTaskApi(data: CreateTaskRequest) {
  return post<Task>('/task', data)
}

/** 获取事务列表 */
export function getTaskListApi(params: TaskListParams) {
  return get<PaginatedData<Task>>('/task/list', params)
}

/** 获取事务详情 */
export function getTaskDetailApi(id: string) {
  return get<{ task: Task; processRecords: TaskProcessRecord[] }>(`/task/${id}`)
}

/** 接收事务 */
export function acceptTaskApi(id: string) {
  return put<Task>(`/task/${id}/accept`)
}

/** 提交处理记录 */
export function addProcessRecordApi(id: string, data: { content: string; attachments?: any[] }) {
  return put<TaskProcessRecord>(`/task/${id}/process`, data)
}

/** 提交处理结果 */
export function submitTaskApi(id: string, data: { content: string; attachments?: any[] }) {
  return put<Task>(`/task/${id}/submit`, data)
}

/** 审核通过 */
export function approveTaskApi(id: string, data: { comment: string }) {
  return put<Task>(`/task/${id}/approve`, data)
}

/** 审核驳回 */
export function rejectTaskApi(id: string, data: { comment: string; reason: string }) {
  return put<Task>(`/task/${id}/reject`, data)
}

/** 向下分派 */
export function reassignTaskApi(id: string, data: { executorId: string }) {
  return post<Task>(`/task/${id}/reassign`, data)
}

/** 取消事务 */
export function cancelTaskApi(id: string, data: { reason: string }) {
  return put<Task>(`/task/${id}/cancel`, data)
}

/** 获取处理记录列表 */
export function getProcessRecordsApi(id: string) {
  return get<TaskProcessRecord[]>(`/task/${id}/records`)
}
