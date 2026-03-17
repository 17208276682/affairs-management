<template>
  <div class="task-feedback" v-loading="taskStore.detailLoading">
    <template v-if="task">
      <!-- 返回 -->
      <div class="page-nav">
        <el-button text @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>返回详情
        </el-button>
      </div>

      <!-- 任务摘要 -->
      <el-card class="summary-card">
        <div class="summary-header">
          <div class="level-badge" :style="{ background: TASK_LEVEL_MAP[task.level]?.color }">
            {{ task.level }}
          </div>
          <div class="summary-info">
            <h3>{{ task.title }}</h3>
            <div class="summary-meta">
              <span>执行人：{{ task.executorName }}</span>
              <el-divider direction="vertical" />
              <span>{{ TASK_LEVEL_MAP[task.level]?.label }}</span>
            </div>
          </div>
          <el-tag
            :color="TASK_STATUS_MAP[task.status]?.bgColor"
            :style="{ color: TASK_STATUS_MAP[task.status]?.color, border: 'none' }"
            size="large"
            round
          >
            {{ TASK_STATUS_MAP[task.status]?.label }}
          </el-tag>
        </div>
      </el-card>

      <el-row :gutter="20">
        <!-- 左侧：提交内容 & 操作 -->
        <el-col :span="16">
          <!-- 提交成果 -->
          <el-card class="section-card">
            <template #header><span class="section-title">提交的成果</span></template>
            <div v-if="submitRecord">
              <p class="submit-content">{{ submitRecord.content }}</p>
              <div v-if="submitRecord.attachments.length > 0" class="submit-files">
                <div v-for="f in submitRecord.attachments" :key="f.id" class="file-item">
                  <el-icon color="#4F6EF7"><Document /></el-icon>
                  <span class="file-name">{{ f.name }}</span>
                  <span class="file-size">{{ formatFileSize(f.size) }}</span>
                  <el-button link type="primary" size="small" @click="openPreview(f.id, f.name, f.type)">预览</el-button>
                  <el-button link type="primary" size="small" @click="openDownload(f.id)">下载</el-button>
                </div>
              </div>
              <div class="submit-time">
                提交时间：{{ formatDateTime(submitRecord.createdAt) }}
              </div>
            </div>
            <el-empty v-else description="暂无提交记录" :image-size="60" />
          </el-card>

          <!-- 审核操作 -->
          <el-card v-if="task.status === 'submitted'" class="section-card feedback-card">
            <template #header><span class="section-title">审核操作</span></template>

            <el-tabs v-model="activeAction" type="card">
              <el-tab-pane label="通过" name="approve">
                <div class="feedback-form">
                  <el-alert title="通过后事务将标记为已完成" type="success" :closable="false" show-icon />
                  <el-form label-position="top" style="margin-top: 16px">
                    <el-form-item label="审核意见">
                      <el-input
                        v-model="approveForm.content"
                        type="textarea"
                        :rows="3"
                        placeholder="可选：填写审核通过的意见"
                      />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="success" size="large" :loading="approving" @click="handleApprove">
                        <el-icon><CircleCheck /></el-icon>审核通过
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </el-tab-pane>

              <el-tab-pane label="驳回" name="reject">
                <div class="feedback-form">
                  <el-alert title="驳回后执行人需重新处理并提交" type="warning" :closable="false" show-icon />
                  <el-form label-position="top" style="margin-top: 16px">
                    <el-form-item label="驳回理由" required>
                      <el-input
                        v-model="rejectForm.reason"
                        type="textarea"
                        :rows="3"
                        placeholder="请说明驳回原因和修改要求"
                      />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="danger" size="large" :loading="rejecting" :disabled="!rejectForm.reason" @click="handleReject">
                        <el-icon><CircleClose /></el-icon>驳回
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </el-tab-pane>

              <el-tab-pane label="转派" name="reassign">
                <div class="feedback-form">
                  <el-alert title="转派给其他执行人继续处理" type="info" :closable="false" show-icon />
                  <el-form label-position="top" style="margin-top: 16px">
                    <el-form-item label="新执行人" required>
                      <div class="reassign-selector">
                        <div v-if="reassignForm.executor" class="selected-user">
                          <el-avatar :size="32">{{ reassignForm.executor.name.charAt(0) }}</el-avatar>
                          <span>{{ reassignForm.executor.name }} - {{ reassignForm.executor.deptName }}</span>
                          <el-button link @click="showPicker = true">更换</el-button>
                        </div>
                        <el-button v-else @click="showPicker = true">
                          <el-icon><Plus /></el-icon>选择执行人
                        </el-button>
                      </div>
                    </el-form-item>
                    <el-form-item label="转派说明">
                      <el-input
                        v-model="reassignForm.reason"
                        type="textarea"
                        :rows="3"
                        placeholder="可选：说明转派原因"
                      />
                    </el-form-item>
                    <el-form-item>
                      <el-button
                        type="warning"
                        size="large"
                        :loading="reassigning"
                        :disabled="!reassignForm.executor"
                        @click="handleReassign"
                      >
                        <el-icon><Switch /></el-icon>确认转派
                      </el-button>
                    </el-form-item>
                  </el-form>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>

          <!-- 已完成 -->
          <el-card v-if="task.status === 'completed'" class="section-card">
            <el-result icon="success" title="事务已完成" sub-title="审核已通过，事务圆满结束">
              <template #extra>
                <el-button @click="router.push('/task/list/assigned')">返回列表</el-button>
              </template>
            </el-result>
          </el-card>
        </el-col>

        <!-- 右侧：时间线 -->
        <el-col :span="8">
          <el-card class="timeline-card">
            <template #header><span class="section-title">完整记录</span></template>
            <el-timeline v-if="taskStore.currentRecords.length > 0">
              <el-timeline-item
                v-for="record in taskStore.currentRecords"
                :key="record.id"
                :color="PROCESS_ACTION_MAP[record.action]?.color"
                :timestamp="formatDateTime(record.createdAt)"
                size="normal"
              >
                <div class="tl-header">
                  <span class="tl-name">{{ record.operatorName }}</span>
                  <el-tag size="small" :type="getActionType(record.action)">
                    {{ PROCESS_ACTION_MAP[record.action]?.label }}
                  </el-tag>
                </div>
                <p class="tl-content">{{ record.content }}</p>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无记录" :image-size="60" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 人员选择弹窗 -->
      <el-dialog v-model="showPicker" title="选择新执行人" width="560px" destroy-on-close>
        <el-input v-model="pickerSearch" placeholder="搜索姓名" prefix-icon="Search" clearable style="margin-bottom: 12px" />
        <div class="picker-list">
          <div
            v-for="m in filteredMembers"
            :key="m.id"
            class="picker-item"
            :class="{ active: reassignForm.executor?.id === m.id }"
            @click="selectReassign(m)"
          >
            <el-avatar :size="32">{{ m.name.charAt(0) }}</el-avatar>
            <div>
              <div class="picker-name">{{ m.name }}</div>
              <div class="picker-dept">{{ m.deptName }} · {{ m.position }}</div>
            </div>
            <el-icon v-if="reassignForm.executor?.id === m.id" color="#4F6EF7"><CircleCheck /></el-icon>
          </div>
        </div>
      </el-dialog>

      <AttachmentPreviewDialog
        :visible="previewVisible"
        :url="previewUrl"
        :title="previewTitle"
        :mime-type="previewMimeType"
        @update:visible="handlePreviewVisibleChange"
        @download="downloadCurrentPreview"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import AttachmentPreviewDialog from '@/components/AttachmentPreviewDialog.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTaskStore, useOrganizationStore } from '@/stores'
