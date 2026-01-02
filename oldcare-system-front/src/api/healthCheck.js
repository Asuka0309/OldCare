import http from './http'

export function fetchHealthChecks(params) {
  return http.get('/health-check', { params })
}

export function createHealthCheck(data) {
  return http.post('/health-check', data)
}

export function updateHealthCheck(data) {
  return http.put('/health-check', data)
}

export function deleteHealthCheck(id) {
  return http.delete(`/health-check/${id}`)
}


