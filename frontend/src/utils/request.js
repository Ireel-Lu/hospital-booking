import axios from 'axios'
import { ElMessage } from 'element-plus'

// 所有请求统一走 /api 前缀，由 vite 代理转发到后端 8081 端口
const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

/**
 * 请求拦截器：自动携带 JWT
 */
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

/**
 * 清理登录态并跳转登录页
 */
function toLogin() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  if (!window.location.pathname.startsWith('/login')) {
    window.location.href = '/login'
  }
}

/**
 * 响应拦截器
 * 后端统一返回 { code, massage, data }，code=200 视为成功
 */
request.interceptors.response.use(
  (response) => {
    // 文件下载（Excel）直接返回原始响应
    if (response.config.responseType === 'blob') {
      return response
    }

    const res = response.data
    if (res && typeof res.code !== 'undefined' && res.code !== 200) {
      const message = res.massage || res.message || '操作失败'
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    return res ? res.data : res
  },
  (error) => {
    const status = error.response?.status
    const data = error.response?.data
    const message = data?.massage || data?.message || error.message || '网络错误'

    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      toLogin()
    } else if (status === 403) {
      ElMessage.error(message || '无权限访问该接口')
    } else {
      ElMessage.error(message)
    }

    return Promise.reject(error)
  }
)

export default request
