import { reactive } from 'vue'

// 全局轻提示：任何地方 import { toast } 即可弹出。
// 用法：toast.success('成功') / toast.error('失败') / toast.info('提示')
let seed = 0
export const toasts = reactive([])

function push(message, type) {
  const id = ++seed
  toasts.push({ id, message, type })
  setTimeout(() => remove(id), 3000)
}

function remove(id) {
  const idx = toasts.findIndex((t) => t.id === id)
  if (idx >= 0) toasts.splice(idx, 1)
}

export const toast = {
  success: (m) => push(m, 'success'),
  error: (m) => push(m, 'error'),
  info: (m) => push(m, 'info')
}
