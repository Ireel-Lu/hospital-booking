import request from '@/utils/request'

/** 分页查询药品 */
export function getDrugList(params) {
  return request.get('/drug/list', { params })
}

/** 药品详情 */
export function getDrugById(id) {
  return request.get(`/drug/${id}`)
}

/** 所有药品（写病历时选择） */
export function getAllDrugs() {
  return request.get('/drug/all')
}

/** 搜索药品 */
export function searchDrugs(keyword) {
  return request.get('/drug/search', { params: { keyword } })
}

/** 新增药品（管理员） */
export function addDrug(data) {
  return request.post('/drug/add', data)
}

/** 更新药品（管理员） */
export function updateDrug(data) {
  return request.put('/drug/update', data)
}

/** 删除药品（管理员） */
export function deleteDrug(id) {
  return request.delete(`/drug/${id}`)
}
