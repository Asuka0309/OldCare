import http from './http'

const api = {
  list: (params) => http.get('/appointment', { params }),
  create: (data) => http.post('/appointment', data),
  update: (data) => http.put('/appointment', data),
  cancel: (id) => http.put(`/appointment/${id}/cancel`),
  confirm: (id) => http.put(`/appointment/${id}/confirm`),
  complete: (id) => http.put(`/appointment/${id}/complete`),
  getById: (id) => http.get(`/appointment/${id}`),
  delete: (id) => http.delete(`/appointment/${id}`)
}

export default api


