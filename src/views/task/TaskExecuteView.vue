<template>
  <div class="task-execute" v-loading="taskStore.detailLoading">
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
              <span>下达人：{{ task.assignerName }}</span>
              <el-divider direction="vertical" />
              <span>{{ TASK_LEVEL_MAP[task.level]?.label }}</span>
              <el-divider direction="vertical" />
              <span :class="{ 'text-danger': getTimeRemaining(task.completionDeadline).isOverdue }">
                截止：{{ formatDateTime(task.completionDeadline) }}
              </span>
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
        <el-divider />
        <p class="summary-desc">{{ task.description }}</p>
      </el-card>

      <!-- 操作区域 -->
      <el-row :gutter="20">
        <!-- 左侧：操作面板 -->
        <el-col :span="16">
          <!-- 接收事务 -->
          <el-card v-if="task.status === 'pending'" class="action-card">
            <template #header><span class="section-title">接收事务</span></template>
            <div class="accept-section">
              <el-alert title="请确认接收此事务，接收后将开始计时" type="info" :closable="false" show-icon />
              <div class="accept-actions">
                <el-button type="primary" size="large" :loading="acceptLoading" @click="handleAccept">
                  <el-icon><CircleCheck /></el-icon>确认接收
                </el-button>
              </div>
            </div>
          </el-card>

          <!-- 添加处理记录 -->
          <el-card v-if="['accepted', 'in_progress', 'rejected'].includes(task.status)" class="action-card">
            <template #header><span class="section-title">添加处理记录</span></template>
            <el-form :model="recordForm" label-position="top">
              <el-form-item label="处理内容">
                <el-input
                  v-model="recordForm.content"
                  type="textarea"
                  :rows="4"
                  placeholder="描述您的处理过程、进展、遇到的问题等"
                />
              </el-form-item>
              <el-form-item label="附件">
                <el-upload
                  v-model:file-list="recordFileList"
                  action="#"
                  :auto-upload="false"
                  :limit="3"
                  multiple
                >
                  <el-button type="primary" plain>
                    <el-icon><Upload /></el-icon>上传附件
                  </el-button>
                </el-upload>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="recordLoading" :disabled="!recordForm.content" @click="handleAddRecord">
                  <el-icon><EditPen /></el-icon>提交记录
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 提交成果 -->
          <el-card v-if="['accepted', 'in_progress', 'rejected'].includes(task.status)" class="action-card submit-card">
            <template #header>
              <span class="section-title">
                <el-icon><Promotion /></el-icon>提交成果
              </span>
            </template>
            <el-form :model="submitForm" label-position="top">
              <el-form-item label="完成说明">
                <el-input
                  v-model="submitForm.content"
                  type="textarea"
                  :rows="4"
                  placeholder="请描述事务完成情况、成果说明等"
                />
              </el-form-item>
              <el-form-item label="成果附件">
                <el-upload
                  v-model:file-list="submitFileList"
                  action="#"
                  :auto-upload="false"
                  :limit="5"
                  multiple
                  drag
                >
                  <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
                  <div class="el-upload__text">拖拽或<em>点击上传</em>成果文件</div>
                </el-upload>
              </el-form-item>
              <el-form-item>
                <el-button type="success" size="large" :loading="submitLoading" :disabled="!submitForm.content" @click="handleSubmit">
                  <el-icon><Promotion /></el-icon>提交审核
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>

        <!-- 右侧：时间线 -->
        <el-col :span="8">
          <el-card class="timeline-card">
            <template #header><span class="section-title">处理记录</span></template>
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
                  <el-tag size="small" :type="getActionTagType(record.action)">
                    {{ PROCESS_ACTION_MAP[record.action]?.label }}
                  </el-tag>
                </div>
                <p class="tl-content">{{ record.content }}</p>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无记录" :image-size="60" />
          </el-card>

          <!-- 倒计时 -->
          <el-card class="countdown-card">
            <template #header><span class="section-title">截止倒计时</span></template>
            <div class="countdown" :class="`level-${getTimeRemaining(task.completionDeadline).urgentLevel}`">
              <div class="countdown-value">{{ getTimeRemaining(task.completionDeadline).text }}</div>
              <div class="countdown-label">{{ formatDateTime(task.completionDeadline) }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { UploadUserFile } from 'element-plus'
import { useTaskStore } from '@/stores'
import { TASK_STATUS_MAP, TASK_LEVEL_MAP, PROCESS_ACTION_MAP } from '@/utils/constants'
import { formatDateTime, getTimeRemaining } from '@/utils/format'
import type { ProcessAction } from '@/types'

const router = useRouter()
const route = useRoute()
const taskStore = useTaskStore()

const task = computed(() => taskStore.currentTask)
const acceptLoading = ref(false)
const recordLoading = ref(false)
const submitLoading = ref(false)
const recordFileList = ref<UploadUserFile[]>([])
const submitFileList = ref<UploadUserFile[]>([])

const recordForm = ref({ content: '' })
const submitForm = ref({ content: '' })

function getActionTagType(action: ProcessAction) {
  const map: Record<string, string> = {
    create: 'primary', accept: 'success', process: '', submit: 'warning',
    approve: 'success', reject: 'danger', reassign: 'info', cancel: 'info',
  }
  return (map[action] || '') as any
}

async function handleAccept() {
  acceptLoading.value = true
  try {
    await taskStore.acceptTask(task.value!.id)
    ElMessage.success('已接收事务，开始执行！')
  } finally {
    acceptLoading.value = false
  }
}

async function handleAddRecord() {
  if (!recordForm.value.content) return
  recordLoading.value = true
  try {
    await taskStore.addProcessRecord(task.value!.id, { content: recordForm.value.content })
    ElMessage.success('处理记录已添加')
    recordForm.value.content = ''
    recordFileList.value = []
  } finally {
    recordLoading.value = false
  }
}

async function handleSubmit() {
  if (!submitForm.value.content) return
  await ElMessageBox.confirm('提交后将进入审核环节，是否确认提交？', '确认提交', {
    confirmButtonText: '确认提交',
    cancelButtonText: '继续编辑',
    type: 'warning',
  })
  submitLoading.value = true
  try {
    await taskStore.submitResult(task.value!.id, { content: submitForm.value.content })
    ElMessage.success('成果已提交，等待审核')
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  const id = route.params.id as string
  taskStore.fetchTaskDetail(id)
})
</script>

<style lang="scss" scoped>
.task-execute {
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
    h3 {
      margin: 0 0 6px;
      font-size: $font-size-lg;
      font-weight: 600;
    }
  }

  .summary-meta {
    font-size: $font-size-sm;
    color: $text-secondary;
    .text-danger { color: $danger; font-weight: 500; }
  }

  .summary-desc {
    font-size: $font-size-base;
    color: $text-regular;
    line-height: 1.8;
    margin: 0;
  }
}

.action-card {
  margin-bottom: $spacing-md;
  .section-title {
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.accept-section {
  .accept-actions {
    margin-top: $spacing-md;
    text-align: center;
  }
}

.submit-card {
  border: 1px solid lighten($success, 30%);
}

.timeline-card {
  margin-bottom: $spacing-md;
  .section-title {
    font-weight: 600;
  }

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

.countdown-card {
  .section-title {
    font-weight: 600;
  }

  .countdown {
    text-align: center;
    padding: $spacing-md;

    .countdown-value {
      font-size: $font-size-2xl;
      font-weight: 700;
      margin-bottom: 8px;
    }

    .countdown-label {
      font-size: $font-size-xs;
      color: $text-secondary;
    }

    &.level-danger .countdown-value { color: $danger; }
    &.level-warning .countdown-value { color: $warning; }
    &.level-normal .countdown-value { color: $text-primary; }
  }
}
</style>
