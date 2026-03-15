// ============================================
// 通知 API
// ============================================
import { get, put } from './request'
import type { Notification } from '@/types'

/** 获取通知列表 */
export function getNotificationListApi() {
  return get<Notification[]>('/notification/list')
}

/** 标记单条已读 */
export function markNotificationReadApi(id: string) {
  return put<void>(`/notification/${id}/read`)
}

/** 全部标记已读 */
export function markAllNotificationReadApi() {
  return put<void>('/notification/read-all')
}

/** 获取未读数量 */
export function getUnreadCountApi() {
  return get<number>('/notification/unread-count')
}
