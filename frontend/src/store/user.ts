import { defineStore } from 'pinia'
import { authApi } from '@/api'
import { clearToken, getToken, setToken } from '@/api/request'
import type { LoginPayload, RoleCode, UserInfo } from '@/types/api'

interface UserState {
  token: string
  info: UserInfo | null
  /** 该用户拥有的细粒度权限码，来自后端 /api/auth/me，由 RBAC 表查询得出 */
  permissions: string[]
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: getToken(),
    info: null,
    permissions: []
  }),
  getters: {
    isLogin: (state) => !!state.token,
    roleCode: (state): RoleCode | '' => state.info?.roleCode ?? '',
    realName: (state) => state.info?.realName || state.info?.username || '',
    /** 权限数量（热更新等异常情况下 permissions 可能为空，这里兜底成 0） */
    permCount: (state) => (state.permissions ?? []).length
  },
  actions: {
    async login(payload: LoginPayload) {
      const data = await authApi.login(payload)
      this.token = data.token
      this.info = data.user
      this.permissions = []
      setToken(data.token)
      // 登录响应里不带权限码，这里补拉一次 /me，
      // 保证跳转后菜单和按钮的权限判断立刻有数据，不会因为空权限被挡在门外
      try {
        await this.loadProfile()
      } catch {
        // 权限拉取失败不影响已登录状态，进入页面后路由守卫会再拉一次
      }
      return data
    },
    /** 拉取当前用户 + 权限码，登录后与刷新时都会走这里 */
    async loadProfile() {
      const data = await authApi.me()
      this.info = data.user
      this.permissions = data.permissions ?? []
      return this.info
    },
    logout() {
      this.token = ''
      this.info = null
      this.permissions = []
      clearToken()
    },
    /** 粗粒度：角色判断（STUDENT / COUNSELOR / HR / COLLEGE_ADMIN） */
    hasRole(roles: RoleCode[]): boolean {
      return !!this.info && roles.includes(this.info.roleCode)
    },
    /** 细粒度：权限码判断，如 hasPerm('job:add') */
    hasPerm(perm: string): boolean {
      return (this.permissions ?? []).includes(perm)
    },
    /** 任意一个权限码命中即可 */
    hasAnyPerm(perms: string[]): boolean {
      return perms.some((perm) => (this.permissions ?? []).includes(perm))
    }
  }
})
