import path from 'node:path'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  base: '/',
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      // 前端统一请求 /api/**，由 Vite 转发到后端并去掉 /api 前缀
      // 例：/api/auth/login -> http://localhost:8081/auth/login
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/api/, '')
      },
      // 头像等上传文件由后端 /uploads/** 提供
      '/uploads': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(import.meta.dirname, './src')
    },
  },
})
