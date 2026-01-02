import http from './http'

export function fetchUsers(params) {
  return http.get('user', { params })
}

export function createUser(data) {
  return http.post('user', data)
}

export function updateUser(data) {
  return http.put('user', data)
}

export function deleteUser(id) {
  return http.delete(`/user/${id}`)
}

export function updateUserStatus(id, status) {
  return http.put(`/user/${id}/status`, null, { params: { status } })
}


