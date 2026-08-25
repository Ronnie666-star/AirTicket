import axios from 'axios'
import router from '../router'

// Axios 实例：统一挂 JWT、统一解析 ApiResponse、统一错误提示。
const http = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：有 token 就带上
http.interceptors.request.use((config) => {
  const token = localStorage.getItem('airticket_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：code !== 0 视为业务错误；401 清登录态跳登录页
http.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) {
        return body.data
      }
      return Promise.reject(new Error(body.msg || '请求失败'))
    }
    return body
  },
  (err) => {
    const status = err.response?.status
    const msg = err.response?.data?.msg
    if (status === 401) {
      localStorage.removeItem('airticket_token')
      localStorage.removeItem('airticket_user')
      if (router.currentRoute.value.name !== 'login') {
        router.push({ name: 'login' })
      }
    }
    return Promise.reject(new Error(msg || err.message || '网络错误'))
  }
)

export default http
