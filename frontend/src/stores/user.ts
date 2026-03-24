// ============================================
// 用户状态管理
// ============================================
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserRole, RoleContext } from '@/types'
import { loginApi, getUserInfoApi, updateProfileApi, getRoleContextsApi } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<User | null>(null)
  const activeRole = ref<UserRole | ''>('')
  const activeDeptId = ref<string>('')
  const roleContexts = ref<RoleContext[]>([])

  const currentRole = computed<UserRole>(() => {
    if (activeRole.value) return activeRole.value
    return userInfo.value?.role || 'staff'
  })

  const currentDeptId = computed<string>(() => {
    if (activeDeptId.value) return activeDeptId.value
    return userInfo.value?.deptId || ''
  })

  const currentRoleContext = computed<RoleContext | undefined>(() => {
    return roleContexts.value.find(
      rc => rc.role === currentRole.value && rc.deptId === currentDeptId.value
    ) || roleContexts.value[0]
  })

  const displayRole = computed(() => currentRoleContext.value?.label || '')

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => currentRole.value === 'admin')
  const isCeo = computed(() => currentRole.value === 'ceo')
  const isDirector = computed(() => currentRole.value === 'director' || currentRole.value === 'ceo')
  const isManager = computed(() => currentRole.value === 'manager' || isDirector.value)
  const managedDeptIds = computed(() => userInfo.value?.managedDeptIds || [])
  const availableRoles = computed(() => roleContexts.value.map(rc => rc.role))

  // Actions
  async function login(username: string, password: string) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    userInfo.value = res.data.user
    activeRole.value = res.data.user.role
    activeDeptId.value = res.data.user.deptId
    setToken(res.data.token)
    await fetchRoleContexts()
    return res.data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    activeRole.value = ''
    activeDeptId.value = ''
    roleContexts.value = []
    removeToken()
  }

  async function getUserInfo() {
    if (!token.value) return
    const res = await getUserInfoApi()
    userInfo.value = res.data
    if (!activeRole.value) {
      activeRole.value = res.data.role
      activeDeptId.value = res.data.deptId
    }
    await fetchRoleContexts()
    return res.data
  }

  async function fetchRoleContexts() {
    try {
      const res = await getRoleContextsApi()
      roleContexts.value = res.data
    } catch {
      roleContexts.value = []
    }
  }

  async function updateProfile(data: Partial<User>) {
    const res = await updateProfileApi(data)
    userInfo.value = res.data
    return res.data
  }

  function hasRole(roles: UserRole[]): boolean {
    if (!userInfo.value) return false
    return roles.includes(currentRole.value)
  }

  function switchRole(ctx: RoleContext) {
    activeRole.value = ctx.role
    activeDeptId.value = ctx.deptId
  }

  return {
    token, userInfo, activeRole, activeDeptId, roleContexts,
    currentRole, currentDeptId, currentRoleContext, displayRole,
    availableRoles,
    isLoggedIn, isAdmin, isCeo, isDirector, isManager, managedDeptIds,
    login, logout, getUserInfo, fetchRoleContexts, updateProfile, hasRole, switchRole,
  }
}, {
  persist: {
    pick: ['token', 'activeRole', 'activeDeptId'],
  },
})
