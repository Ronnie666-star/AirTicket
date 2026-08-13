import axios from 'axios'
import { ElMessage } from 'element-plus'

// 统一 axios 封装：baseURL 用 /api，Vite dev 代理 与 Docker nginx 反代 都指向它
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截：自动带上 JWT token（存储键约定为 airticket-token）
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('airticket-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一错误提示，401 跳登录
request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      localStorage.removeItem('airticket-token')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.response?.data?.message || '请求失败，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
