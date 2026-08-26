<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '../api/order'
import { flightApi } from '../api/flight'
import BaseCard from '../components/BaseCard.vue'
import BaseButton from '../components/BaseButton.vue'
import BaseModal from '../components/BaseModal.vue'
import Skeleton from '../components/Skeleton.vue'
import { toast } from '../composables/useToast'
import { useAuthStore } from '../stores/auth'
import { formatDateTime, formatTime, yuan, CABIN_LABELS, ORDER_STATUS_LABELS, PAY_STATUS_LABELS } from '../utils/format'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const order = ref(null)
const flight = ref(null)
const loading = ref(true)
const acting = ref(false)

// 改签
const reschedOpen = ref(false)
const candidates = ref([])
const selectedFlight = ref(null)
const reschedLoading = ref(false)

const cabinLabel = computed(() => CABIN_LABELS[order.value?.cabinClass] || order.value?.cabinClass)
const oldCabinPrice = computed(() => {
  if (!flight.value || !order.value) return 0
  const c = order.value.cabinClass
  return c === 'FIRST_CLASS' ? flight.value.priceFirstClass
    : c === 'BUSINESS_CLASS' ? flight.value.priceBusinessClass
    : flight.value.price
})
const diff = computed(() => {
  if (!selectedFlight.value) return null
  const c = order.value.cabinClass
  const newPrice = c === 'FIRST_CLASS' ? selectedFlight.value.priceFirstClass
    : c === 'BUSINESS_CLASS' ? selectedFlight.value.priceBusinessClass
    : selectedFlight.value.price
  return newPrice - oldCabinPrice.value
})

async function load() {
  try {
    order.value = await orderApi.detail(route.params.id)
    flight.value = await flightApi.detail(order.value.flightId)
  } catch (e) {
    toast.error(e.message)
    router.replace('/orders')
  } finally {
    loading.value = false
  }
}
onMounted(load)

function canPay() { return order.value.payStatus === 'UNPAID' && order.value.orderStatus === 'PENDING_TICKET_ISSUANCE' }
function canCancel() { return order.value.payStatus === 'UNPAID' && order.value.orderStatus === 'PENDING_TICKET_ISSUANCE' }
function canRefund() { return order.value.payStatus === 'PAID' }
function canVerify() { return order.value.orderStatus === 'ISSUED_TICKET' && auth.canManageFlights }
function canResched() {
  if (order.value.orderStatus === 'ISSUED_TICKET') return true   // 旅客可自助改签已出票订单
  // 航班取消后退款的订单可改签（未支付的取消单无退款可退，不可改签）
  return order.value.orderStatus === 'CANCELLED' && order.value.payStatus === 'REFUNDED' && flight.value?.status === 'CANCELLED'
}

async function goPay() { router.push(`/payment/${order.value.id}`) }

async function cancel() {
  if (!confirm('确认取消该订单？未支付取消不退款。')) return
  acting.value = true
  try {
    const updated = await orderApi.cancel(order.value.id)
    order.value = updated
    toast.success('订单已取消')
  } catch (e) {
    toast.error(e.message)
  } finally {
    acting.value = false
  }
}

async function refund() {
  if (!confirm('确认退订该订单？将按总价扣退票费退款。')) return
  acting.value = true
  try {
    const updated = await orderApi.cancel(order.value.id)
    order.value = updated
    toast.success(`退订成功，退款 ${yuan(updated.refundAmount)}`)
  } catch (e) {
    toast.error(e.message)
  } finally {
    acting.value = false
  }
}

async function verify() {
  acting.value = true
  try {
    order.value = await orderApi.verify(order.value.id)
    toast.success('已核销')
  } catch (e) {
    toast.error(e.message)
  } finally {
    acting.value = false
  }
}