import { buildPreviewUrl, buildDownloadUrl } from '@/api/file'
import { TASK_STATUS_MAP, TASK_LEVEL_MAP, PROCESS_ACTION_MAP } from '@/utils/constants'
import { formatDateTime, formatFileSize } from '@/utils/format'
import type { User, ProcessAction } from '@/types'

const router = useRouter()
const route = useRoute()
const taskStore = useTaskStore()
const orgStore = useOrganizationStore()

const task = computed(() => taskStore.currentTask)
const activeAction = ref('approve')
const showPicker = ref(false)
const pickerSearch = ref('')
const approving = ref(false)
const rejecting = ref(false)
const reassigning = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('')
const previewMimeType = ref('')
const previewAttachmentId = ref('')

const approveForm = ref({ content: '' })
const rejectForm = ref({ reason: '' })
const reassignForm = ref<{ executor: User | null; reason: string }>({ executor: null, reason: '' })

const submitRecord = computed(() =>
  taskStore.currentRecords.find(r => r.action === 'submit')
)

const filteredMembers = computed(() => {
  const kw = pickerSearch.value.toLowerCase()
  return orgStore.selectableMembers.filter(m =>
    (!kw || m.name.toLowerCase().includes(kw)) && m.id !== task.value?.executorId
  )
})

function getActionType(action: ProcessAction) {
  const map: Record<string, string> = {
    create: 'primary', accept: 'success', process: '', submit: 'warning',
    approve: 'success', reject: 'danger', reassign: 'info', cancel: 'info',
  }
  return (map[action] || '') as any
}

