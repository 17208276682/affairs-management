<template>
  <div class="task-create">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h3>新建事务</h3>
        </div>
      </template>

      <!-- 事务表单 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="taskFormRules"
        label-position="top"
        size="large"
      >
        <!-- 事务描述 -->
        <el-form-item label="事务描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述事务内容、要求、预期成果等"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>

        <!-- 事务紧急性 -->
        <el-form-item label="事务紧急性" prop="urgencyType">
          <el-radio-group v-model="form.urgencyType" class="urgency-group">
            <el-radio-button
              v-for="opt in urgencyOptions"
              :key="opt.value"
              :value="opt.value"
              :class="['urgency-btn', `urgency-${opt.value}`]"
            >
              {{ opt.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 事务完成时间 -->
        <el-form-item label="事务完成时间" prop="completionDeadline">
          <div class="deadline-input">
            <div class="deadline-presets">
              <el-button size="small" @click="setPresetDeadline(30, 'minute')">半个小时</el-button>
              <el-button size="small" @click="setPresetDeadline(1, 'hour')">一个小时</el-button>
              <el-button size="small" @click="setPresetDeadline(1, 'day')">一天</el-button>
              <el-button size="small" @click="setPresetDeadline(1, 'week')">一周</el-button>
            </div>
            <el-date-picker
              v-model="form.completionDeadline"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              placeholder="请选择完成时间"
              style="width: 100%"
              :shortcuts="deadlineShortcuts"
            />
          </div>
        </el-form-item>

        <!-- 事务执行人 -->
        <el-form-item label="事务执行人" prop="executorId">
          <div class="executor-selector">
            <div v-if="selectedExecutor" class="selected-executor" @click="showPersonPicker = true">
              <el-avatar :size="32">{{ selectedExecutor.name.charAt(0) }}</el-avatar>
              <div>
                <div class="executor-name">{{ selectedExecutor.name }}</div>
                <div class="executor-dept">{{ selectedExecutor.deptName }} · {{ selectedExecutor.position }}</div>
              </div>
              <el-icon class="change-icon"><Edit /></el-icon>
            </div>
            <el-button v-else @click="showPersonPicker = true">
              <el-icon><Plus /></el-icon>
              选择执行人
            </el-button>
          </div>
        </el-form-item>

        <!-- 附件上传 -->
        <el-form-item label="附件">
          <el-upload
            v-model:file-list="fileList"
            action="#"
            :auto-upload="false"
            :limit="5"
            multiple
          >
            <el-button type="primary" plain>
              <el-icon><UploadFilled /></el-icon>选择文件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">最多上传5个文件，单个文件不超过10MB</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-divider />

        <el-form-item>
          <div class="form-actions">
            <el-button @click="router.back()">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              <el-icon><Promotion /></el-icon>
              下达事务
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 人员选择弹窗 -->
    <el-dialog v-model="showPersonPicker" title="选择执行人" width="480px" destroy-on-close>
      <div class="person-picker">
        <div class="person-list">
          <div
            v-for="member in orgStore.selectableMembers"
            :key="member.id"
            class="person-item"
            :class="{ active: form.executorId === member.id }"
            @click="selectExecutor(member)"
          >
            <el-avatar :size="36">{{ member.name.charAt(0) }}</el-avatar>
            <div class="person-info">
              <div class="person-name">{{ member.name }}</div>
              <div class="person-dept">{{ member.deptName }} · {{ member.position }}</div>
            </div>
            <el-icon v-if="form.executorId === member.id" color="#4F6EF7"><CircleCheck /></el-icon>
          </div>
          <el-empty v-if="orgStore.selectableMembers.length === 0" description="无可选下级人员" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { ElMessage } from 'element-plus'
import type { FormInstance, UploadUserFile } from 'element-plus'
import { useUserStore, useTaskStore, useOrganizationStore } from '@/stores'
import { taskFormRules } from '@/utils/validate'
import type { User, TaskUrgencyType } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const taskStore = useTaskStore()
const orgStore = useOrganizationStore()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const showPersonPicker = ref(false)
const fileList = ref<UploadUserFile[]>([])
const selectedExecutor = ref<User | null>(null)

const form = ref({
  description: '',
  urgencyType: '' as TaskUrgencyType | '',
  completionDeadline: '',
  executorId: '',
})

const urgencyOptions: { value: TaskUrgencyType; label: string }[] = [
  { value: 'important_urgent', label: '重要紧急' },
  { value: 'important_not_urgent', label: '重要不紧急' },
  { value: 'not_important_urgent', label: '不重要紧急' },
  { value: 'not_important_not_urgent', label: '不重要不紧急' },
]

const deadlineShortcuts = [
  { text: '半个小时', value: () => dayjs().add(30, 'minute').toDate() },
  { text: '一个小时', value: () => dayjs().add(1, 'hour').toDate() },
  { text: '一天', value: () => dayjs().add(1, 'day').toDate() },
  { text: '一周', value: () => dayjs().add(1, 'week').toDate() },
]

function setPresetDeadline(value: number, unit: 'minute' | 'hour' | 'day' | 'week') {
  form.value.completionDeadline = dayjs().add(value, unit).format('YYYY-MM-DDTHH:mm:ss')
}

function selectExecutor(member: User) {
  form.value.executorId = member.id
  selectedExecutor.value = member
  showPersonPicker.value = false
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    await taskStore.createTask({
      description: form.value.description,
      urgencyType: form.value.urgencyType as TaskUrgencyType,
      completionDeadline: form.value.completionDeadline,
      executorId: form.value.executorId,
      attachments: [],
    })
    ElMessage.success('事务下达成功！')
    router.push('/task/list/assigned')
  } catch (e) {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  orgStore.fetchSelectableMembers(undefined, userStore.userInfo?.role === 'manager' ? 'subordinates' : undefined)
})
</script>

<style lang="scss" scoped>
.task-create {
  max-width: 720px;
  margin: 0 auto;
}

.form-card {
  .card-header h3 {
    font-size: $font-size-xl;
    font-weight: 600;
    margin: 0;
  }
}

// 紧急性选择
.urgency-group {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.urgency-btn {
  &.is-active.urgency-important_urgent :deep(.el-radio-button__inner) {
    background-color: #EF4444;
    border-color: #EF4444;
    color: white;
  }
  &.is-active.urgency-important_not_urgent :deep(.el-radio-button__inner) {
    background-color: #F59E0B;
    border-color: #F59E0B;
    color: white;
  }
  &.is-active.urgency-not_important_urgent :deep(.el-radio-button__inner) {
    background-color: #3B82F6;
    border-color: #3B82F6;
    color: white;
  }
  &.is-active.urgency-not_important_not_urgent :deep(.el-radio-button__inner) {
    background-color: #22C55E;
    border-color: #22C55E;
    color: white;
  }
}

// 完成时间
.deadline-input {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

.deadline-presets {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

// 执行人
.executor-selector {
  .selected-executor {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-md;
    border: 1px solid $border-light;
    border-radius: $radius-sm;
    cursor: pointer;
    transition: border-color $transition-fast;

    &:hover {
      border-color: $primary;
    }

    .executor-name {
      font-weight: 500;
      font-size: $font-size-sm;
    }

    .executor-dept {
      font-size: $font-size-xs;
      color: $text-secondary;
    }

    .change-icon {
      margin-left: auto;
      color: $text-secondary;
    }
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
  width: 100%;
}

// 人员选择弹窗
.person-picker {
  .person-list {
    max-height: 360px;
    overflow-y: auto;
  }

  .person-item {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-sm $spacing-md;
    border-radius: $radius-sm;
    cursor: pointer;
    transition: background $transition-fast;
    border-bottom: 1px solid $border-lighter;

    &:hover {
      background: $bg-hover;
    }

    &.active {
      background: $primary-light;
    }

    .person-name {
      font-weight: 500;
      font-size: $font-size-sm;
    }

    .person-dept {
      font-size: $font-size-xs;
      color: $text-secondary;
    }
  }
}
</style>
