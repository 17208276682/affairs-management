// ============================================
// 通知状态管理
// ============================================
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Notification } from '@/types'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([
    {
      id: 'N001', userId: 'U002', title: '新事务指派',
      content: '陈志远向您下达了事务"Q1季度产品规划方案制定"',
      type: 'task_assigned', relatedTaskId: 'T20260201001', isRead: true,
      createdAt: '2026-02-01T08:00:00Z',
    },
    {
      id: 'N002', userId: 'U008', title: '新事务指派',
      content: '李雪梅向您下达了事务"品牌推广活动策划书"',
      type: 'task_assigned', relatedTaskId: 'T20260210001', isRead: false,
      createdAt: '2026-02-10T09:00:00Z',
    },
    {
      id: 'N003', userId: 'U001', title: '事务结果已提交',
      content: '刘明提交了"新员工入职培训方案"的处理结果，请审核',
      type: 'task_submitted', relatedTaskId: 'T20260218001', isRead: false,
      createdAt: '2026-02-24T10:00:00Z',
    },
    {
      id: 'N004', userId: 'U007', title: '事务结果已提交',
      content: '吴倩提交了"数据库性能优化方案"的处理结果，请审核',
      type: 'task_submitted', relatedTaskId: 'T20260226001', isRead: false,
      createdAt: '2026-02-27T15:00:00Z',
    },
  ])

  const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)

  function markAsRead(id: string) {
    const n = notifications.value.find(n => n.id === id)
    if (n) n.isRead = true
  }

  function markAllAsRead() {
    notifications.value.forEach(n => { n.isRead = true })
  }

  function addNotification(notification: Notification) {
    notifications.value.unshift(notification)
  }

  return { notifications, unreadCount, markAsRead, markAllAsRead, addNotification }
})
