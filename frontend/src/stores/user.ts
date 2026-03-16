// ============================================
// 用户状态管理
// ============================================
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, UserRole } from '@/types'
import { loginApi, getUserInfoApi, updateProfileApi } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { ROLE_MAP } from '@/utils/constants'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<User | null>(null)

  // Getters
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')
  const isDirector = computed(() => userInfo.value?.role === 'director')
  const isManager = computed(() => userInfo.value?.role === 'manager' || userInfo.value?.role === 'director')
  const managedDeptIds = computed(() => userInfo.value?.managedDeptIds || [])
  const displayRole = computed(() => ROLE_MAP[userInfo.value?.role || ''] || '未知')

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
    return roles.includes(userInfo.value.role)
  }

  return {
    token, userInfo,
    isLoggedIn, isAdmin, isDirector, isManager, managedDeptIds, displayRole,
    login, logout, getUserInfo, updateProfile, hasRole,
  }
}, {
  persist: {
    pick: ['token'],
  },
})
