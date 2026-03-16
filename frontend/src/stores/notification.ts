// ============================================
// 通知状态管理
// ============================================
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Notification } from '@/types'
import {
  getNotificationListApi,
  markNotificationReadApi,
  markAllNotificationReadApi,
  getUnreadCountApi,
} from '@/api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([])
  const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)

  async function fetchNotifications() {
    try {
      const res = await getNotificationListApi()
      notifications.value = res.data
    } catch {
      // 静默失败，保持当前列表
    }
  }

  async function markAsRead(id: string) {
    try {
      await markNotificationReadApi(id)
      const n = notifications.value.find(n => n.id === id)
      if (n) n.isRead = true
    } catch {
      // 静默失败
    }
  }

  async function markAllAsRead() {
    try {
      await markAllNotificationReadApi()
      notifications.value.forEach(n => { n.isRead = true })
    } catch {
      // 静默失败
    }
  }

  function addNotification(notification: Notification) {
    notifications.value.unshift(notification)
  }

  return { notifications, unreadCount, fetchNotifications, markAsRead, markAllAsRead, addNotification }
})
