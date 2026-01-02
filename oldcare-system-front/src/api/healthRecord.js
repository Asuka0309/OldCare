import http from './http'

/**
 * 获取我的健康记录列表
 */
export function getMyHealthRecords(params) {
  return http.get('/health-records/my', { params })
}

/**
 * 获取所有健康记录（分页�?
 */
export function getAllHealthRecords(params) {
  return http.get('/health-records', { params })
}

/**
 * 获取单个健康记录详情
 */
export function getHealthRecordById(id) {
  return http.get(`/health-records/${id}`)
}

/**
 * 新增健康记录
 */
export function createHealthRecord(data) {
  return http.post('/health-records', data)
}

/**
 * 更新健康记录
 */
export function updateHealthRecord(id, data) {
  return http.put(`/health-records/${id}`, data)
}

/**
 * 删除健康记录
 */
export function deleteHealthRecord(id) {
  return http.delete(`/health-records/${id}`)
}


