// ============================================
// 用户 / 认证 API
// ============================================
import { get, post, put } from './request'
import type { LoginRequest, LoginResponse, User } from '@/types'

/** 登录 */
export function loginApi(data: LoginRequest) {
  return post<LoginResponse>('/auth/login', data)
}

/** 获取当前用户信息 */
export function getUserInfoApi() {
  return get<User>('/auth/userinfo')
}

/** 更新个人信息 */
export function updateProfileApi(data: Partial<User>) {
  return put<User>('/user/profile', data)
}

/** 手机号重置密码（登录页） */
export function resetPasswordApi(data: { phone: string; newPassword: string }) {
  return post<boolean>('/auth/reset-password', data)
}

/** 修改密码（已登录用户） */
export function changePasswordApi(data: { oldPassword: string; newPassword: string }) {
  return post<boolean>('/auth/change-password', data)
}
