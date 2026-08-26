import http from './http'

// ===== 航班 =====
export const flightApi = {
  search(params) {
    return http.get('/flight', { params })
  },
  detail(id) {
    return http.get(`/flight/${id}`)
  },
  create(data) {
    return http.post('/flight', data)
  },
  update(id, data) {
    return http.put(`/flight/${id}`, data)
  },
  remove(id) {
    return http.delete(`/flight/${id}`)
  },
  cancel(id) {
    return http.post(`/flight/${id}/cancel`)
  }
}
