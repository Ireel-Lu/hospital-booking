import request from '@/utils/request'

/** 医生列表（公共） */
export function getDoctorList(params) {
  return request.get('/doctor/list', { params })
}

/** 医生详情 */
export function getDoctorById(id) {
  return request.get(`/doctor/${id}`)
}

/** 当前登录医生信息（医生端页面取 doctorId） */
export function getCurrentDoctor() {
  return request.get('/doctor/current')
}

/** 医生某天的排班 */
export function getDoctorSchedule(params) {
  return request.get('/doctor/schedule', { params })
}

/** 新增医生（管理员） */
export function addDoctor(data) {
  return request.post('/doctor/add', data)
}

/** 更新医生（管理员） */
export function updateDoctor(data) {
  return request.put('/doctor/update', data)
}

/** 删除医生（管理员） */
export function deleteDoctor(id) {
  return request.delete(`/doctor/delete/${id}`)
}

/** 保存号源设置（医生/管理员） */
export function saveSourceSetting(data) {
  return request.post('/doctor/source-setting/save', data)
}

/** 查询某天号源设置（公共） */
export function getSourceSetting(params) {
  return request.get('/doctor/source-setting/list', { params })
}
