// ============================================
// 用户 & 认证 相关类型
// ============================================

/** 用户角色 */
export type UserRole = 'admin' | 'ceo' | 'director' | 'manager' | 'staff'

/** 用户状态 */
export type UserStatus = 0 | 1 // 0=离职 1=在职

/** 用户信息 */
export interface User {
  id: string
  username: string
  name: string
  avatar: string
  phone: string
  email: string
  deptId: string
  deptName: string
  position: string
  role: UserRole
  managedDeptIds: string[]
  status: UserStatus
  createdAt: string
  updatedAt: string
}

/** 登录请求 */
export interface LoginRequest {
  username: string
  password: string
}

/** 登录响应 */
export interface LoginResponse {
  token: string
  user: User
}

/** 角色上下文：描述用户可切换的角色视角 */
export interface RoleContext {
  role: UserRole
  deptId: string
  deptName: string
  label: string
}
