import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResult } from '@/types/api'

const TOKEN_KEY = 'job-agent-token'

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

const instance: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 120000
})

instance.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

instance.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      clearToken()
      ElMessage.error('登录状态已失效，请重新登录')
      if (!location.hash.includes('/login')) {
        location.hash = '#/login'
      }
    } else if (status === 403) {
      ElMessage.error('当前角色无权访问该功能')
    } else {
      ElMessage.error(error?.response?.data?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

/** 统一拆包：业务成功返回 data，业务失败抛出并提示。 */
async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const response = await instance.request<ApiResult<T>>(config)
  const body = response.data
  if (body.code !== 20000) {
    ElMessage.error(body.message || '请求失败')
    throw new Error(body.message)
  }
  return body.data
}

export const http = {
  get: <T>(url: string, params?: Record<string, unknown>) => request<T>({ url, method: 'get', params }),
  post: <T>(url: string, data?: unknown, params?: Record<string, unknown>) =>
    request<T>({ url, method: 'post', data, params }),
  put: <T>(url: string, data?: unknown) => request<T>({ url, method: 'put', data }),
  delete: <T>(url: string) => request<T>({ url, method: 'delete' })
}

export default instance
