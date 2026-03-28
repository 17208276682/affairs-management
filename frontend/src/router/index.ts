// ============================================
// 路由配置
// ============================================
import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores'

NProgress.configure({ showSpinner: false })

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '事务总览', icon: 'Monitor' },
      },
      // --- 事务管理 ---
      {
        path: 'task/list/:type?',
        name: 'TaskList',
        component: () => import('@/views/task/TaskListView.vue'),
        meta: { title: '事务列表', icon: 'List' },
      },
      {
        path: 'task/detail/:id',
        name: 'TaskDetail',
        component: () => import('@/views/task/TaskDetailView.vue'),
        meta: { title: '事务详情', icon: 'Document', hidden: true },
      },
      {
        path: 'task/execute/:id',
        name: 'TaskExecute',
        component: () => import('@/views/task/TaskExecuteView.vue'),
        meta: { title: '事务执行', icon: 'VideoPlay', hidden: true },
      },
      {
        path: 'task/feedback/:id',
        name: 'TaskFeedback',
        component: () => import('@/views/task/TaskFeedbackView.vue'),
        meta: { title: '事务反馈', icon: 'ChatDotRound', hidden: true },
      },
      // --- 组织管理 ---
      {
        path: 'org/tree',
        name: 'OrgTree',
        component: () => import('@/views/organization/OrgTreeView.vue'),
        meta: { title: '组织架构', icon: 'Share', parent: '组织管理', roles: ['ceo', 'manager'] },
      },
      {
        path: 'org/dept',
        name: 'DeptManage',
        component: () => import('@/views/organization/DeptManageView.vue'),
        meta: { title: '组织管理', icon: 'OfficeBuilding', roles: ['admin', 'ceo'] },
      },
      {
        path: 'org/member',
        name: 'MemberManage',
        component: () => import('@/views/organization/MemberManageView.vue'),
        meta: { title: '人员管理', icon: 'User', roles: ['admin', 'ceo'] },
      },
      // --- 事务统计 ---
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/StatisticsView.vue'),
        meta: { title: '事务统计', icon: 'DataAnalysis', roles: ['admin', 'ceo'] },
      },
      // --- 报表导出 ---
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/views/report/ReportView.vue'),
        meta: { title: '报表导出', icon: 'Document', roles: ['admin'] },
      },
      // --- 系统设置 ---
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/SettingsView.vue'),
        meta: { title: '系统设置', icon: 'Setting', roles: ['admin'] },
      },
      // --- 个人中心 ---
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { title: '个人中心', icon: 'UserFilled', hidden: true },
      },
    ],
  },
  // 404
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

// 路由守卫
function resolvePageTitle(path: string, fallback: string) {
  if (path.startsWith('/task/list/assigned')) return '我的下达'
  if (path.startsWith('/task/list/todo')) return '我的代办'
  if (path.startsWith('/task/list/scope')) return '事务列表'
  return fallback
}

router.beforeEach(async (to) => {
  NProgress.start()
  const title = resolvePageTitle(to.path, String(to.meta.title || '中小企业事务管理数智化系统'))
  document.title = `${title} - TM System`

  if (to.meta.requiresAuth === false) {
    return true
  }

  const token = getToken()
  if (!token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  // 确保用户信息已加载（正常情况下由 main.ts 预加载，这里仅作兜底）
  const userStore = useUserStore()
  if (!userStore.userInfo) {
    try {
      await userStore.getUserInfo()
    } catch {
      // token 无效，跳转登录
      userStore.logout()
      return { path: '/login', query: { redirect: to.fullPath } }
    }
  }

  // 角色重定向逻辑
  const role = userStore.currentRole

  if (role === 'admin') {
    // 管理员只能访问组织管理和个人中心
    if (to.path === '/dashboard' || to.path.startsWith('/task')) {
      return '/org/dept'
    }
  } else if (role === 'ceo' || role === 'director') {
    // 总经理/副总经理使用高级管理者界面
    if (to.path.startsWith('/org/dept') || to.path.startsWith('/org/member') || to.path.startsWith('/statistics')) {
      return '/dashboard'
    }
  } else if (role === 'staff') {
    // 普通员工不能访问事务列表（我下达的）和统计
    if (to.path === '/task/list/assigned' || to.path.startsWith('/statistics')) {
      return '/task/list/todo'
    }
  }

  return true
})

router.afterEach(() => {
  NProgress.done()
})

export default router
