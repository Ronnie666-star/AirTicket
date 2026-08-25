import http from './http'

// ===== 订单 =====
export const orderApi = {
  list(params) {
    return http.get('/order', { params })
  },
  detail(id) {
    return http.get(`/order/${id}`)
  },
  book(data) {
    return http.post('/order', data)
  },
  pay(id) {
    return http.post(`/order/${id}/pay`)
  },
  confirmPay(id, success = true) {
    return http.post(`/order/${id}/pay/confirm`, { success })
  },
  cancel(id) {
    return http.post(`/order/${id}/cancel`)
  },
  verify(id) {
    return http.put(`/order/${id}/verify`)
  },
  reschedule(id, newFlightId) {
    return http.put(`/order/${id}/reschedule`, { newFlightId })
  }
}

// ===== 支付单 =====
export const payApi = {
  status(no) {
    return http.get('/pay/status', { params: { no } })
  }
}
