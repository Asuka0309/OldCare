import http from './http'

/**
 * 获取所有社区活�?
 */
export function getAllActivities(params) {
  return http.get('activities', { params })
}

/**
 * 获取社区活动详情
 */
export function getActivityById(id) {
  return http.get(`/activities/${id}`)
}

/**
 * 新建社区活动（管理员�?
 */
export function createActivity(data) {
  return http.post('activities', data)
}

/**
 * 更新社区活动（管理员�?
 */
export function updateActivity(id, data) {
  return http.put(`/activities/${id}`, data)
}

/**
 * 删除社区活动（管理员�?
 */
export function deleteActivity(id) {
  return http.delete(`/activities/${id}`)
}

/**
 * 取消社区活动（管理员）
 */
export function cancelActivity(id) {
  return http.put(`/activities/${id}/cancel`)
}

/**
 * 报名参加活动
 */
export function registerActivity(id) {
  return http.post(`/activities/${id}/register`)
}

/**
 * 取消活动报名
 */
export function unregisterActivity(id) {
  return http.delete(`/activities/${id}/register`)
}

/**
 * 完成社区活动（管理员�?
 */
export function completeActivity(id) {
  return http.put(`/activities/${id}/complete`)
}


