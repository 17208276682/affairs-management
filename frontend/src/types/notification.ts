// ============================================
// 通知 相关类型
// ============================================

/** 通知类型 */
export type NotificationType =
  | 'task_assigned'    // 事务指派
  | 'task_accepted'    // 事务接收
  | 'task_responded'   // 事务响应
  | 'task_completed'   // 事务完成
  | 'task_submitted'   // 结果提交
  | 'task_approved'    // 审核通过
  | 'task_rejected'    // 审核驳回
  | 'system'           // 系统通知

/** 通知 */
export interface Notification {
  id: string
  userId: string
  title: string
  content: string
  type: NotificationType
  relatedTaskId: string | null
  isRead: boolean
  createdAt: string
}
