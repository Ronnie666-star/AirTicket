import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 本地测试：pnpm dev 起在 :5173，浏览器访问 http://localhost:5173
// /api 代理到后端 :8080（本地 IDEA 启动的 Spring Boot）
// Docker 交付：该配置仅用于构建产物，nginx 负责静态托管 + /api 反代
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist'
  }
})
