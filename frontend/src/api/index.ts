import { http } from './request'
import type {
  DashboardData,
  InterviewSession,
  JobMatch,
  JobPost,
  LoginResponse,
  PageResult,
  PolicyItem,
  ResumeItem,
  UserInfo
} from '@/types/api'

/** 认证与用户 */
export const authApi = {
  login: (data: { username: string; password: string }) =>
    http.post<LoginResponse>('/auth/login', data),
  register: (data: Record<string, unknown>) => http.post<UserInfo>('/auth/register', data),
  me: () => http.get<UserInfo>('/auth/me'),
  updateProfile: (data: Record<string, unknown>) => http.put<UserInfo>('/users/me', data)
}

/** 智能简历 */
export const resumeApi = {
  list: (pageNum = 1, pageSize = 10) =>
    http.get<PageResult<ResumeItem>>('/resumes', { pageNum, pageSize }),
  create: (data: Record<string, unknown>) => http.post<ResumeItem>('/resumes', data),
  diagnose: (id: number, targetJob?: string) =>
    http.post<Record<string, unknown>>(`/resumes/${id}/diagnose`, null, { targetJob }),
  detail: (id: number) => http.get<ResumeItem>(`/resumes/${id}`)
}

/** 岗位匹配与申请 */
export const jobApi = {
  list: (params: Record<string, unknown> = {}) =>
    http.get<PageResult<JobPost>>('/jobs', { pageNum: 1, pageSize: 10, ...params }),
  detail: (id: number) => http.get<JobPost>(`/jobs/${id}`),
  publish: (data: Record<string, unknown>) => http.post<JobPost>('/jobs', data),
  offline: (id: number) => http.delete<null>(`/jobs/${id}`),
  match: (id: number) => http.get<JobMatch>(`/jobs/${id}/match`),
  apply: (id: number, resumeId?: number, matchScore?: number) =>
    http.post<unknown>(`/jobs/${id}/apply`, null, { resumeId, matchScore }),
  myApplications: () => http.get<Array<Record<string, unknown>>>('/jobs/my/applications')
}

/** 实习管理 */
export const internshipApi = {
  apply: (data: Record<string, unknown>) => http.post<unknown>('/internships/apply', data),
  my: (pageNum = 1, pageSize = 10) =>
    http.get<PageResult<Record<string, unknown>>>('/internships/my', { pageNum, pageSize }),
  pending: (pageNum = 1, pageSize = 10) =>
    http.get<PageResult<Record<string, unknown>>>('/internships/pending', { pageNum, pageSize }),
  approve: (id: number, data: { approve: boolean; comment?: string }) =>
    http.post<unknown>(`/internships/${id}/approve`, data),
  submitLog: (id: number, data: Record<string, unknown>) =>
    http.post<unknown>(`/internships/${id}/logs`, data),
  logs: (id: number) => http.get<Array<Record<string, unknown>>>(`/internships/${id}/logs`)
}

/** AI 面试训练 */
export const interviewApi = {
  start: (data: { category: string; difficulty: string }) =>
    http.post<InterviewSession>('/interviews/start', data),
  answer: (id: number, data: { questionIndex: number; answer: string }) =>
    http.post<Record<string, unknown>>(`/interviews/${id}/answer`, data),
  finish: (id: number) => http.post<Record<string, unknown>>(`/interviews/${id}/finish`),
  history: (pageNum = 1, pageSize = 10) =>
    http.get<PageResult<InterviewSession>>('/interviews/history', { pageNum, pageSize })
}

/** 政策问答 */
export const policyApi = {
  list: (params: Record<string, unknown> = {}) =>
    http.get<PageResult<PolicyItem>>('/policies', { pageNum: 1, pageSize: 10, ...params }),
  ask: (question: string) => http.post<Record<string, unknown>>('/policies/ask', { question }),
  hot: (topN = 10) => http.get<Record<string, unknown>>('/policies/hot', { topN })
}

/** 数据看板 */
export const dashboardApi = {
  overview: () => http.get<DashboardData>('/dashboard/overview')
}

/** AI 能力状态 */
export const aiApi = {
  status: () => http.get<Record<string, unknown>>('/ai/status')
}
