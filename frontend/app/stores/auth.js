import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    checked: false
  }),
  getters: {
    isAdmin: state => state.user?.role === 'ADMIN'
  },
  actions: {
    setUser(user) {
      this.user = user
      this.checked = true
    },
    clear() {
      this.setUser(null)
    }
  }
})
