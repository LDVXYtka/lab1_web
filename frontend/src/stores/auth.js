import { defineStore } from 'pinia'
import { request, resetCsrf, resetSession, logoutSession } from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    checked: false,
    error: '',
    version: 0
  }),
  getters: {
    isAdmin: state => state.user?.role === 'ADMIN'
  },
  actions: {
    clear() {
      this.version++
      resetSession()
      this.user = null
      this.checked = true
      this.error = ''
    },
    async loadUser(force = false) {
      if (this.checked && !force) {
        return this.user
      }
      const version = this.version
      try {
        const user = await request('GET', '/api/auth/me', undefined, { notifyExpired: false })
        if (version !== this.version) return this.user
        this.user = user
        this.checked = true
        this.error = ''
      } catch (error) {
        if (version !== this.version) return this.user
        if (error.status !== 401) {
          throw error
        }
        this.clear()
      }
      return this.user
    },
    async login(username, password) {
      const version = ++this.version
      resetSession()
      await request('POST', '/api/auth/login', { username, password }, { auth: false })
      resetCsrf()
      if (version === this.version) await this.loadUser(true)
    },
    async register(user) {
      await request('POST', '/api/auth/register', user, { auth: false })
      resetCsrf()
    },
    async logout() {
      this.version++
      await logoutSession()
      this.clear()
    }
  }
})
