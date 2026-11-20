import request from '@/utils/request'

/** 科室列表（公共） */
export function getDepartmentList(keyword) {
  return request.get('/department/list', { params: { keyword } })
}

/** 科室详情 */
export function getDepartmentById(id) {
  return request.get(`/department/${id}`)
}

/** 新增科室（管理员） */
export function addDepartment(data) {
  return request.post('/department/add', data)
}

/** 更新科室（管理员） */
export function updateDepartment(data) {
  return request.put('/department/update', data)
}

/** 删除科室（管理员） */
export function deleteDepartment(id) {
  return request.delete(`/department/delete/${id}`)
}
