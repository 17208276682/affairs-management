// ============================================
// 格式化工具函数
// ============================================
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

/** 格式化日期时间 */
export function formatDateTime(date: string | Date | null | undefined): string {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

/** 格式化日期 */
export function formatDate(date: string | Date | null | undefined): string {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD')
}

/** 格式化时间 */
export function formatTime(date: string | Date | null | undefined): string {
  if (!date) return '-'
  return dayjs(date).format('HH:mm')
}

/** 相对时间（如：3小时前） */
export function formatRelativeTime(date: string | Date | null | undefined): string {
  if (!date) return '-'
  return dayjs(date).fromNow()
}

/** 格式化文件大小 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return `${(bytes / Math.pow(1024, i)).toFixed(i === 0 ? 0 : 1)} ${units[i]}`
}

/** 格式化时长（小时） */
export function formatDuration(hours: number): string {
  if (hours < 1) return `${Math.round(hours * 60)}分钟`
  if (hours < 24) return `${hours.toFixed(1)}小时`
  return `${(hours / 24).toFixed(1)}天`
}

/** 计算剩余时间 */
export function getTimeRemaining(deadline: string | null | undefined): {
  text: string
  isOverdue: boolean
  urgentLevel: 'normal' | 'warning' | 'danger'
} {
  if (!deadline) return { text: '-', isOverdue: false, urgentLevel: 'normal' }
  const now = dayjs()
  const end = dayjs(deadline)
  const diff = end.diff(now, 'hour', true)

  if (diff < 0) {
    return { text: `已超时 ${formatDuration(Math.abs(diff))}`, isOverdue: true, urgentLevel: 'danger' }
  }
  if (diff < 2) {
    return { text: `剩余 ${formatDuration(diff)}`, isOverdue: false, urgentLevel: 'danger' }
  }
  if (diff < 8) {
    return { text: `剩余 ${formatDuration(diff)}`, isOverdue: false, urgentLevel: 'warning' }
  }
  return { text: `剩余 ${formatDuration(diff)}`, isOverdue: false, urgentLevel: 'normal' }
}

/** 生成唯一 ID */
export function generateId(prefix: string = ''): string {
  const timestamp = dayjs().format('YYYYMMDDHHmmss')
  const random = Math.random().toString(36).substring(2, 6).toUpperCase()
  return `${prefix}${timestamp}${random}`
}
