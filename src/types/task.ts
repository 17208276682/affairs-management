// ============================================
// 事务 相关类型
// ============================================

/** 事务状态 */
export type TaskStatus =
  | 'pending'      // 待接收
  | 'accepted'     // 已接收
  | 'in_progress'  // 执行中
  | 'submitted'    // 已提交结果
  | 'approved'     // 已通过
  | 'rejected'     // 已驳回
  | 'completed'    // 已完成
  | 'overdue'      // 已逾期
  | 'cancelled'    // 已取消

/** 重要性 1-3 */
export type Importance = 1 | 2 | 3

/** 紧急度 1-3 */
export type Urgency = 1 | 2 | 3

/** 事务级别 */
export type TaskLevel = 'A' | 'B' | 'C' | 'D'

/** 操作类型 */
export type ProcessAction =
  | 'create'    // 创建
  | 'assign'    // 指派
  | 'accept'    // 接收
  | 'process'   // 处理过程
  | 'submit'    // 提交结果
  | 'approve'   // 审核通过
  | 'reject'    // 驳回
  | 'reassign'  // 向下分派
  | 'cancel'    // 取消
  | 'comment'   // 备注

/** 附件 */
export interface Attachment {
  id: string
  name: string
  url: string
  size: number
  type: string
  uploadedAt: string
}

/** 事务 */
export interface Task {
  id: string
  title: string
  description: string

  // 下达人信息
  assignerId: string
  assignerName: string
  assignerDept: string
  assignerPosition: string

  // 执行人信息
  executorId: string
  executorName: string
  executorDept: string

  // 级别
  importance: Importance
  urgency: Urgency
  level: TaskLevel

  // 时间
  responseDeadline: string
  completionDeadline: string
  responseTime: string | null
  completionTime: string | null

  // 状态
  status: TaskStatus

  // 附件
  attachments: Attachment[]

  // 关联
  parentTaskId: string | null
  childTaskIds: string[]

  createdAt: string
  updatedAt: string
}

/** 事务处理记录 */
export interface TaskProcessRecord {
  id: string
  taskId: string
  operatorId: string
  operatorName: string
  operatorAvatar: string
  action: ProcessAction
  content: string
  attachments: Attachment[]
  createdAt: string
}

/** 事务紧急性 */
export type TaskUrgencyType = 'important_urgent' | 'important_not_urgent' | 'not_important_urgent' | 'not_important_not_urgent'

/** 完成时间单位 */
export type TimeUnit = 'minute' | 'hour' | 'day' | 'month' | 'year'

/** 创建事务请求 */
export interface CreateTaskRequest {
  description: string
  urgencyType: TaskUrgencyType
  completionDeadline: string
  duration?: number
  durationUnit?: TimeUnit
  executorId: string
  attachments?: Attachment[]
}

/** 事务列表查询参数 */
export interface TaskListParams {
  type: 'assigned' | 'received' | 'all' | 'scope' | 'todo'
  status?: TaskStatus | ''
  level?: TaskLevel | ''
  keyword?: string
  page: number
  pageSize: number
}
