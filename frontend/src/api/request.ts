// ============================================
// Axios 请求封装
// ============================================
import axios from 'axios'
import type { AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import { useUserStore } from '@/stores'
import type { ApiResponse } from '@/types'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 多角色账号：透传当前激活角色和部门，后端按该角色返回数据与权限
    const userStore = useUserStore()
    if (userStore.currentRole) {
      config.headers['X-Active-Role'] = userStore.currentRole
    }
    if (userStore.activeDeptId) {
      config.headers['X-Active-Dept'] = userStore.activeDeptId
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      if (res.code === 401) {
        removeToken()
        // 延迟跳转避免循环
        setTimeout(() => { window.location.hash = '#/login' }, 100)
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res as any
  },
  (error) => {
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

/** GET 请求 */
export function get<T = any>(url: string, params?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.get(url, { params, ...config }) as any
}

/** POST 请求 */
export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.post(url, data, config) as any
}

/** PUT 请求 */
export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.put(url, data, config) as any
}

/** DELETE 请求 */
export function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
  return service.delete(url, config) as any
}

export default service
