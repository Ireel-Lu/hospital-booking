import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, getCurrentUserInfo } from '@/apis/auth'

/**
 * 当前登录用户状态
 * token 与 userInfo 持久化到 localStorage：
 * 刷新页面时只要 token 未过期、未进 Redis 黑名单，就保持登录态
 */
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const role = computed(() => userInfo.value?.role || '')
  const realName = computed(() => userInfo.value?.realName || userInfo.value?.username || '用户')
  const userId = computed(() => userInfo.value?.id || null)
  const avatar = computed(() => userInfo.value?.avatar || '')

  function setAuth(data) {
    token.value = data.token
    userInfo.value = {
      id: data.userId,
      username: data.username,
      realName: data.realName,
      role: data.role
    }
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  async function login(form) {
    const data = await loginApi(form)
    setAuth(data)
    //登录后补充头像、性别等资料；失败不影响登录态
    try {
      await fetchInfo()
    } catch (e) {
      // 忽略
    }
    return data
  }

  async function fetchInfo() {
    const data = await getCurrentUserInfo()
    updateUserInfo(data)
    return data
  }

  /**
   * 局部更新本地用户信息（个人中心修改资料后同步）
   */
  function updateUserInfo(partial) {
    userInfo.value = { ...userInfo.value, ...partial }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      // token 可能已过期，忽略服务端登出失败，本地仍然清理
    }
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function hasRole(...roles) {
    return roles.includes(role.value)
  }

  return { token, userInfo, role, realName, userId, avatar, login, logout, fetchInfo, setAuth, updateUserInfo, hasRole }
})
