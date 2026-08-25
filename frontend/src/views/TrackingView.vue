<script setup>
import { ref } from 'vue'
import { routeApi } from '../api/misc'
import { flightApi } from '../api/flight'
import BaseCard from '../components/BaseCard.vue'
import BaseInput from '../components/BaseInput.vue'
import BaseButton from '../components/BaseButton.vue'
import EmptyState from '../components/EmptyState.vue'
import Skeleton from '../components/Skeleton.vue'
import { toast } from '../composables/useToast'
import { formatDateTime } from '../utils/format'

const flightId = ref('')
const result = ref(null)
const flight = ref(null)
const loading = ref(false)
const queried = ref(false)

async function query() {
  if (!flightId.value) {
    toast.error('请输入航班 ID')
    return
  }
  loading.value = true
  result.value = null
  flight.value = null
  try {
    // 先拿航班信息用于展示
    flight.value = await flightApi.detail(Number(flightId.value))
    result.value = await routeApi.get(Number(flightId.value))
  } catch (e) {
    if (e.message) toast.error(e.message)
  } finally {
    loading.value = false
    queried.value = true
  }
}
</script>

<template>
  <div>
    <h1 class="page-title">实时轨迹</h1>
    <p class="page-subtitle">按航班查询飞行中的实时位置与状态</p>

    <BaseCard class="q-card">
      <div class="row">
        <BaseInput v-model="flightId" label="航班 ID" type="number" placeholder="输入航班 ID" />
        <BaseButton @click="query">查询</BaseButton>
      </div>
    </BaseCard>

    <div v-if="loading" class="mt"><Skeleton card :rows="4" /></div>

    <div v-else-if="result" class="mt">
      <h2 class="s-title">{{ flight.code }} · 实时轨迹</h2>
      <div class="grid grid-2">
        <BaseCard class="stat"><div class="stat-label muted">剩余距离</div><div class="stat-val">{{ result.distanceRemain }} km</div></BaseCard>
        <BaseCard class="stat"><div class="stat-label muted">剩余时间</div><div class="stat-val">{{ result.timeRemain }} 分钟</div></BaseCard>
        <BaseCard class="stat"><div class="stat-label muted">高度</div><div class="stat-val">{{ result.altitude }} m</div></BaseCard>
        <BaseCard class="stat"><div class="stat-label muted">速度</div><div class="stat-val">{{ result.speed }} km/h</div></BaseCard>
        <BaseCard class="stat"><div class="stat-label muted">纬度</div><div class="stat-val">{{ result.latitude }}</div></BaseCard>
        <BaseCard class="stat"><div class="stat-label muted">经度</div><div class="stat-val">{{ result.longitude }}</div></BaseCard>
      </div>
      <p class="muted time">采集时间：{{ formatDateTime(result.timeStamp) }}</p>
    </div>

    <EmptyState v-else-if="queried" title="该航班暂无轨迹" description="航班可能未起飞或没有轨迹记录" emoji="🛰️" />
  </div>
</template>

<style scoped>
.q-card { margin-bottom: var(--space-5); }
.q-card .row { align-items: flex-end; gap: var(--space-3); }
.q-card :deep(.field) { flex: 1; }
.mt { margin-top: var(--space-4); }
.s-title { font-size: var(--text-xl); margin-bottom: var(--space-4); }
.stat { text-align: center; padding: var(--space-5); }
.stat-label { font-size: var(--text-sm); margin-bottom: var(--space-2); }
.stat-val { font-size: var(--text-xl); font-weight: 700; }
.time { margin-top: var(--space-4); text-align: center; }
</style>
