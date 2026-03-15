<template>
  <div class="profile-view">
    <div class="page-header">
      <h3>个人中心</h3>
    </div>

    <el-row :gutter="20" class="content-row">
      <!-- 左侧个人信息 -->
      <el-col :span="8" class="profile-col">
        <el-card class="profile-card">
          <div class="avatar-section">
            <el-avatar :size="80">{{ userStore.userInfo?.name?.charAt(0) }}</el-avatar>
            <h3 class="user-name">{{ userStore.userInfo?.name }}</h3>
            <el-tag :type="userStore.userInfo?.role === 'admin' ? 'danger' : userStore.userInfo?.role === 'manager' ? 'warning' : 'info'" effect="light">
              {{ userStore.displayRole }}
            </el-tag>
          </div>
          <el-divider />
          <div class="info-list">
            <div class="info-item">
              <el-icon><OfficeBuilding /></el-icon>
              <span class="info-label">部门</span>
              <span class="info-value">{{ userStore.userInfo?.deptName }}</span>
            </div>
            <div class="info-item">
              <el-icon><Briefcase /></el-icon>
              <span class="info-label">职位</span>
              <span class="info-value">{{ userStore.userInfo?.position }}</span>
            </div>
            <div class="info-item">
              <el-icon><Phone /></el-icon>
              <span class="info-label">手机</span>
              <span class="info-value">{{ userStore.userInfo?.phone }}</span>
            </div>
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <span class="info-label">邮箱</span>
              <span class="info-value">{{ userStore.userInfo?.email }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧编辑 -->
      <el-col :span="16" class="profile-col">
        <el-card class="detail-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="info">
              <el-descriptions :column="2" border class="readonly-info">
                <el-descriptions-item label="账号">{{ userStore.userInfo?.username || '-' }}</el-descriptions-item>
                <el-descriptions-item label="姓名">{{ userStore.userInfo?.name || '-' }}</el-descriptions-item>
                <el-descriptions-item label="角色">{{ userStore.displayRole }}</el-descriptions-item>
                <el-descriptions-item label="部门">{{ userStore.userInfo?.deptName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="职位">{{ userStore.userInfo?.position || '-' }}</el-descriptions-item>
                <el-descriptions-item label="手机号">{{ userStore.userInfo?.phone || '-' }}</el-descriptions-item>
                <el-descriptions-item label="邮箱">{{ userStore.userInfo?.email || '-' }}</el-descriptions-item>
                <el-descriptions-item label="状态">{{ userStore.userInfo?.status === 1 ? '在职' : '离职' }}</el-descriptions-item>
              </el-descriptions>
            </el-tab-pane>

            <el-tab-pane label="修改密码" name="password">
              <el-form :model="pwdForm" label-width="100px" style="max-width: 500px; margin-top: 16px">
                <el-form-item label="当前密码">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认新密码">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="changingPwd" @click="handleChangePwd">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores'

const userStore = useUserStore()
const activeTab = ref('info')
const changingPwd = ref(false)

const pwdForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

async function handleChangePwd() {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword || !pwdForm.value.confirmPassword) {
    ElMessage.warning('请完整填写密码信息')
    return
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  changingPwd.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    ElMessage.success('密码修改成功（Mock）')
    pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } finally {
    changingPwd.value = false
  }
}

</script>

<style lang="scss" scoped>
.profile-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .page-header {
    margin-bottom: $spacing-sm;
    h3 {
      font-size: $font-size-xl;
      font-weight: 600;
      margin: 0;
    }
  }
}

.content-row {
  flex: 1;
  min-height: 0;
}

.profile-col {
  height: 100%;
}

.profile-card {
  height: 100%;
  text-align: center;

  :deep(.el-card__body) {
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .avatar-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: $spacing-sm;

    .user-name {
      font-size: $font-size-lg;
      font-weight: 600;
      margin: 0;
    }
  }
}

.info-list {
  margin-top: 4px;

  .info-item {
    display: grid;
    grid-template-columns: 16px 48px minmax(0, 1fr);
    align-items: center;
    gap: $spacing-sm;
    padding: 10px 0;
    border-bottom: 1px solid $border-lighter;
    font-size: $font-size-sm;

    &:last-child { border-bottom: none; }

    .el-icon {
      color: $text-secondary;
      justify-self: center;
    }

    .info-label {
      color: $text-secondary;
      text-align: left;
    }

    .info-value {
      color: $text-primary;
      text-align: right;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.detail-card {
  height: 100%;

  :deep(.el-card__body) {
    height: 100%;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  :deep(.el-tabs) {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
  }

  :deep(.el-tabs__content) {
    flex: 1;
    min-height: 0;
    overflow: auto;
    padding-right: 2px;
  }
}

.readonly-info {
  margin-top: 8px;
}
</style>
