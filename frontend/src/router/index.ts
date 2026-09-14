import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import type { MenuItem, RoleCode } from '@/types/api'

const ALL: RoleCode[] = ['STUDENT', 'COUNSELOR', 'HR', 'COLLEGE_ADMIN']

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/DefaultLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '就业数据看板', roles: ALL }
      },
      {
        path: 'resume',
        name: 'resume',
        component: () => import('@/views/ResumeView.vue'),
        meta: { title: '智能简历', roles: ['STUDENT', 'COUNSELOR'] }
      },
      {
        path: 'job',
        name: 'job',
        component: () => import('@/views/JobView.vue'),
        meta: { title: '岗位匹配', roles: ALL }
      },
      {
        path: 'internship',
        name: 'internship',
        component: () => import('@/views/InternshipView.vue'),
        meta: { title: '实习管理', roles: ALL }
      },
      {
        path: 'interview',
        name: 'interview',
        component: () => import('@/views/InterviewView.vue'),
        meta: { title: 'AI 面试训练', roles: ['STUDENT', 'COUNSELOR'] }
      },
      {
        path: 'policy',
        name: 'policy',
        component: () => import('@/views/PolicyView.vue'),
        meta: { title: '政策问答', roles: ALL }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/ProfileView.vue'),
        meta: { title: '个人档案', roles: ALL }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { public: true, title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

/** 侧边菜单：按角色过滤（前端控制可见性，后端接口再做一次鉴权） */
export const menus: MenuItem[] = [
  { index: '/dashboard', title: '就业数据看板', icon: 'DataAnalysis', roles: ALL },
  { index: '/resume', title: '智能简历', icon: 'Document', roles: ['STUDENT', 'COUNSELOR'] },
  { index: '/job', title: '岗位匹配', icon: 'Briefcase', roles: ALL },
  { index: '/internship', title: '实习管理', icon: 'OfficeBuilding', roles: ALL },
  { index: '/interview', title: 'AI 面试训练', icon: 'Microphone', roles: ['STUDENT', 'COUNSELOR'] },
  { index: '/policy', title: '政策问答', icon: 'ChatDotRound', roles: ALL },
  { index: '/profile', title: '个人档案', icon: 'User', roles: ALL }
]

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  if (to.meta.public) {
    return true
  }
  if (!userStore.isLogin) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (!userStore.info) {
    try {
      await userStore.loadProfile()
    } catch {
      userStore.logout()
      return { name: 'login' }
    }
  }
  const roles = to.meta.roles as RoleCode[] | undefined
  if (roles && roles.length > 0 && !userStore.hasRole(roles)) {
    return { name: 'dashboard' }
  }
  return true
})

export default router
