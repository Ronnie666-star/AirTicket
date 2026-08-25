<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { flightApi } from '../api/flight'
import { orderApi } from '../api/order'
import BaseCard from '../components/BaseCard.vue'
import BaseButton from '../components/BaseButton.vue'
import Skeleton from '../components/Skeleton.vue'
import { toast } from '../composables/useToast'
import { formatDate, formatTime, yuan, CABIN_LABELS } from '../utils/format'

const route = useRoute()
const router = useRouter()
const flight = ref(null)
const loading = ref(true)
const selected = ref('ECONOMY_CLASS')
const submitting = ref(false)
const remark = ref('')

const cabins = computed(() => {
  if (!flight.value) return []
  return [
    { key: 'ECONOMY_CLASS', label: '经济舱', price: flight.value.price, seats: flight.value.seatEconomyClass },
    { key: 'BUSINESS_CLASS', label: '商务舱', price: flight.value.priceBusinessClass, seats: flight.value.seatBusinessClass },
    { key: 'FIRST_CLASS', label: '头等舱', price: flight.value.priceFirstClass, seats: flight.value.seatFirstClass }
  ]
})

const selectedCabin = computed(() => cabins.value.find((c) => c.key === selected.value))
const totalPrice = computed(() => selectedCabin.value?.price ?? 0)

onMounted(async () => {
  try {
    flight.value = await flightApi.detail(route.params.id)
  } finally {
    loading.value = false
  }
})

async function submit() {
  if (!selectedCabin.value || selectedCabin.value.seats <= 0) {
    toast.error('该舱级余票不足，请选择其他舱级')
    return
  }
  submitting.value = true
  try {
    const order = await orderApi.book({
      flightId: flight.value.id,
      cabinClass: selected.value,
      remark: remark.value || null
    })
    toast.success('下单成功')
    router.push(`/payment/${order.id}`)
  } catch (e) {
    toast.error(e.message)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div v-if="loading"><Skeleton card :rows="5" /></div>
  <div v-else-if="flight">
    <RouterLink :to="`/flight/${flight.id}`" class="back">← 返回航班详情</RouterLink>
    <h1 class="page-title">选择舱级下单</h1>
    <p class="page-subtitle">
      {{ flight.code }} · {{ flight.regionDep }} → {{ flight.regionArr }} · {{ formatDate(flight.datetimeDep) }} {{ formatTime(flight.datetimeDep) }}
    </p>

    <BaseCard v-for="c in cabins" :key="c.key"
      class="cabin-option"
      :class="{ selected: selected === c.key }"
      clickable
      @click="selected = c.key"
    >
      <div class="row-between">
        <div class="row">
          <span class="radio" :class="{ on: selected === c.key }" />
          <div>
            <div class="cabin-name">{{ c.label }}</div>
            <div class="muted">{{ c.seats }} 张余票{{ c.seats <= 0 ? '（已售罄）' : '' }}</div>
          </div>
        </div>
        <div class="cabin-price">{{ yuan(c.price) }}</div>
      </div>
    </BaseCard>

    <BaseCard class="summary">
      <div class="row-between">
        <span class="muted">订单总价（{{ CABIN_LABELS[selected] }}）</span>
        <span class="total">{{ yuan(totalPrice) }}</span>
      </div>
      <input v-model="remark" class="remark" placeholder="备注（选填）" />
    </BaseCard>

    <div class="cta">
      <BaseButton size="lg" block :loading="submitting" @click="submit">确认下单</BaseButton>
    </div>
  </div>
</template>

<style scoped>
.back { display: inline-block; margin-bottom: var(--space-3); font-size: var(--text-sm); }
.cabin-option { margin-bottom: var(--space-3); cursor: pointer; transition: var(--transition-fast); }
.cabin-option.selected { border-color: var(--color-accent); box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.15); }
.radio {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid var(--color-border);
  transition: var(--transition-fast);
}
.radio.on { border-color: var(--color-accent); background: radial-gradient(circle, var(--color-accent) 45%, transparent 50%); }
.cabin-name { font-size: var(--text-lg); font-weight: 600; }
.cabin-price { font-size: var(--text-xl); font-weight: 700; color: var(--color-accent); }
.summary { margin-top: var(--space-4); margin-bottom: var(--space-6); }
.total { font-size: var(--text-2xl); font-weight: 700; color: var(--color-accent); }
.remark {
  margin-top: var(--space-4);
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface-2);
  font-family: inherit;
}
.cta { max-width: 320px; margin: 0 auto; }
</style>
