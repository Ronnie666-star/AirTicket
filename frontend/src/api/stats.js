import http from './http'

// ===== 管理员统计（数据统计页）=====
export const statsApi = {
  // 热门航班销量 Top：非取消订单数 / 成交金额 / 座舱容量
  flightSales(limit = 10) {
    return http.get('/admin/stats/flight-sales', { params: { limit } })
  },
  // 营收总览（单行汇总）
  revenue() {
    return http.get('/admin/stats/revenue')
  },
  // 渠道营收占比
  channels() {
    return http.get('/admin/stats/revenue/channels')
  },
  // 旅客消费排行 Top
  topPassengers(limit = 10) {
    return http.get('/admin/stats/top-passengers', { params: { limit } })
  }
}
