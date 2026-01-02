import http from './http'

/**
 * 获取我的报价列表（服务提供商�?
 */
export function getMyQuotations(params) {
  return http.get('quotations/my', { params })
}

/**
 * 获取某个服务需求的所有报价（居民查看�?
 */
export function getQuotationsByNeed(needId, params) {
  return http.get(`/quotations/need/${needId}`, { params })
}

/**
 * 获取报价详情
 */
export function getQuotationById(id) {
  return http.get(`/quotations/${id}`)
}

/**
 * 提交报价（服务提供商�?
 */
export function createQuotation(data) {
  return http.post('quotations', data)
}

/**
 * 修改报价（服务提供商�?
 */
export function updateQuotation(id, data) {
  return http.put(`/quotations/${id}`, data)
}

/**
 * 接受报价（居民）
 */
export function acceptQuotation(id) {
  return http.post(`/quotations/${id}/accept`)
}

/**
 * 拒绝报价（居民）
 */
export function rejectQuotation(id) {
  return http.post(`/quotations/${id}/reject`)
}

/**
 * 删除报价（服务提供商�?
 */
export function deleteQuotation(id) {
  return http.delete(`/quotations/${id}`)
}


