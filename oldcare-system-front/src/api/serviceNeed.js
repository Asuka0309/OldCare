import http from './http'

/**
 * 获取所有服务需求（支持过滤�?
 */
export function getAllServiceNeeds(params) {
  return http.get('service-needs', { params })
}

/**
 * 获取我的服务需�?
 */
export function getMyServiceNeeds(params) {
  return http.get('service-needs/my', { params })
}

/**
 * 获取服务需求详�?
 */
export function getServiceNeedById(id) {
  return http.get(`/service-needs/${id}`)
}

/**
 * 发布新的服务需�?
 */
export function createServiceNeed(data) {
  return http.post('service-needs', data)
}

/**
 * 更新服务需�?
 */
export function updateServiceNeed(id, data) {
  return http.put(`/service-needs/${id}`, data)
}

/**
 * 删除服务需�?
 */
export function deleteServiceNeed(id) {
  return http.delete(`/service-needs/${id}`)
}

/**
 * 完成服务需�?
 */
export function completeServiceNeed(id) {
  return http.put(`/service-needs/${id}/complete`)
}


