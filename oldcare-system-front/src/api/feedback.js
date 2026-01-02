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
 * 列表获取反馈（根据角色自动选择）
 */
export function listFeedback(params) {
  // 这个函数会在组件中根据用户角色调用对应的 API
  // 实际调用由前端组件决定
  return http.get('/feedback', { params })
}

/**
 * 获取反馈详情
 */
export function getFeedbackById(id) {
  return http.get(`/feedback/${id}`)
}

/**
 * 删除反馈
 */
export function deleteFeedback(id) {
  return http.delete(`/feedback/${id}`)
}

/**
 * 处理反馈（管理员）
 */
export function resolveFeedback(id, data) {
  return http.put(`/feedback/${id}/resolve`, data)
}