function selectReassign(m: User) {
  reassignForm.value.executor = m
  showPicker.value = false
}

function openPreview(fileId: string, name?: string, mimeType?: string) {
  previewAttachmentId.value = fileId
  previewUrl.value = buildPreviewUrl(fileId)
  previewTitle.value = name || '附件预览'
  previewMimeType.value = mimeType || ''
  previewVisible.value = true
}

function openDownload(fileId: string) {
  window.open(buildDownloadUrl(fileId), '_blank')
}

function downloadCurrentPreview() {
  if (previewAttachmentId.value) {
    window.open(buildDownloadUrl(previewAttachmentId.value), '_blank')
  }
}

function handlePreviewVisibleChange(visible: boolean) {
  previewVisible.value = visible
}

async function handleApprove() {
  await ElMessageBox.confirm('确认通过审核？事务将标记为已完成。', '审核通过', {
    confirmButtonText: '确认通过',
    type: 'success',
  })
  approving.value = true
  try {
    await taskStore.approveTask(task.value!.id, approveForm.value.content || '审核通过')
    ElMessage.success('已通过审核')
  } finally {
    approving.value = false
  }
}

async function handleReject() {
  if (!rejectForm.value.reason) return
  rejecting.value = true
  try {
    await taskStore.rejectTask(task.value!.id, '驳回', rejectForm.value.reason)
    ElMessage.success('已驳回，执行人将收到通知')
  } finally {
    rejecting.value = false
  }
}

async function handleReassign() {
  if (!reassignForm.value.executor) return
  reassigning.value = true
  try {
    await taskStore.reassignTask(task.value!.id, { executorId: reassignForm.value.executor.id, reason: reassignForm.value.reason } as any)
    ElMessage.success('已转派给 ' + reassignForm.value.executor.name)
  } finally {
    reassigning.value = false
  }
}

onMounted(() => {
  const id = route.params.id as string
  taskStore.fetchTaskDetail(id)
  orgStore.fetchSelectableMembers()
})
</script>

<style lang="scss" scoped>
.task-feedback {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  overflow-y: auto;
}

.page-nav {
  margin-bottom: $spacing-md;
}

.summary-card {
  margin-bottom: $spacing-md;

  .summary-header {
    display: flex;
    align-items: center;
    gap: $spacing-md;
  }

  .level-badge {
    width: 48px;
    height: 48px;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: $font-size-2xl;
    font-weight: 700;
    flex-shrink: 0;
  }

  .summary-info {
    flex: 1;
    h3 { margin: 0 0 6px; font-weight: 600; }
  }

  .summary-meta {
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

.section-card {
  margin-bottom: $spacing-md;
  .section-title {
    font-weight: 600;
    font-size: $font-size-md;
  }
}

.submit-content {
  font-size: $font-size-base;
  color: $text-regular;
  line-height: 1.8;
  margin: 0 0 $spacing-md;
  white-space: pre-wrap;
}

.submit-files {
  margin-bottom: $spacing-md;

  .file-item {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: 8px 0;
    border-bottom: 1px solid $border-lighter;

    &:last-child { border-bottom: none; }

    .file-name {
      font-size: $font-size-sm;
      color: $text-primary;
      flex: 1;
    }

    .file-size {
      font-size: $font-size-xs;
      color: $text-secondary;
    }
  }
}

.submit-time {
  font-size: $font-size-xs;
  color: $text-secondary;
}

.feedback-card {
  border: 1px solid $border-light;
}

.feedback-form {
  padding: $spacing-sm 0;
}

.reassign-selector {
  .selected-user {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-md;
    border: 1px solid $border-light;
    border-radius: $radius-sm;
  }
}

// 时间线
.timeline-card {
  .section-title { font-weight: 600; }

  .tl-header {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    .tl-name {
      font-weight: 500;
      font-size: $font-size-sm;
    }
  }

  .tl-content {
    font-size: $font-size-sm;
    color: $text-regular;
    margin: 6px 0 0;
    line-height: 1.6;
  }
}

// 人员选择
.picker-list {
  max-height: 360px;
  overflow-y: auto;

  .picker-item {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-md;
    cursor: pointer;
    border-radius: $radius-sm;
    border-bottom: 1px solid $border-lighter;

    &:hover { background: $bg-hover; }
    &.active { background: $primary-light; }

    .picker-name {
      font-weight: 500;
      font-size: $font-size-sm;
    }

    .picker-dept {
      font-size: $font-size-xs;
      color: $text-secondary;
    }
  }
}
</style>