// 改签：按旧航班起落地区搜同航线候选（后端校验未起飞/未取消/该舱余票/同航线），只列该舱还有余票的
async function openResched() {
  reschedOpen.value = true
  selectedFlight.value = null
  candidates.value = []
  reschedLoading.value = true
  try {
    const data = await flightApi.search({ depCity: flight.value.regionDep, arrCity: flight.value.regionArr, hideExpired: true, size: 50 })
    const c = order.value.cabinClass
    const seatKey = c === 'FIRST_CLASS' ? 'seatFirstClass' : c === 'BUSINESS_CLASS' ? 'seatBusinessClass' : 'seatEconomyClass'
    candidates.value = (data.data || []).filter((f) => f.id !== flight.value.id && f[seatKey] > 0)
  } catch (e) {
    toast.error(e.message)
  } finally {
    reschedLoading.value = false
  }
}

async function submitResched() {
  if (!selectedFlight.value) {
    toast.error('请选择改签航班')
    return
  }
  acting.value = true
  try {
    const updated = await orderApi.reschedule(order.value.id, selectedFlight.value.id)
    order.value = updated
    reschedOpen.value = false
    toast.success('改签成功')
    await load()
  } catch (e) {
    toast.error(e.message)
  } finally {
    acting.value = false
  }
}
</script>

<template>
  <div v-if="loading"><Skeleton card :rows="5" /></div>
  <div v-else-if="order && flight">
    <RouterLink to="/orders" class="back">← 返回订单列表</RouterLink>
    <h1 class="page-title">订单详情</h1>

    <BaseCard class="detail">
      <div class="row-between head">
        <div>
          <div class="flight-code">{{ flight.code }}</div>
          <div class="muted">{{ flight.regionDep }} → {{ flight.regionArr }} · {{ flight.airlineName || '' }}</div>
        </div>
        <div class="status-badge" :class="order.payStatus">{{ PAY_STATUS_LABELS[order.payStatus] || order.payStatus }}</div>
      </div>

      <div class="kv">
        <div class="kv-row"><span class="muted">订单号</span><span>{{ order.code }}</span></div>
        <div class="kv-row"><span class="muted">订单状态</span><span>{{ ORDER_STATUS_LABELS[order.orderStatus] || order.orderStatus }}</span></div>
        <div class="kv-row"><span class="muted">舱级</span><span>{{ cabinLabel }}</span></div>
        <div class="kv-row"><span class="muted">出发</span><span>{{ formatDateTime(flight.datetimeDep) }}</span></div>
        <div class="kv-row"><span class="muted">到达</span><span>{{ formatDateTime(flight.datetimeArr) }}</span></div>
        <div class="kv-row"><span class="muted">总价</span><span class="price">{{ yuan(order.totalPrice) }}</span></div>
        <div v-if="order.refundAmount != null" class="kv-row"><span class="muted">退款金额</span><span class="ok">{{ yuan(order.refundAmount) }}</span></div>
        <div v-if="order.adjustAmount != null" class="kv-row">
          <span class="muted">改签差价</span>
          <span :class="order.adjustAmount >= 0 ? 'danger' : 'ok'">
            {{ order.adjustAmount >= 0 ? '补差 ' : '应退 ' }}{{ yuan(Math.abs(order.adjustAmount)) }}
          </span>
        </div>
        <div class="kv-row"><span class="muted">下单时间</span><span>{{ formatDateTime(order.createAt) }}</span></div>
        <div v-if="order.payTime" class="kv-row"><span class="muted">支付时间</span><span>{{ formatDateTime(order.payTime) }}</span></div>
        <div v-if="order.issueTime" class="kv-row"><span class="muted">出票时间</span><span>{{ formatDateTime(order.issueTime) }}</span></div>
        <div v-if="order.cancelTime" class="kv-row"><span class="muted">取消/退订时间</span><span>{{ formatDateTime(order.cancelTime) }}</span></div>
      </div>
    </BaseCard>

    <div v-if="order.payStatus === 'PROCESSING'" class="tip">
      ⏳ 订单支付处理中，可前往支付页查询状态或重新确认
      <BaseButton variant="secondary" class="mt" @click="goPay">前往支付</BaseButton>
    </div>

    <div v-if="flight.status === 'CANCELLED'" class="tip cancelled-tip">
      ⚠️ 该航班因天气等原因已取消并全额退款，可点击下方"改签"重新选择同航线航班
    </div>

    <div class="actions row">
      <BaseButton v-if="canPay()" @click="goPay">去支付</BaseButton>
      <BaseButton v-if="canCancel()" variant="secondary" :disabled="acting" @click="cancel">取消订单</BaseButton>
      <BaseButton v-if="canRefund()" variant="secondary" :disabled="acting" @click="refund">退订退款</BaseButton>
      <BaseButton v-if="canVerify()" variant="danger" :disabled="acting" @click="verify">核销</BaseButton>
      <BaseButton v-if="canResched()" variant="secondary" :disabled="acting" @click="openResched">改签</BaseButton>
    </div>

    <BaseModal :open="reschedOpen" title="选择改签航班" @close="reschedOpen = false">
      <p class="muted">
        仅列出同航线、未起飞且该舱还有余票的航班。改签差价 = 新航班同舱票价 − 旧航班同舱票价（正=补差、负=应退）
      </p>
      <div v-if="reschedLoading" class="mt"><Skeleton :rows="3" /></div>
      <div v-else class="cand-list">
        <div
          v-for="f in candidates"
          :key="f.id"
          class="cand"
          :class="{ on: selectedFlight?.id === f.id }"
          @click="selectedFlight = f"
        >
          <div class="row-between">
            <div>
              <div class="cand-code">{{ f.code }} · {{ f.regionDep }} → {{ f.regionArr }}</div>
              <div class="muted">{{ formatTime(f.datetimeDep) }} 起飞 · {{ yuan(f.price) }} 起</div>
            </div>
            <div v-if="selectedFlight?.id === f.id" class="cand-diff">
              {{ diff >= 0 ? '+' : '' }}{{ yuan(diff) }}
            </div>
          </div>
        </div>
        <p v-if="!candidates.length" class="muted">没有可改签的同航线航班</p>
      </div>
      <template #footer>
        <div class="row" style="justify-content: flex-end">
          <BaseButton variant="ghost" @click="reschedOpen = false">取消</BaseButton>
          <BaseButton :disabled="!selectedFlight || acting" @click="submitResched">确认改签</BaseButton>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<style scoped>
