import { http } from './request'
import type { DashboardData, JobMatch, JobPost, LoginResponse, PageData, UserInfo } from '@/types/api'

/** 认证与用户档案 */
export const authApi = {
  login: (data: { username: string; password: string }) => http.post<LoginResponse>('/auth/login', data),
  register: (data: Record<string, unknown>) => http.post<UserInfo>('/auth/register', data),
  me: () => http.get<UserInfo>('/auth/me'),
  updateProfile: (data: Record<string, unknown>) => http.put<UserInfo>('/users/me', data)
}

/** 岗位匹配与申请 */
export const jobApi = {
  list: (params: Record<string, unknown> = {}) =>
    http.get<PageData<JobPost>>('/jobs', { pageNum: 1, pageSize: 10, ...params }),
  detail: (id: number) => http.get<JobPost>(`/jobs/${id}`),
  publish: (data: Record<string, unknown>) => http.post<JobPost>('/jobs', data),
  offline: (id: number) => http.delete<null>(`/jobs/${id}`),
  match: (id: number) => http.get<JobMatch>(`/jobs/${id}/match`),
  apply: (id: number, resumeId?: number, matchScore?: number) =>
    http.post<unknown>(`/jobs/${id}/apply`, null, { resumeId, matchScore }),
  myApplications: () => http.get<Array<Record<string, unknown>>>('/jobs/my/applications')
}

/** 就业数据看板 */
export const dashboardApi = {
  overview: () => http.get<DashboardData>('/dashboard/overview')
}
