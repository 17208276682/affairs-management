<template>
  <div class="task-detail" v-loading="taskStore.detailLoading">
    <template v-if="task">
      <!-- 返回 -->
      <div class="page-nav">
        <el-button text @click="router.back()">
          <el-icon><ArrowLeft /></el-icon>返回列表
        </el-button>
      </div>

      <!-- 头部信息 -->
      <el-card class="detail-header-card">
        <div class="detail-header">
          <div class="header-left">
            <div class="level-badge" :style="{ background: TASK_LEVEL_MAP[task.level]?.color }">
              {{ task.level }}
            </div>
            <div>
              <h2 class="detail-title">{{ task.title }}</h2>
              <div class="detail-id">编号：{{ task.id }}</div>
            </div>
          </div>
          <div class="header-right">
            <el-tag
              :color="TASK_STATUS_MAP[task.status]?.bgColor"
              :style="{ color: TASK_STATUS_MAP[task.status]?.color, border: 'none' }"
              size="large"
              round
            >
              {{ TASK_STATUS_MAP[task.status]?.label }}
            </el-tag>
          </div>
        </div>
      </el-card>

      <div class="detail-body">
        <!-- 左侧主内容 -->
        <div class="detail-main">
          <!-- 事务描述 -->
          <el-card class="section-card">
            <template #header><span class="section-title">事务描述</span></template>
            <p class="description-text">{{ task.description }}</p>
          </el-card>

          <!-- 附件 -->
          <el-card v-if="task.attachments.length > 0" class="section-card">
            <template #header><span class="section-title">附件 ({{ task.attachments.length }})</span></template>
            <div class="attachment-list">
              <div v-for="file in task.attachments" :key="file.id" class="attachment-item">
                <el-icon :size="20" color="#4F6EF7"><Document /></el-icon>
                <div class="file-info">
                  <span class="file-name">{{ file.name }}</span>
                  <span class="file-size">{{ formatFileSize(file.size) }}</span>
                </div>
                <el-button link type="primary" size="small">下载</el-button>
              </div>
            </div>
          </el-card>

          <!-- 处理时间线 -->
          <el-card class="section-card">
            <template #header><span class="section-title">处理记录</span></template>
            <el-timeline v-if="taskStore.currentRecords.length > 0">
              <el-timeline-item
                v-for="record in taskStore.currentRecords"
                :key="record.id"
                :color="PROCESS_ACTION_MAP[record.action]?.color"
                :timestamp="formatDateTime(record.createdAt)"
                placement="top"
              >
                <div class="record-header">
                  <el-avatar :size="24">{{ record.operatorName.charAt(0) }}</el-avatar>
                  <span class="record-operator">{{ record.operatorName }}</span>
                  <el-tag size="small" type="info" effect="plain">{{ PROCESS_ACTION_MAP[record.action]?.label }}</el-tag>
                </div>
                <p class="record-content">{{ record.content }}</p>
                <div v-if="record.attachments.length > 0" class="record-attachments">
                  <div v-for="f in record.attachments" :key="f.id" class="record-file">
                    <el-icon><Paperclip /></el-icon>
                    <span>{{ f.name }}</span>
                  </div>
                </div>
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无处理记录" :image-size="60" />
          </el-card>
        </div>

        <!-- 右侧信息栏 -->
        <div class="detail-sidebar">
          <!-- 人员信息 -->
          <el-card class="info-card">
            <template #header><span class="section-title">下达人</span></template>
            <div class="person-info">
              <el-avatar :size="40">{{ task.assignerName.charAt(0) }}</el-avatar>
              <div>
                <div class="person-name">{{ task.assignerName }}</div>
                <div class="person-meta">{{ task.assignerDept }} · {{ task.assignerPosition }}</div>
              </div>
            </div>
          </el-card>

          <el-card class="info-card">
            <template #header><span class="section-title">执行人</span></template>
            <div class="person-info">
              <el-avatar :size="40">{{ task.executorName.charAt(0) }}</el-avatar>
              <div>
                <div class="person-name">{{ task.executorName }}</div>
                <div class="person-meta">{{ task.executorDept }}</div>
              </div>
            </div>
          </el-card>

          <!-- 时间信息 -->
          <el-card class="info-card">
            <template #header><span class="section-title">时间信息</span></template>
            <div class="time-list">
              <div class="time-item">
                <span class="time-label">创建时间</span>
                <span class="time-value">{{ formatDateTime(task.createdAt) }}</span>
              </div>
              <div class="time-item">
                <span class="time-label">响应截止</span>
                <span class="time-value">{{ formatDateTime(task.responseDeadline) }}</span>
              </div>
              <div class="time-item">
                <span class="time-label">实际响应</span>
                <span class="time-value">{{ formatDateTime(task.responseTime) }}</span>
              </div>
              <div class="time-item">
                <span class="time-label">完成截止</span>
                <span class="time-value" :class="{ 'text-danger': getTimeRemaining(task.completionDeadline).isOverdue }">
                  {{ formatDateTime(task.completionDeadline) }}
                </span>
              </div>
              <div class="time-item">
                <span class="time-label">实际完成</span>
                <span class="time-value">{{ formatDateTime(task.completionTime) }}</span>
              </div>
              <div class="time-item highlight">
                <span class="time-label">剩余时间</span>
                <span class="time-value" :class="`level-${getTimeRemaining(task.completionDeadline).urgentLevel}`">
                  {{ getTimeRemaining(task.completionDeadline).text }}
                </span>
              </div>
            </div>
          </el-card>

          <!-- 级别信息 -->
          <el-card class="info-card">
            <template #header><span class="section-title">事务级别</span></template>
            <div class="level-info-detail">
              <div class="level-badge-lg" :style="{ background: TASK_LEVEL_MAP[task.level]?.color }">{{ task.level }}</div>
              <div>
                <div class="level-label">{{ TASK_LEVEL_MAP[task.level]?.label }}</div>
                <div class="level-desc">{{ TASK_LEVEL_MAP[task.level]?.description }}</div>
              </div>
            </div>
          </el-card>

          <!-- 操作按钮 -->
          <el-card v-if="showActions" class="info-card">
            <template #header><span class="section-title">操作</span></template>
            <div class="action-buttons">
              <el-button
                v-if="canExecute"
                type="primary"
                style="width: 100%"
                @click="router.push(`/task/execute/${task.id}`)"
              >
                <el-icon><VideoPlay /></el-icon>
                {{ task.status === 'pending' ? '接收并执行' : '继续执行' }}
              </el-button>
              <el-button
                v-if="canFeedback"
                type="warning"
                style="width: 100%"
                @click="router.push(`/task/feedback/${task.id}`)"
              >
                <el-icon><ChatDotRound /></el-icon>审核反馈
              </el-button>
            </div>
          </el-card>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore, useTaskStore } from '@/stores'
