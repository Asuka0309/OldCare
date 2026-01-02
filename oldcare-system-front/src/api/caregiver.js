import http from './http'

export function fetchCaregivers(params) {
  return http.get('caregiver', { params })
}

export function fetchAllCaregivers() {
  return http.get('caregiver/all')
}

export function createCaregiver(data) {
  return http.post('caregiver', data)
}

export function updateCaregiver(data) {
  return http.put('caregiver', data)
}

export function deleteCaregiver(id) {
  return http.delete(`/caregiver/${id}`)
}

export function updateCaregiverRating(id, rating) {
  return http.put(`/caregiver/${id}/rating`, null, { params: { rating } })
}


