import { defineStore } from 'pinia'

// 认证 store：token + 用户信息，持久化到 localStorage，刷新保持登录。
const TOKEN_KEY = 'airticket_token'
const USER_KEY = 'airticket_user'

function readUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY)) || null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: readUser()   // { userId, username, realName, role }
  }),
  getters: {
    isLoggedIn: (s) => Boolean(s.token),
    isAdmin: (s) => s.user?.role === 'ADMIN',
    isMerchant: (s) => s.user?.role === 'MERCHANT',
    canManageFlights: (s) => s.user && ['MERCHANT', 'ADMIN'].includes(s.user.role)
  },
  actions: {
    setLogin(token, user) {
      this.token = token
      this.user = user
      localStorage.setItem(TOKEN_KEY, token)
      localStorage.setItem(USER_KEY, JSON.stringify(user))
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    }
  }
})
