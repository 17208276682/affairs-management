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
          <!-- 事务总览 — ceo / director / manager / staff -->
          <el-menu-item v-if="!userStore.isAdmin" index="/dashboard">
            <el-icon><Monitor /></el-icon>
            <template #title>事务总览</template>
          </el-menu-item>

          <!-- 事务列表 — ceo / director / manager / staff -->
          <el-menu-item v-if="!userStore.isAdmin" index="/task/list/scope">
            <el-icon><Promotion /></el-icon>
            <template #title>事务列表</template>
          </el-menu-item>

          <!-- 我的下达 — director / manager -->
          <el-menu-item v-if="userStore.isManager" index="/task/list/assigned">
            <el-icon><Position /></el-icon>
            <template #title>我的下达</template>
          </el-menu-item>

          <!-- 我的代办 — ceo / director / manager / staff -->
          <el-menu-item v-if="!userStore.isAdmin" index="/task/list/todo">
            <el-icon><Message /></el-icon>
            <template #title>我的代办</template>
          </el-menu-item>

          <!-- 组织管理 — 管理员 -->
          <el-menu-item v-if="userStore.isAdmin" index="/org/dept">
            <el-icon><Grid /></el-icon>
            <template #title>组织管理</template>
          </el-menu-item>

          <!-- 人员管理 — 管理员 -->
          <el-menu-item v-if="userStore.isAdmin" index="/org/member">
            <el-icon><User /></el-icon>
            <template #title>人员管理</template>
          </el-menu-item>

          <!-- 事务统计 — 管理员 -->
          <el-menu-item v-if="userStore.isAdmin" index="/statistics">
            <el-icon><DataAnalysis /></el-icon>
            <template #title>事务统计</template>
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
          <!-- 用户信息 -->
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar || undefined">
                {{ displayUserName?.charAt(0) }}
              </el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ displayUserName }}</span>
                <span class="user-role">{{ displayUserRole }}</span>
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
            <component :is="Component" :key="viewRenderKey" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

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
const displayUserName = computed(() => userStore.currentRole === 'admin' ? '管理员' : (userStore.userInfo?.name || ''))
const displayUserRole = computed(() => userStore.currentRole === 'admin' ? '' : userStore.displayRole)
const viewRenderKey = computed(() => route.fullPath)

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
</style>
