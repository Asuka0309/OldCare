import http from './http'

const api = {
  list: (params) => http.get('evaluation', { params }),
  add: (data) => http.post('evaluation', data),
  update: (data) => http.put(`/evaluation/${data.id}`, data),
  delete: (id) => http.delete(`/evaluation/${id}`),
  getById: (id) => http.get(`/evaluation/${id}`),
  getComplaints: (params) => http.get('evaluation/complaints', { params })
}

export default api


