import http from './http'

/**
 * 获取所有紧急求助（管理员）
 */
export function getAllEmergencyHelps(params) {
  return http.get('/emergency-help', { params })
}

/**
 * 获取我的紧急求助
 */
export function getMyEmergencyHelps(params) {
  return http.get('/emergency-help/my', { params })
}

/**
 * 获取紧急求助详情
 */
export function getEmergencyHelpById(id) {
  return http.get(`/emergency-help/${id}`)
}

/**
 * 发起紧急求助
 */
export function createEmergencyHelp(data) {
  return http.post('/emergency-help', data)
}

/**
 * 响应紧急求助（管理员或员工）
 */
export function respondEmergencyHelp(id, data) {
  return http.put(`/emergency-help/${id}/respond`, data)
}

/**
 * 解决紧急求助
 */
export function resolveEmergencyHelp(id, data) {
  return http.put(`/emergency-help/${id}/resolve`, data)
}

/**
 * 删除紧急求助
 */
export function deleteEmergencyHelp(id) {
  return http.delete(`/emergency-help/${id}`)
}

/**
 * 取消紧急求助（发起人）
 */
export function cancelEmergencyHelp(id) {
  return http.put(`/emergency-help/${id}/cancel`)
}


