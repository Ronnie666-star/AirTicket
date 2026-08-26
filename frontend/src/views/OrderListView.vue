<script setup>
import { reactive, ref, onMounted } from 'vue'
import { orderApi } from '../api/order'
import BaseCard from '../components/BaseCard.vue'
import BaseSelect from '../components/BaseSelect.vue'
import BaseButton from '../components/BaseButton.vue'
import Skeleton from '../components/Skeleton.vue'
import EmptyState from '../components/EmptyState.vue'
import { formatDateTime, yuan, CABIN_LABELS, ORDER_STATUS_LABELS } from '../utils/format'

const filters = reactive({ orderStatus: '', payStatus: '' })
const orders = ref([])
const loading = ref(false)

const ORDER_STATUS_OPTIONS = [
  { value: '', label: '全部' },
  { value: 'PENDING_TICKET_ISSUANCE', label: '待出票' },
  { value: 'ISSUED_TICKET', label: '已出票' },
  { value: 'VERIFIED', label: '已核销' },
  { value: 'REFUNDED', label: '已退订' },
  { value: 'RESCHEDULED', label: '已改签' },
  { value: 'CANCELLED', label: '已取消' }
]
const PAY_STATUS_OPTIONS = [
  { value: '', label: '全部' },
  { value: 'UNPAID', label: '未支付' },
  { value: 'PROCESSING', label: '支付中' },
  { value: 'PAID', label: '已支付' },
  { value: 'REFUNDED', label: '已退款' }
]

function statusClass(status) {
  const map = {
    PENDING_TICKET_ISSUANCE: 'warn',
    ISSUED_TICKET: 'ok',
    VERIFIED: 'ok',
    REFUNDED: 'muted',
    RESCHEDULED: 'info',
    CANCELLED: 'muted'
  }
  return map[status] || ''
}

async function load() {
  loading.value = true
  try {
    const params = { size: 50 }
    if (filters.orderStatus) params.orderStatus = filters.orderStatus
    if (filters.payStatus) params.payStatus = filters.payStatus
    const data = await orderApi.list(params)
    orders.value = data.data || []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <h1 class="page-title">我的订单</h1>
    <p class="page-subtitle">查看与管理您的全部订单</p>

    <div class="filters row">
      <BaseSelect v-model="filters.orderStatus" placeholder="订单状态" :options="ORDER_STATUS_OPTIONS" />
      <BaseSelect v-model="filters.payStatus" placeholder="支付状态" :options="PAY_STATUS_OPTIONS" />
      <BaseButton variant="secondary" @click="load">筛选</BaseButton>
    </div>

    <div v-if="loading" class="mt"><Skeleton :rows="3" /></div>
    <div v-else-if="orders.length" class="list col">
      <BaseCard
        v-for="o in orders"
        :key="o.id"
        hoverable
        clickable
        @click="$router.push(`/orders/${o.id}`)"
      >
        <div class="row-between">
          <div>
            <div class="row o-head">
              <span class="o-code">{{ o.flightCode }}</span>
              <span class="cabin">{{ CABIN_LABELS[o.cabinClass] || o.cabinClass }}</span>
            </div>
            <div class="muted">{{ o.regionDep }} → {{ o.regionArr }} · {{ o.airlineName }}</div>
          </div>
          <div class="o-right">
            <div class="o-status" :class="statusClass(o.orderStatus)">{{ ORDER_STATUS_LABELS[o.orderStatus] || o.orderStatus }}</div>
            <div class="o-price">{{ yuan(o.totalPrice) }}</div>
          </div>
        </div>
        <div class="muted o-meta">{{ o.code }} · {{ formatDateTime(o.createAt) }}</div>
      </BaseCard>
    </div>
    <EmptyState v-else title="暂无订单" description="去搜航班下单吧" emoji="📭" />
  </div>
</template>

<style scoped>
.filters { margin-bottom: var(--space-5); }
.filters :deep(.field) { min-width: 150px; }
.mt { margin-top: var(--space-4); }
.list { gap: var(--space-3); }
.o-head { gap: var(--space-2); margin-bottom: var(--space-1); }
.o-code { font-weight: 700; font-size: var(--text-lg); }
.cabin {
  font-size: var(--text-xs);
  color: var(--color-accent);
  background: rgba(0, 113, 227, 0.08);
  padding: 2px 10px;
  border-radius: var(--radius-full);
}
.o-right { text-align: right; }
.o-status { font-size: var(--text-sm); margin-bottom: var(--space-1); }
.o-status.ok { color: var(--color-success); }
.o-status.warn { color: var(--color-warning); }
.o-status.info { color: var(--color-accent); }
.o-status.muted { color: var(--color-text-tertiary); }
.o-price { font-weight: 700; font-size: var(--text-lg); }
.o-meta { margin-top: var(--space-3); font-size: var(--text-xs); }
</style>
