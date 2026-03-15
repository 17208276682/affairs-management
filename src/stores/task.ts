// ============================================
// 事务状态管理
// ============================================
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Task, TaskProcessRecord, TaskListParams, CreateTaskRequest } from '@/types'
import {
  createTaskApi, getTaskListApi, getTaskDetailApi,
  acceptTaskApi, addProcessRecordApi, submitTaskApi,
  approveTaskApi, rejectTaskApi, reassignTaskApi, cancelTaskApi,
} from '@/api/task'

export const useTaskStore = defineStore('task', () => {
  const taskList = ref<Task[]>([])
  const listTotal = ref(0)
  const currentTask = ref<Task | null>(null)
  const currentRecords = ref<TaskProcessRecord[]>([])
  const listLoading = ref(false)
  const detailLoading = ref(false)

  const listParams = ref<TaskListParams>({
    type: 'received',
    status: '',
    keyword: '',
    page: 1,
    pageSize: 20,
  })

  // Getters
  const pendingCount = computed(() => taskList.value.filter(t => t.status === 'pending').length)
  const overdueCount = computed(() => taskList.value.filter(t => t.status === 'overdue').length)

  // Actions
  async function fetchTaskList(params?: Partial<TaskListParams>) {
    if (params) Object.assign(listParams.value, params)
    listLoading.value = true
    try {
      const res = await getTaskListApi(listParams.value)
      taskList.value = res.data.list
      listTotal.value = res.data.total
    } finally {
      listLoading.value = false
    }
  }

  async function fetchTaskDetail(id: string) {
    detailLoading.value = true
    try {
      const res = await getTaskDetailApi(id)
      currentTask.value = res.data.task
      currentRecords.value = res.data.processRecords
    } finally {
      detailLoading.value = false
    }
  }

  async function createTask(data: CreateTaskRequest) {
    const res = await createTaskApi(data)
    return res.data
  }

  async function acceptTask(id: string) {
    const res = await acceptTaskApi(id)
    if (currentTask.value?.id === id) {
      currentTask.value = res.data
    }
    return res.data
  }

  async function addProcessRecord(id: string, data: { content: string; attachments?: any[] }) {
    const res = await addProcessRecordApi(id, data)
    currentRecords.value.push(res.data)
    return res.data
  }

  async function submitResult(id: string, data: { content: string; attachments?: any[] }) {
    const res = await submitTaskApi(id, data)
    if (currentTask.value?.id === id) {
      currentTask.value = res.data
    }
    return res.data
  }

  async function approveTask(id: string, comment: string) {
    const res = await approveTaskApi(id, { comment })
    if (currentTask.value?.id === id) {
      currentTask.value = res.data
    }
    return res.data
  }

  async function rejectTask(id: string, comment: string, reason: string) {
    const res = await rejectTaskApi(id, { comment, reason })
    if (currentTask.value?.id === id) {
      currentTask.value = res.data
    }
    return res.data
  }

  async function reassignTask(id: string, executorId: string) {
    const res = await reassignTaskApi(id, { executorId })
    return res.data
  }

  async function cancelTask(id: string, reason: string) {
    const res = await cancelTaskApi(id, { reason })
    if (currentTask.value?.id === id) {
      currentTask.value = res.data
    }
    return res.data
  }

  return {
    taskList, listTotal, currentTask, currentRecords,
    listLoading, detailLoading, listParams,
    pendingCount, overdueCount,
    fetchTaskList, fetchTaskDetail, createTask,
    acceptTask, addProcessRecord, submitResult,
    approveTask, rejectTask, reassignTask, cancelTask,
  }
})
