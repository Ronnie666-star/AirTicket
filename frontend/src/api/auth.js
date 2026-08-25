import http from './http'

// ===== 认证 / 个人中心 / 初始化 =====
export const authApi = {
  login(data) {
    return http.post('/login', data)
  },
  register(data) {
    return http.post('/register', data)
  },
  // 初始化（首次运行创建初始管理员）
  initStatus() {
    return http.get('/init/status')
  },
  initAdmin(data) {
    return http.post('/init/admin', data)
  },
  getProfile() {
    return http.get('/user/profile')
  },
  updateProfile(data) {
    return http.put('/user/profile', data)
  },
  changePassword(data) {
    return http.put('/user/password', data)
  }
}
