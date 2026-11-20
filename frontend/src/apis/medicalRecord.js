import request from '@/utils/request'

/** 创建病历（医生） */
export function createMedicalRecord(data) {
  return request.post('/medical-record/create', data)
}

/** 上传病历图片（OSS） */
export function uploadMedicalRecordImage(recordId, file) {
  const form = new FormData()
  form.append('file', file)
  return request.post(`/medical-record/${recordId}/images`, form)
}

/** 患者就诊记录 */
export function getPatientRecords(patientId) {
  return request.get(`/medical-record/patient/${patientId}`)
}

/** 添加处方（医生） */
export function addPrescription(data) {
  return request.post('/medical-record/prescription/add', data)
}

/** 病历对应的处方列表 */
export function getPrescriptions(recordId) {
  return request.get(`/medical-record/prescription/${recordId}`)
}
