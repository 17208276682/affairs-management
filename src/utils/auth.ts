// ============================================
// Token / 认证管理
// ============================================

const TOKEN_KEY = 'tm_token'
const USER_KEY = 'tm_user'

/** 获取 Token */
export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

/** 设置 Token */
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

/** 移除 Token */
export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

/** 是否已登录 */
export function isAuthenticated(): boolean {
  return !!getToken()
}
