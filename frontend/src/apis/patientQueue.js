import request from '@/utils/request'

/** 医生当前候诊队列（userId 为登录用户ID） */
export function getQueueStatus(userId) {
  return request.get(`/patient-queue/status/${userId}`)
}

/** 呼叫下一位 */
export function callNextPatient(userId) {
  return request.post(`/patient-queue/call-next/${userId}`)
}

/** 跳过当前患者 */
export function skipPatient(queueId) {
  return request.post(`/patient-queue/skip/${queueId}`)
}

/** 患者查看自己的候诊进度 */
export function getMyQueueStatus() {
  return request.get('/patient-queue/my')
}
