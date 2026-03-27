// ============================================
// 用户状态管理
// ============================================
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserRole } from '@/types'
import { loginApi, getUserInfoApi, updateProfileApi } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<User | null>(null)

  const currentRole = computed<UserRole>(() => {
    return userInfo.value?.role || 'staff'
  })

  const currentDeptId = computed<string>(() => {
    return userInfo.value?.deptId || ''
  })

  const displayRole = computed(() => {
    const role = currentRole.value
    switch (role) {
      case 'ceo': return '总经理'
      case 'director': return '副总经理'
      case 'manager': return '负责人'
      case 'staff': return '普通员工'
      case 'admin': return '管理员'
      default: return role
    }
  })

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => currentRole.value === 'admin')
  const isCeo = computed(() => currentRole.value === 'ceo')
  const isDirector = computed(() => currentRole.value === 'director' || currentRole.value === 'ceo')
  const isManager = computed(() => currentRole.value === 'manager' || isDirector.value)
  const managedDeptIds = computed(() => userInfo.value?.managedDeptIds || [])

  // Actions
  async function login(username: string, password: string) {
    const res = await loginApi({ username, password })
    token.value = res.data.token
    userInfo.value = res.data.user
    setToken(res.data.token)
    return res.data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    removeToken()
  }

  async function getUserInfo() {
    if (!token.value) return
    const res = await getUserInfoApi()
    userInfo.value = res.data
    return res.data
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

  return {
    token, userInfo,
    currentRole, currentDeptId, displayRole,
    isLoggedIn, isAdmin, isCeo, isDirector, isManager, managedDeptIds,
    login, logout, getUserInfo, updateProfile, hasRole,
  }
}, {
  persist: {
    pick: ['token'],
  },
})
