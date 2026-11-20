import request from '@/utils/request'

/**
 * 获取当前登录用户的资料卡
 */
export function getProfile() {
  return request.get('/profile')
}

/**
 * 修改个人资料
 */
export function updateProfile(data) {
  return request.put('/profile', data)
}

/**
 * 修改密码
 */
export function changePassword(data) {
  return request.put('/profile/password', data)
}

/**
 * 上传头像（multipart/form-data）
 */
export function uploadAvatar(file) {
  const form = new FormData()
  form.append('file', file)
  return request.post('/profile/avatar', form)
}

/**
 * 医生查看患者资料卡
 */
export function getPatientProfile(userId) {
  return request.get(`/profile/patient/${userId}`)
}
