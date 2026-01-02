import http from './http'

export function fetchServices(params) {
  return http.get('service', { params })
}

export function fetchAllServices() {
  return http.get('service/all')
}

export function createService(data) {
  return http.post('service', data)
}

export function updateService(data) {
  return http.put('service', data)
}

export function deleteService(id) {
  return http.delete(`/service/${id}`)
}


