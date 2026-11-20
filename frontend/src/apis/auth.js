import request from '@/utils/request'

/** 登录 */
export function login(data) {
  return request.post('/auth/login', data)
}

/** 注册（仅患者） */
export function register(data) {
  return request.post('/auth/register', data)
}

/** 登出（服务端把 token 加入 Redis 黑名单） */
export function logout() {
  return request.post('/auth/logout')
}

/** 获取当前登录用户信息 */
export function getCurrentUserInfo() {
  return request.get('/auth/info')
}
