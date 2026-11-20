import request from '@/utils/request'

/** 用户列表（管理员） */
export function getUserList(params) {
  return request.get('/user/list', { params })
}

/** 启用/禁用用户 */
export function updateUserStatus(data) {
  return request.put('/user/status', data)
}

/** 编辑用户信息 */
export function updateUser(data) {
  return request.put('/user/update', data)
}

/** 重置密码 */
export function resetPassword(data) {
  return request.put('/user/reset-password', data)
}

/** 删除用户 */
export function deleteUser(id) {
  return request.delete(`/user/${id}`)
}
