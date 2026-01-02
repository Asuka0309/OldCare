import http from './http'

/**
 * 提交反馈
 */
export function submitFeedback(data) {
  return http.post('/feedback', data)
}

/**
 * 获取我的反馈列表
 */
export function getMyFeedback(params) {
  return http.get('/feedback/my', { params })
}

/**
 * 获取所有反馈（管理员）
 */
export function getAllFeedback(params) {
  return http.get('/feedback', { params })
}

/**
 * 获取反馈详情
 */
export function getFeedbackById(id) {
  return http.get(`/feedback/${id}`)
}

/**
 * 处理反馈（管理员）
 */
export function resolveFeedback(id, data) {
  return http.put(`/feedback/${id}/resolve`, data)
}
