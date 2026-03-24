// ============================================
// 常量与枚举定义
// ============================================
import type { TaskLevel, Importance, Urgency } from '@/types'

/** 事务状态映射 */
export const TASK_STATUS_MAP: Record<string, { label: string; color: string; bgColor: string }> = {
  pending:     { label: '待接收', color: '#F59E0B', bgColor: '#FFFBEB' },
  accepted:    { label: '已接收', color: '#4F6EF7', bgColor: '#EEF1FE' },
  in_progress: { label: '执行中', color: '#3B82F6', bgColor: '#EFF6FF' },
  submitted:   { label: '已提交', color: '#8B5CF6', bgColor: '#F5F3FF' },
  approved:    { label: '已通过', color: '#22C55E', bgColor: '#ECFDF5' },
  rejected:    { label: '已驳回', color: '#EF4444', bgColor: '#FEF2F2' },
  completed:   { label: '已完成', color: '#10B981', bgColor: '#ECFDF5' },
  overdue:     { label: '已逾期', color: '#DC2626', bgColor: '#FEF2F2' },
  cancelled:   { label: '已取消', color: '#9CA3AF', bgColor: '#F3F4F6' },
}

/** 事务级别映射 */
export const TASK_LEVEL_MAP: Record<TaskLevel, { label: string; color: string; bgColor: string; description: string }> = {
  A: { label: '重要紧急',     color: '#EF4444', bgColor: '#FEF2F2', description: '重要且紧急，需立即处理' },
  B: { label: '不重要紧急',   color: '#F59E0B', bgColor: '#FFFBEB', description: '不重要但紧急，需尽快处理' },
  C: { label: '重要不紧急',   color: '#4F6EF7', bgColor: '#EEF1FE', description: '重要但不紧急，按计划推进' },
  D: { label: '不重要不紧急', color: '#22C55E', bgColor: '#ECFDF5', description: '不重要不紧急，低优先级' },
}

/** 级别计算矩阵：importance_urgency => level */
export const LEVEL_MATRIX: Record<string, TaskLevel> = {
  '3_3': 'A', '3_2': 'A', '2_3': 'A',
  '3_1': 'B', '2_2': 'B', '1_3': 'B',
  '2_1': 'C', '1_2': 'C',
  '1_1': 'D',
}

/** 响应时间要求（小时） */
export const RESPONSE_HOURS: Record<TaskLevel, number> = { A: 1, B: 2, C: 4, D: 8 }

/** 处理时限要求（小时） */
export const COMPLETION_HOURS: Record<TaskLevel, number> = { A: 4, B: 8, C: 24, D: 48 }

/** 重要性选项 */
export const IMPORTANCE_OPTIONS = [
  { value: 1 as Importance, label: '一般', color: '#22C55E' },
  { value: 2 as Importance, label: '重要', color: '#F59E0B' },
  { value: 3 as Importance, label: '非常重要', color: '#EF4444' },
]

/** 紧急度选项 */
export const URGENCY_OPTIONS = [
  { value: 1 as Urgency, label: '不紧急', color: '#22C55E' },
  { value: 2 as Urgency, label: '紧急', color: '#F59E0B' },
  { value: 3 as Urgency, label: '非常紧急', color: '#EF4444' },
]

/** 操作类型映射 */
export const PROCESS_ACTION_MAP: Record<string, { label: string; icon: string; color: string }> = {
  create:   { label: '创建事务', icon: 'Plus',           color: '#4F6EF7' },
  assign:   { label: '指派事务', icon: 'Position',       color: '#4F6EF7' },
  accept:   { label: '接收事务', icon: 'Check',          color: '#22C55E' },
  process:  { label: '处理记录', icon: 'EditPen',        color: '#3B82F6' },
  submit:   { label: '提交结果', icon: 'Upload',         color: '#8B5CF6' },
  approve:  { label: '审核通过', icon: 'CircleCheck',    color: '#22C55E' },
  reject:   { label: '审核驳回', icon: 'CircleClose',    color: '#EF4444' },
  reassign: { label: '向下分派', icon: 'Share',          color: '#F59E0B' },
  cancel:   { label: '取消事务', icon: 'Close',          color: '#9CA3AF' },
  comment:  { label: '备注',     icon: 'ChatDotSquare',  color: '#6B7280' },
}

/** 角色映射 */
export const ROLE_MAP: Record<string, string> = {
  admin: '管理员',
  ceo: '总经理',
  director: '副总经理',
  manager: '负责人',
  staff: '普通员工',
}

/** 计算事务级别 */
export function calculateLevel(importance: Importance, urgency: Urgency): TaskLevel {
  return LEVEL_MATRIX[`${importance}_${urgency}`] || 'D'
}
