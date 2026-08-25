<script setup>
import { reactive, ref, onMounted } from 'vue'
import { flightApi } from '../api/flight'
import BaseInput from '../components/BaseInput.vue'
import BaseButton from '../components/BaseButton.vue'
import BaseCard from '../components/BaseCard.vue'
import Skeleton from '../components/Skeleton.vue'
import EmptyState from '../components/EmptyState.vue'
import { formatDate, formatTime, yuan } from '../utils/format'

const filters = reactive({ depCity: '', arrCity: '', depDate: '', priceMin: '', priceMax: '' })
const flights = ref([])
const total = ref(0)
const loading = ref(false)
const searched = ref(false)

async function search() {
  loading.value = true
  try {
    const params = {}
    if (filters.depCity) params.depCity = filters.depCity
    if (filters.arrCity) params.arrCity = filters.arrCity
    if (filters.depDate) params.depDate = filters.depDate
    if (filters.priceMin) params.priceMin = filters.priceMin
    if (filters.priceMax) params.priceMax = filters.priceMax
    params.size = 50
    const data = await flightApi.search(params)
    flights.value = data.data || []
    total.value = data.total
    searched.value = true
  } finally {
    loading.value = false
  }
}

onMounted(search)
</script>

<template>
  <div>
    <h1 class="page-title">搜航班</h1>
    <p class="page-subtitle">选择出发地与目的地，发现您的旅程</p>

    <BaseCard class="filter-card">
      <div class="filter-grid">
        <BaseInput v-model="filters.depCity" placeholder="出发地，如 北京" />
        <BaseInput v-model="filters.arrCity" placeholder="到达地，如 上海" />
        <input v-model="filters.depDate" type="date" class="date-input" />
        <BaseInput v-model="filters.priceMin" placeholder="最低价" type="number" />
        <BaseInput v-model="filters.priceMax" placeholder="最高价" type="number" />
        <BaseButton @click="search">搜索</BaseButton>
      </div>
    </BaseCard>

    <div v-if="loading" class="results">
      <Skeleton :rows="4" />
    </div>

    <div v-else-if="flights.length" class="results">
      <p class="muted count">共 {{ total }} 个航班</p>
      <div class="col">
        <BaseCard
          v-for="f in flights"
          :key="f.id"
          hoverable
          clickable
          class="flight-card"
          @click="$router.push(`/flight/${f.id}`)"
        >
          <div class="flight-row">
            <div class="flight-code">{{ f.code }}</div>
            <div class="flight-time">
              <span class="big">{{ formatTime(f.datetimeDep) }}</span>
              <span class="arrow">→</span>
              <span class="big">{{ formatTime(f.datetimeArr) }}</span>
            </div>
            <div class="flight-route muted">
              {{ f.regionDep }} · {{ f.regionArr }}　{{ formatDate(f.datetimeDep) }}
            </div>
            <div class="flight-price">
              <span class="price">{{ yuan(f.price) }}</span>
              <span class="muted">起</span>
            </div>
          </div>
          <div class="flight-cabins">
            <span class="cabin-chip">经济 {{ yuan(f.price) }}</span>
            <span class="cabin-chip">商务 {{ yuan(f.priceBusinessClass) }}</span>
            <span class="cabin-chip">头等 {{ yuan(f.priceFirstClass) }}</span>
          </div>
        </BaseCard>
      </div>
    </div>

    <EmptyState
      v-else-if="searched"
      title="没有找到航班"
      description="换个条件试试"
      emoji="🔎"
    />
  </div>
</template>

<style scoped>
.filter-card { margin-bottom: var(--space-6); }
.filter-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr 1fr auto;
  gap: var(--space-3);
  align-items: end;
}
.date-input {
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface-2);
  font-family: inherit;
  font-size: var(--text-base);
}
.results { display: flex; flex-direction: column; gap: var(--space-3); }
.count { margin-bottom: var(--space-2); }
.flight-card { cursor: pointer; }
.flight-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  flex-wrap: wrap;
}
.flight-code { font-weight: 700; font-size: var(--text-lg); }
.flight-time { display: flex; align-items: center; gap: var(--space-3); }
.big { font-size: var(--text-xl); font-weight: 600; letter-spacing: -0.01em; }
.arrow { color: var(--color-text-tertiary); }
.flight-route { font-size: var(--text-sm); }
.flight-price { text-align: right; }
.price { font-size: var(--text-xl); font-weight: 700; color: var(--color-accent); }
.flight-cabins {
  display: flex;
  gap: var(--space-2);
  margin-top: var(--space-4);
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border);
}
.cabin-chip {
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  background: var(--color-surface-2);
  border-radius: var(--radius-full);
  padding: 4px 12px;
}
@media (max-width: 640px) {
  .filter-grid { grid-template-columns: 1fr 1fr; }
  .flight-row { flex-direction: column; align-items: flex-start; }
}
</style>
