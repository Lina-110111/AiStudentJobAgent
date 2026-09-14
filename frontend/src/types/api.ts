/** 与后端 common/api/Result 对应的统一响应结构 */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: number
}

/** 分页结构 */
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

export type RoleCode = 'STUDENT' | 'COUNSELOR' | 'HR' | 'COLLEGE_ADMIN'

export interface UserInfo {
  id: number
  username: string
  realName: string
  roleCode: RoleCode
  roleLabel: string
  phone?: string
  email?: string
  avatar?: string
  college?: string
  major?: string
  grade?: string
  education?: string
  skills?: string
  jobIntention?: string
  createTime?: string
}

export interface LoginResponse {
  token: string
  expiresIn: number
  user: UserInfo
}

export interface JobPost {
  id: number
  title: string
  companyName: string
  jobCategory?: string
  city?: string
  salaryMin?: number
  salaryMax?: number
  headcount?: number
  educationReq?: string
  description?: string
  requirement?: string
  deadline?: string
  status?: number
  createTime?: string
}

export interface JobMatch {
  jobId: number
  jobTitle: string
  companyName: string
  matchScore: number
  matchLevel: string
  matchedSkills: string[]
  missingSkills: string[]
  scoreReasons: string[]
  agentExplanation: Record<string, unknown>
}

export interface ResumeItem {
  id: number
  title: string
  targetJob?: string
  fileName?: string
  filePath?: string
  fileType?: string
  score?: number
  createTime?: string
}

export interface InterviewSession {
  id: number
  category: string
  difficulty: string
  questionsJson: string
  answersJson: string
  reportJson?: string
  totalScore?: number
  status: string
  createTime?: string
}

export interface PolicyItem {
  id: number
  title: string
  category?: string
  region?: string
  publishOrg?: string
  publishDate?: string
  content?: string
  tags?: string
  viewCount?: number
}

/** 数据看板（就业率 / 行业分布 / 薪资分析 三大核心图表） */
export interface DashboardData {
  employmentRate: number
  industryDistribution: Array<{ name: string; value: number }>
  salaryAnalysis: Array<{ name: string; avgSalary: number; jobCount: number }>
  applicationTrend: Array<{ day: string; value: number }>
  applicationStatus: Array<{ name: string; value: number }>
  statistics: Record<string, number>
}

/** 菜单项 */
export interface MenuItem {
  index: string
  title: string
  icon: string
  roles: RoleCode[]
}
