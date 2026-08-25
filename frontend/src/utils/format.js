// 通用格式化工具
export function formatDateTime(dt) {
  if (!dt) return '--'
  return String(dt).replace('T', ' ').slice(0, 16)
}

export function formatDate(dt) {
  if (!dt) return '--'
  return String(dt).slice(0, 10)
}

export function formatTime(dt) {
  if (!dt) return '--'
  return String(dt).slice(11, 16)
}

export function yuan(n) {
  if (n === null || n === undefined) return '--'
  return `¥${Number(n).toFixed(2)}`
}

// 舱级展示名
export const CABIN_LABELS = {
  FIRST_CLASS: '头等舱',
  BUSINESS_CLASS: '商务舱',
  ECONOMY_CLASS: '经济舱'
}

// 订单状态展示名
export const ORDER_STATUS_LABELS = {
  PENDING_TICKET_ISSUANCE: '待出票',
  ISSUED_TICKET: '已出票',
  VERIFIED: '已核销',
  REFUNDED: '已退订',
  RESCHEDULED: '已改签',
  CANCELLED: '已取消'
}

// 支付状态展示名
export const PAY_STATUS_LABELS = {
  UNPAID: '未支付',
  PROCESSING: '支付中',
  PAID: '已支付',
  REFUNDED: '已退款'
}

// 舱级选项（下拉用）
export const CABIN_OPTIONS = [
  { value: 'ECONOMY_CLASS', label: '经济舱' },
  { value: 'BUSINESS_CLASS', label: '商务舱' },
  { value: 'FIRST_CLASS', label: '头等舱' }
]