.back { display: inline-block; margin-bottom: var(--space-3); font-size: var(--text-sm); }
.detail { margin-bottom: var(--space-5); }
.head { margin-bottom: var(--space-5); }
.flight-code { font-size: var(--text-xl); font-weight: 700; }
.status-badge {
  padding: 4px 14px;
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  background: var(--color-surface-2);
  color: var(--color-text-secondary);
}
.status-badge.UNPAID { color: var(--color-warning); background: rgba(255, 149, 0, 0.1); }
.status-badge.PROCESSING { color: var(--color-accent); background: rgba(0, 113, 227, 0.1); }
.status-badge.PAID { color: var(--color-success); background: rgba(52, 199, 89, 0.12); }
.status-badge.REFUNDED { color: var(--color-text-tertiary); }
.kv { display: flex; flex-direction: column; gap: var(--space-2); }
.kv-row { display: flex; justify-content: space-between; padding: var(--space-2) 0; border-bottom: 1px solid var(--color-border); font-size: var(--text-sm); }
.kv-row:last-child { border-bottom: none; }
.price { font-weight: 700; font-size: var(--text-lg); }
.ok { color: var(--color-success); }
.danger { color: var(--color-danger); }
.tip {
  background: rgba(0, 113, 227, 0.06);
  border-radius: var(--radius-sm);
  padding: var(--space-4);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-4);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: var(--space-2);
}
.cancelled-tip {
  background: rgba(255, 59, 48, 0.08);
  color: var(--color-danger);
}
.actions { flex-wrap: wrap; }
.cand-list { display: flex; flex-direction: column; gap: var(--space-2); margin-top: var(--space-3); max-height: 320px; overflow-y: auto; }
.cand {
  padding: var(--space-3);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: var(--transition-fast);
}
.cand:hover { border-color: var(--color-text-tertiary); }
.cand.on { border-color: var(--color-accent); box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.12); }
.cand-code { font-weight: 600; }
.cand-diff { font-weight: 700; color: var(--color-accent); }
</style>
