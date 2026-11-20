import request from '@/utils/request'

/** 操作日志列表（管理员） */
export function getLogList() {
  return request.get('/log/list')
}
