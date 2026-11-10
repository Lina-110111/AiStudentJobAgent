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
        // 就业数据看板属于教务侧数据，只有具备 dashboard:view 的角色可见
        meta: { title: '就业数据看板', roles: ['COUNSELOR', 'COLLEGE_ADMIN'] as RoleCode[], perm: 'dashboard:view' }
      },
      {
        path: 'job',
        name: 'job',
        component: () => import('@/views/JobView.vue'),
        meta: { title: '岗位匹配', roles: ALL, perm: 'job:view' }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/ProfileView.vue'),
        // 个人档案对所有角色开放，作为无权限时的兜底页
        meta: { title: '个人档案', roles: ALL }
      },
      {
        path: 'rbac',
        name: 'rbac',
        component: () => import('@/views/RbacView.vue'),
        // 权限管理页：只有院系管理员（拥有 user:manage 权限码）能进
        meta: { title: '权限管理', roles: ['COLLEGE_ADMIN'] as RoleCode[], perm: 'user:manage' }
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

/** 侧边菜单：角色 + 权限码双重过滤（前端控制可见性，后端接口再做一次鉴权） */
export const menus: MenuItem[] = [
  { index: '/dashboard', title: '就业数据看板', icon: 'DataAnalysis', roles: ALL, perm: 'dashboard:view' },
  { index: '/job', title: '岗位匹配', icon: 'Briefcase', roles: ALL, perm: 'job:view' },
  { index: '/profile', title: '个人档案', icon: 'User', roles: ALL },
  { index: '/rbac', title: '权限管理', icon: 'Setting', roles: ['COLLEGE_ADMIN'] as RoleCode[], perm: 'user:manage' }
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
  const perm = to.meta.perm as string | undefined
  const roleDenied = !!roles && roles.length > 0 && !userStore.hasRole(roles)
  const permDenied = !!perm && !userStore.hasPerm(perm)
  if (roleDenied || permDenied) {
    // 回落到当前角色确实有权限的页面，避免重定向死循环
    const fallback = userStore.hasPerm('job:view') ? 'job' : 'profile'
    if (to.name === fallback) {
      // 兜底页本身不再拦截，否则用户会被卡在登录页出不去
      return true
    }
    return { name: fallback }
  }
  return true
})

export default router
