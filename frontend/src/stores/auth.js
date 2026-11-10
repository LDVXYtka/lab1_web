import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    checked: false,
    error: ''
  }),
  getters: {
    isAdmin: state => state.user?.role === 'ADMIN'
  },
  actions: {
    setUser(user) {
      this.user = user
      this.checked = true
      this.error = ''
    },
    clear() {
      this.setUser(null)
    }
  }
})
