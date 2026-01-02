import http from './http'

export function fetchElderly(params) {
  return http.get('elderly', { params })
}

export function fetchAllElderly() {
  return http.get('elderly/all')
}

export function createElderly(data) {
  return http.post('elderly', data)
}

export function updateElderly(data) {
  return http.put('elderly', data)
}

export function deleteElderly(id) {
  return http.delete(`/elderly/${id}`)
}


