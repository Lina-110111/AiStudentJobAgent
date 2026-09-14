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
        path: 'job',
        name: 'job',
        component: () => import('@/views/JobView.vue'),
        meta: { title: '岗位匹配', roles: ALL }
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
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

/** 侧边菜单：按角色过滤（前端控制可见性，后端接口再做一次鉴权） */
export const menus: MenuItem[] = [
  { index: '/dashboard', title: '就业数据看板', icon: 'DataAnalysis', roles: ALL },
  { index: '/job', title: '岗位匹配', icon: 'Briefcase', roles: ALL },
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
