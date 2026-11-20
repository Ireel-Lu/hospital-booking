import request from '@/utils/request'

/** 创建预约 */
export function createAppointment(data) {
  return request.post('/appointment/create', data)
}

/** 支付预约 */
export function payAppointment(id) {
  return request.put(`/appointment/pay/${id}`)
}

/** 取消预约 */
export function cancelAppointment(id, reason) {
  return request.put(`/appointment/cancel/${id}`, { reason })
}

/** 我的预约 */
export function getMyAppointments() {
  return request.get('/appointment/my')
}

/** 条件筛选预约（科室/医生/日期/状态） */
export function filterAppointments(params) {
  return request.get('/appointment/filter', { params })
}

/** 预约详情 */
export function getAppointmentDetail(id) {
  return request.get(`/appointment/${id}`)
}

/** 完成就诊（医生） */
export function completeAppointment(id) {
  return request.put(`/appointment/complete/${id}`)
}
