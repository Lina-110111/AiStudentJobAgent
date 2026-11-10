import { http } from './request'
import type {
  ClickCaptcha,
  DashboardData,
  ImageCaptcha,
  JobMatch,
  JobPost,
  LoginPayload,
  LoginResponse,
  PageData,
  UserInfo
} from '@/types/api'

/** 认证与用户档案 */
export const authApi = {
  login: (data: LoginPayload) => http.post<LoginResponse>('/auth/login', data),
  register: (data: Record<string, unknown>) => http.post<UserInfo>('/auth/register', data),
  me: () => http.get<UserInfo>('/auth/me'),
  updateProfile: (data: Record<string, unknown>) => http.put<UserInfo>('/users/me', data),
  // 短信登录
  sendSmsCode: (data: { phone: string; captchaId: string; captchaCode: string }) =>
    http.post<void>('/auth/sms/send-code', data),
  smsLogin: (data: Record<string, unknown>) => http.post<LoginResponse>('/auth/sms/login', data),
  // 邮箱登录
  sendEmailCode: (data: { email: string; captchaId: string; captchaCode: string }) =>
    http.post<void>('/auth/email/send-code', data),
  emailLogin: (data: {
    email: string
    code: string
    imageCaptchaId: string
    captchaCode: string
    clickCaptchaId: string
    clickCaptcha: number[]
  }) => http.post<LoginResponse>('/auth/email/login', data),
  // 忘记密码：手机号或邮箱收验证码后重置
  sendResetCode: (data: { account: string; captchaId: string; captchaCode: string }) =>
    http.post<{ channel: string; maskedAccount: string }>('/auth/password/send-code', data),
  resetPassword: (data: { account: string; code: string; newPassword: string }) =>
    http.post<null>('/auth/password/reset', data)
}

/** 验证码 */
export const captchaApi = {
  image: () => http.get<ImageCaptcha>('/captcha/image'),
  click: () => http.get<ClickCaptcha>('/captcha/click')
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
