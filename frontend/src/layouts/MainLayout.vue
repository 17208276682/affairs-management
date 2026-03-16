<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '260px'" class="sidebar">
      <!-- Logo -->
      <div class="sidebar-logo" @click="router.push('/dashboard')">
        <el-icon :size="28" color="#4F6EF7"><Monitor /></el-icon>
        <span v-show="!isCollapsed" class="logo-text">中小企业事务管理数智化系统</span>
      </div>

      <!-- 菜单 -->
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapsed"
          :collapse-transition="false"
          router
        >
          <!-- 事务总览 — director / manager / staff -->
          <el-menu-item v-if="!userStore.isAdmin" index="/dashboard">
            <el-icon><Monitor /></el-icon>
            <template #title>事务总览</template>
          </el-menu-item>

          <!-- 事务列表 — director / manager / staff -->
          <el-menu-item v-if="!userStore.isAdmin" index="/task/list/scope">
            <el-icon><Promotion /></el-icon>
            <template #title>事务列表</template>
          </el-menu-item>

          <!-- 我的下达 — director / manager -->
          <el-menu-item v-if="userStore.isManager" index="/task/list/assigned">
            <el-icon><Position /></el-icon>
            <template #title>我的下达</template>
          </el-menu-item>

          <!-- 我的代办 — director / manager / staff -->
          <el-menu-item v-if="!userStore.isAdmin" index="/task/list/todo">
            <el-icon><Message /></el-icon>
            <template #title>我的代办</template>
          </el-menu-item>

          <!-- 组织管理 — admin -->
          <el-menu-item v-if="userStore.isAdmin" index="/org/dept">
            <el-icon><Grid /></el-icon>
            <template #title>组织管理</template>
          </el-menu-item>

          <!-- 人员管理 — admin -->
          <el-menu-item v-if="userStore.isAdmin" index="/org/member">
            <el-icon><User /></el-icon>
            <template #title>人员管理</template>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container class="main-container">
      <!-- 顶部栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon
            class="collapse-btn"
            :size="20"
            @click="isCollapsed = !isCollapsed"
          >
            <component :is="isCollapsed ? 'Expand' : 'Fold'" />
          </el-icon>
          <!-- 面包屑 -->
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-if="!isOrgPage" :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.parent && !isOrgPage">
              {{ route.meta.parent }}
            </el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRouteTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <!-- 通知 -->
          <el-popover placement="bottom-end" :width="360" trigger="click">
            <template #reference>
              <el-badge :value="notificationStore.unreadCount" :hidden="notificationStore.unreadCount === 0" :max="99">
                <el-icon :size="20" class="header-icon"><Bell /></el-icon>
              </el-badge>
            </template>
            <div class="notification-panel">
              <div class="notification-header">
                <span class="notification-title">消息通知</span>
                <el-link type="primary" :underline="false" @click="notificationStore.markAllAsRead()">
                  全部已读
                </el-link>
              </div>
              <el-scrollbar max-height="320px">
                <div
                  v-for="n in notificationStore.notifications"
                  :key="n.id"
                  class="notification-item"
                  :class="{ unread: !n.isRead }"
                  @click="handleNotificationClick(n)"
                >
                  <div class="notification-item-title">{{ n.title }}</div>
                  <div class="notification-item-content">{{ n.content }}</div>
                  <div class="notification-item-time">{{ formatRelativeTime(n.createdAt) }}</div>
                </div>
                <el-empty v-if="notificationStore.notifications.length === 0" description="暂无消息" :image-size="60" />
              </el-scrollbar>
            </div>
          </el-popover>

          <!-- 用户信息 -->
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar || undefined">
                {{ userStore.userInfo?.name?.charAt(0) }}
              </el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ userStore.userInfo?.name }}</span>
                <span class="user-role">{{ userStore.displayRole }}</span>
              </div>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore, useNotificationStore } from '@/stores'
import { formatRelativeTime } from '@/utils/format'
import type { Notification } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

const isCollapsed = ref(false)

const activeMenu = computed(() => {
  return route.path
})

const currentRouteTitle = computed(() => {
  if (route.path.startsWith('/task/list/assigned')) return '我的下达'
  if (route.path.startsWith('/task/list/todo')) return '我的代办'
  if (route.path.startsWith('/task/list/scope')) return '事务列表'
  return String(route.meta.title || '')
})

const isOrgPage = computed(() => route.path.startsWith('/org/'))

onMounted(async () => {
  // userInfo 已由路由守卫保证加载完成，这里只拉取通知
  if (userStore.isLoggedIn) {
    notificationStore.fetchNotifications()
  }
})

function handleNotificationClick(n: Notification) {
  notificationStore.markAsRead(n.id)
  if (n.relatedTaskId) {
    router.push(`/task/detail/${n.relatedTaskId}`)
  }
}

function handleUserCommand(cmd: string) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style lang="scss" scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background: $bg-sidebar;
  border-right: 1px solid $border-light;
  transition: width $transition-normal;
  overflow: hidden;

  .sidebar-logo {
    height: $header-height;
    display: flex;
    align-items: center;
    padding: 0 16px;
    cursor: pointer;
    gap: 10px;
    border-bottom: 1px solid $border-lighter;

    .logo-text {
      font-size: 13px;
      font-weight: 600;
      color: $text-primary;
      white-space: nowrap;
    }
  }
}

.main-container {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: $header-height;
  background: $bg-header;
  border-bottom: 1px solid $border-light;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-lg;
  box-shadow: $shadow-sm;

  .header-left {
    display: flex;
    align-items: center;
    gap: $spacing-md;

    .collapse-btn {
      cursor: pointer;
      color: $text-regular;
      transition: color $transition-fast;
      &:hover {
        color: $primary;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: $spacing-lg;

    .header-icon {
      cursor: pointer;
      color: $text-regular;
      transition: color $transition-fast;
      &:hover {
        color: $primary;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: $spacing-sm;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: $radius-sm;
      transition: background $transition-fast;
      &:hover {
        background: $bg-hover;
      }

      .user-detail {
        display: flex;
        flex-direction: column;
        line-height: 1.3;

        .user-name {
          font-size: $font-size-base;
          font-weight: 500;
          color: $text-primary;
        }

        .user-role {
          font-size: $font-size-xs;
          color: $text-secondary;
        }
      }
    }
  }
}

.main-content {
  flex: 1;
  min-height: 0;
  background: $bg-page;
  overflow: hidden;
  padding: $spacing-lg;
  display: flex;
  flex-direction: column;

  // 让 router-view 的根组件自动填满
  > * {
    flex: 1;
    min-height: 0;
  }
}

// 通知面板
.notification-panel {
  .notification-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: $spacing-sm;
    border-bottom: 1px solid $border-light;
    margin-bottom: $spacing-sm;

    .notification-title {
      font-weight: 600;
      font-size: $font-size-md;
    }
  }

  .notification-item {
    padding: $spacing-sm $spacing-xs;
    border-radius: $radius-sm;
    cursor: pointer;
    transition: background $transition-fast;
    border-bottom: 1px solid $border-lighter;

    &:hover {
      background: $bg-hover;
    }

    &.unread {
      background: $primary-light;
    }

    .notification-item-title {
      font-weight: 500;
      font-size: $font-size-sm;
      color: $text-primary;
    }

    .notification-item-content {
      font-size: $font-size-xs;
      color: $text-regular;
      margin-top: 4px;
    }

    .notification-item-time {
      font-size: $font-size-xs;
      color: $text-secondary;
      margin-top: 4px;
    }
  }
}
</style>
