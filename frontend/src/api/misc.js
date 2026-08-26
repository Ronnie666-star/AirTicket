import http from './http'

// ===== 常用乘机人 / 轨迹 / 管理 =====
export const passengerApi = {
  list() {
    return http.get('/passenger')
  },
  add(passengerId) {
    return http.post('/passenger', { passengerId })
  },
  remove(id) {
    return http.delete(`/passenger/${id}`)
  }
}

export const routeApi = {
  get(flightId) {
    return http.get('/route', { params: { flightId } })
  },
  update(flightId, data) {
    return http.put(`/route/${flightId}`, data)
  }
}

export const adminApi = {
  users(params) {
    return http.get('/admin/users', { params })
  },
  createUser(data) {
    return http.post('/admin/users', data)
  },
  changeStatus(id, enabled) {
    return http.put(`/admin/users/${id}/status`, { enabled })
  },
  resetPassword(id, newPassword) {
    return http.put(`/admin/users/${id}/password`, { newPassword })
  }
}

export const masterApi = {
  list(kind) {
    return http.get(`/master/${kind}`)
  },
  create(kind, data) {
    return http.post(`/master/${kind}`, data)
  },
  update(kind, id, data) {
    return http.put(`/master/${kind}/${id}`, data)
  },
  remove(kind, id) {
    return http.delete(`/master/${kind}/${id}`)
  }
}