import { TASK_STATUS_MAP, TASK_LEVEL_MAP, PROCESS_ACTION_MAP } from '@/utils/constants'
import { formatDateTime, formatFileSize, getTimeRemaining } from '@/utils/format'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const taskStore = useTaskStore()

const task = computed(() => taskStore.currentTask)

const canExecute = computed(() => {
  if (!task.value || !userStore.userInfo) return false
  return task.value.executorId === userStore.userInfo.id &&
    ['pending', 'accepted', 'in_progress', 'rejected'].includes(task.value.status)
})

const canFeedback = computed(() => {
  if (!task.value || !userStore.userInfo) return false
  return task.value.assignerId === userStore.userInfo.id && task.value.status === 'submitted'
})

const showActions = computed(() => canExecute.value || canFeedback.value)

onMounted(() => {
  const id = route.params.id as string
  taskStore.fetchTaskDetail(id)
})
</script>

<style lang="scss" scoped>
.task-detail {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  overflow-y: auto;
}

.page-nav {
  margin-bottom: $spacing-md;
}

.detail-header-card {
  margin-bottom: $spacing-md;

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-left {
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
  }

  .detail-title {
    font-size: $font-size-xl;
    font-weight: 700;
    margin: 0 0 4px;
  }

  .detail-id {
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

.detail-body {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: $spacing-md;
}

.section-card {
  margin-bottom: $spacing-md;
  .section-title {
    font-weight: 600;
    font-size: $font-size-md;
  }
}

.description-text {
  font-size: $font-size-base;
  color: $text-regular;
  line-height: 1.8;
  white-space: pre-wrap;
  margin: 0;
}

.attachment-list {
  .attachment-item {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm 0;
    border-bottom: 1px solid $border-lighter;

    &:last-child { border-bottom: none; }

    .file-name {
      font-size: $font-size-sm;
      color: $text-primary;
    }

    .file-size {
      font-size: $font-size-xs;
      color: $text-secondary;
      margin-left: auto;
    }
  }
}

.record-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  .record-operator {
    font-weight: 500;
    font-size: $font-size-sm;
  }
}

.record-content {
  font-size: $font-size-sm;
  color: $text-regular;
  margin: 8px 0 4px;
  line-height: 1.6;
}

.record-attachments {
  .record-file {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: $font-size-xs;
    color: $primary;
    cursor: pointer;
  }
}

// 右侧信息栏
.info-card {
  margin-bottom: $spacing-md;
  .section-title {
    font-weight: 600;
    font-size: $font-size-sm;
  }
}

.person-info {
  display: flex;
  align-items: center;
  gap: $spacing-sm;

  .person-name {
    font-weight: 500;
    font-size: $font-size-sm;
  }

  .person-meta {
    font-size: $font-size-xs;
    color: $text-secondary;
  }
}

.time-list {
  .time-item {
    display: flex;
    justify-content: space-between;
    padding: 6px 0;
    border-bottom: 1px solid $border-lighter;

    &:last-child { border-bottom: none; }

    &.highlight {
      background: $bg-page;
      margin: 0 -16px;
      padding: 8px 16px;
      border-radius: $radius-sm;
    }

    .time-label {
      font-size: $font-size-xs;
      color: $text-secondary;
    }

    .time-value {
      font-size: $font-size-xs;
      color: $text-primary;
      font-weight: 500;
    }

    .text-danger { color: $danger; }
    .level-danger { color: $danger; font-weight: 600; }
    .level-warning { color: $warning; font-weight: 600; }
    .level-normal { color: $text-regular; }
  }
}

.level-info-detail {
  display: flex;
  align-items: center;
  gap: $spacing-sm;

  .level-badge-lg {
    width: 36px;
    height: 36px;
    border-radius: $radius-sm;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-weight: 700;
    font-size: $font-size-lg;
  }

  .level-label {
    font-weight: 600;
    font-size: $font-size-sm;
  }

  .level-desc {
    font-size: $font-size-xs;
    color: $text-secondary;
  }
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}
</style>
