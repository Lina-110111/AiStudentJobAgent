import { defineStore } from 'pinia'
import { authApi } from '@/api'
import { clearToken, getToken, setToken } from '@/api/request'
import type { RoleCode, UserInfo } from '@/types/api'

interface UserState {
  token: string
  info: UserInfo | null
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: getToken(),
    info: null
  }),
  getters: {
    isLogin: (state) => !!state.token,
    roleCode: (state): RoleCode | '' => state.info?.roleCode ?? '',
    realName: (state) => state.info?.realName || state.info?.username || ''
  },
  actions: {
    async login(username: string, password: string) {
      const data = await authApi.login({ username, password })
      this.token = data.token
      this.info = data.user
      setToken(data.token)
      return data
    },
    async loadProfile() {
      this.info = await authApi.me()
      return this.info
    },
    logout() {
      this.token = ''
      this.info = null
      clearToken()
    },
    hasRole(roles: RoleCode[]): boolean {
      return !!this.info && roles.includes(this.info.roleCode)
    }
  }
})
