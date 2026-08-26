<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { flightApi } from '../api/flight'
import BaseCard from '../components/BaseCard.vue'
import BaseButton from '../components/BaseButton.vue'
import Skeleton from '../components/Skeleton.vue'
import { useAuthStore } from '../stores/auth'
import { formatDate, formatTime, yuan, CABIN_LABELS } from '../utils/format'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const flight = ref(null)
const loading = ref(true)
const error = ref('')

const cabins = computed(() => {
  if (!flight.value) return []
  return [
    { key: 'ECONOMY_CLASS', label: '经济舱', price: flight.value.price, seats: flight.value.seatEconomyClass },
    { key: 'BUSINESS_CLASS', label: '商务舱', price: flight.value.priceBusinessClass, seats: flight.value.seatBusinessClass },
    { key: 'FIRST_CLASS', label: '头等舱', price: flight.value.priceFirstClass, seats: flight.value.seatFirstClass }
  ]
})

onMounted(async () => {
  try {
    flight.value = await flightApi.detail(route.params.id)
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
})

function goBook() {
  router.push(`/booking/${flight.value.id}`)
}
</script>

<template>
  <div v-if="loading"><Skeleton card :rows="5" /></div>
  <div v-else-if="error" class="error-state">
    <p>{{ error }}</p>
    <BaseButton variant="ghost" @click="$router.push('/')">返回搜索</BaseButton>
  </div>
  <div v-else-if="flight">
    <RouterLink to="/" class="back">← 返回搜索</RouterLink>
    <h1 class="page-title">{{ flight.code }}</h1>
    <p class="page-subtitle">{{ flight.regionDep }} → {{ flight.regionArr }} · {{ formatDate(flight.datetimeDep) }}</p>

    <BaseCard class="detail-card">
      <div class="time-line">
        <div>
          <div class="time-big">{{ formatTime(flight.datetimeDep) }}</div>
          <div class="muted">{{ flight.regionDep }}</div>
        </div>
        <div class="time-dash">
          <span class="muted">飞行 {{ flight.distance }} km</span>
        </div>
        <div>
          <div class="time-big">{{ formatTime(flight.datetimeArr) }}</div>
          <div class="muted">{{ flight.regionArr }}</div>
        </div>
      </div>
      <div class="info-grid muted">
        <span>登机口：{{ flight.gate || '--' }}</span>
        <span>状态：{{ flight.status }}</span>
      </div>
    </BaseCard>

    <h2 class="section-title">选择舱级</h2>
    <div class="cabin-list">
      <BaseCard v-for="c in cabins" :key="c.key" hoverable class="cabin-card">
        <div class="row-between">
          <div>
            <div class="cabin-name">{{ c.label }}</div>
            <div class="muted">{{ c.seats }} 张余票</div>
          </div>
          <div class="cabin-price">{{ yuan(c.price) }}</div>
        </div>
      </BaseCard>
    </div>

    <div class="cta">
      <BaseButton variant="ghost" size="lg" block class="track-btn" @click="router.push(`/tracking/${flight.id}`)">实时轨迹</BaseButton>
      <BaseButton size="lg" block @click="goBook">选舱下单</BaseButton>
    </div>
  </div>
</template>

<style scoped>
.back { display: inline-block; margin-bottom: var(--space-3); font-size: var(--text-sm); }
.detail-card { margin-bottom: var(--space-6); }
.time-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-5);
  padding: var(--space-4) 0;
}
.time-big { font-size: var(--text-2xl); font-weight: 700; letter-spacing: -0.02em; }
.time-dash { flex: 1; text-align: center; border-bottom: 2px dashed var(--color-border); padding-bottom: var(--space-2); }
.info-grid { display: flex; gap: var(--space-6); padding-top: var(--space-4); border-top: 1px solid var(--color-border); }
.section-title { font-size: var(--text-xl); margin-bottom: var(--space-4); }
.cabin-list { display: flex; flex-direction: column; gap: var(--space-3); margin-bottom: var(--space-6); }
.cabin-name { font-size: var(--text-lg); font-weight: 600; }
.cabin-price { font-size: var(--text-xl); font-weight: 700; color: var(--color-accent); }
.cta { max-width: 320px; margin: 0 auto; display: flex; flex-direction: column; gap: var(--space-2); }
.error-state { text-align: center; padding: var(--space-8); }
</style>
