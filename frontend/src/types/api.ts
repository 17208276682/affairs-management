// ============================================
// API 通用响应类型
// ============================================

/** 通用 API 响应 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/** 分页数据 */
export interface PaginatedData<T = any> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

/** 分页 API 响应 */
export type PaginatedResponse<T = any> = ApiResponse<PaginatedData<T>>

/** 统计概览 */
export interface StatsOverview {
  totalTasks: number
  pendingTasks: number
  inProgressTasks: number
  completedTasks: number
  rejectedTasks: number
  overdueTasks: number
  cancelledTasks: number
  overdueRate: number
  completionRate: number
  avgResponseHours: number
  avgCompletionHours: number
  onTimeCompleted: number
  overdueCompleted: number
  failedReview: number
  todoTasks: number
  overdueUnfinished: number
  submittedTasks: number
}

/** 部门统计 */
export interface DeptStats {
  deptId: string
  deptName: string
  level?: number
  total: number
  completed: number
  overdue: number
  avgHours: number
}

/** 级别统计 */
export interface LevelStats {
  level: string
  total: number
  completed: number
  avgHours: number
}

/** 个人统计 */
export interface PersonStats {
  userId: string
  name: string
  deptId: string
  deptName: string
  totalTasks: number
  completedTasks: number
  overdueTasks: number
  completionRate: number
  avgResponseHours: number
  avgCompletionHours: number
  onTimeCompleted: number
  overdueCompleted: number
  failedReview: number
  todoTasks: number
  overdueUnfinished: number
  cancelledTasks: number
}

/** 趋势数据 */
export interface TrendData {
  dates: string[]
  created: number[]
  completed: number[]
}

/** 上传文件响应 */
export interface UploadResponse {
  id: string
  name: string
  url: string
  size: number
  type: string
  uploadedAt: string
}
