import { defineStore } from 'pinia'
import { login as loginApi } from '../api/auth'

const TOKEN_KEY = 'oldcare_token'
const USER_KEY = 'oldcare_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: '',
    user: null,
    initialized: false
  }),
  actions: {
    hydrate() {
      this.token = sessionStorage.getItem(TOKEN_KEY) || ''
      const savedUser = sessionStorage.getItem(USER_KEY)
      this.user = savedUser ? JSON.parse(savedUser) : null
      this.initialized = true
    },
    async login(payload) {
      const data = await loginApi(payload)
      console.log('Login response data:', data)
      this.token = data.token
      this.user = {
        id: data.userId,
        username: data.username,
        role: data.role,
        realName: data.realName
      }
      console.log('User stored:', this.user)
      sessionStorage.setItem(TOKEN_KEY, this.token)
      sessionStorage.setItem(USER_KEY, JSON.stringify(this.user))
    },
    logout() {
      this.token = ''
      this.user = null
      sessionStorage.removeItem(TOKEN_KEY)
      sessionStorage.removeItem(USER_KEY)
    }
  }
})
