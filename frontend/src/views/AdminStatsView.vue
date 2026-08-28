<script setup>
// 数据统计（管理员）：3 个统计功能
//   ① 营收总览 + 渠道营收占比（KPI + 占比条）
//   ② 热门航班销量 Top10（销量 / 成交金额 / 座舱利用率）
//   ③ 旅客消费排行 Top10（单量 / 成交金额）
// 图表为纯 CSS 条形，无第三方依赖，Apple 风格（设计令牌）。
import { ref, computed, onMounted } from 'vue'
import { statsApi } from '../api/stats'
import BaseCard from '../components/BaseCard.vue'
import Skeleton from '../components/Skeleton.vue'
import EmptyState from '../components/EmptyState.vue'
import { toast } from '../composables/useToast'
import { yuan, formatDate } from '../utils/format'

const loading = ref(true)
const revenue = ref(null)         // 营收总览（单行）
const channels = ref([])          // 渠道占比
const flights = ref([])           // 热门航班销量
const passengers = ref([])        // 旅客排行

async function load() {
  loading.value = true
  try {
    const [rev, ch, fs, tp] = await Promise.all([
      statsApi.revenue(),
      statsApi.channels(),
      statsApi.flightSales(10),
      statsApi.topPassengers(10)
    ])
    revenue.value = rev
    channels.value = ch || []
    flights.value = fs || []
    passengers.value = tp || []
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}
onMounted(load)

// 渠道占比：按成交金额归一化成百分比
const channelMax = computed(() => Math.max(1, ...channels.value.map((c) => Number(c.revenue))))
const channelPercent = (c) => Math.round((Number(c.revenue) / channelMax.value) * 100)

// 航班销量：按订单数归一化；利用率 = 订单数 / 座舱容量
const flightMax = computed(() => Math.max(1, ...flights.value.map((f) => f.orderCount)))
const flightPercent = (f) => Math.round((f.orderCount / flightMax.value) * 100)
const utilization = (f) => (f.capacity ? Math.round((f.orderCount / f.capacity) * 100) : 0)

// 旅客排行：按成交金额归一化
const passengerMax = computed(() => Math.max(1, ...passengers.value.map((p) => Number(p.totalSpend))))
const passengerPercent = (p) => Math.round((Number(p.totalSpend) / passengerMax.value) * 100)

// 营收 KPI 卡片
const kpis = computed(() => {
  const r = revenue.value || {}
  return [
    { label: '成交总额', value: yuan(r.totalRevenue) },
    { label: '实收营收', value: yuan(r.collectedRevenue) },
    { label: '退款总额', value: yuan(r.refundAmount) },
    { label: '退票费收入', value: yuan(r.cancellationFeeIncome) },
    { label: '已支付订单', value: String(r.paidOrderCount ?? '--') },
    { label: '订单总数', value: String(r.totalOrderCount ?? '--') }
  ]
})
</script>

<template>
  <div>
    <h1 class="page-title">数据统计</h1>
    <p class="page-subtitle">售票 / 营收 / 旅客 3 个统计功能（仅管理员）</p>

    <div v-if="loading"><Skeleton :rows="6" /></div>

    <template v-else>
      <!-- 统计② 营收总览 -->
      <h2 class="section-title">营收总览</h2>
      <div class="grid grid-3 kpi-grid">
        <BaseCard v-for="k in kpis" :key="k.label" class="kpi">
          <div class="kpi-value">{{ k.value }}</div>
          <div class="kpi-label">{{ k.label }}</div>
        </BaseCard>
      </div>

      <!-- 统计② 渠道营收占比 -->
      <h2 class="section-title">渠道营收占比</h2>
      <BaseCard v-if="channels.length">
        <div class="bar-row" v-for="c in channels" :key="c.channelId">
          <span class="bar-label">{{ c.channelName }}</span>
          <div class="bar-track">
            <div class="bar-fill" :style="{ width: channelPercent(c) + '%' }"></div>
          </div>
          <span class="bar-value">{{ yuan(c.revenue) }} · {{ c.orderCount }}单</span>
        </div>
      </BaseCard>
      <EmptyState v-else title="暂无渠道数据" emoji="📊" />

      <!-- 统计① 热门航班销量 Top10 -->
      <h2 class="section-title">热门航班销量 Top10</h2>
      <BaseCard v-if="flights.length">
        <div class="bar-row" v-for="f in flights" :key="f.flightId">
          <span class="bar-label">{{ f.code }}<span class="bar-sub">{{ f.regionDep }}→{{ f.regionArr }} {{ formatDate(f.datetimeDep) }}</span></span>
          <div class="bar-track">
            <div class="bar-fill accent" :style="{ width: flightPercent(f) + '%' }"></div>
          </div>
          <span class="bar-value">{{ f.orderCount }}单 · {{ yuan(f.revenue) }} · 利用率{{ utilization(f) }}%</span>
        </div>
      </BaseCard>
      <EmptyState v-else title="暂无航班销量数据" emoji="✈️" />

      <!-- 统计③ 旅客消费排行 Top10 -->
      <h2 class="section-title">旅客消费排行 Top10</h2>
      <BaseCard v-if="passengers.length">
        <div class="bar-row" v-for="p in passengers" :key="p.userId">
          <span class="bar-label">{{ p.realName || p.username }}<span class="bar-sub">{{ p.username }}</span></span>
          <div class="bar-track">
            <div class="bar-fill green" :style="{ width: passengerPercent(p) + '%' }"></div>
          </div>
          <span class="bar-value">{{ p.orderCount }}单 · {{ yuan(p.totalSpend) }}</span>
        </div>
      </BaseCard>
      <EmptyState v-else title="暂无旅客数据" emoji="👤" />
    </template>
  </div>
</template>

<style scoped>
.section-title {
  font-size: var(--text-lg);
  margin: var(--space-6) 0 var(--space-3);
}
.kpi-grid { margin-bottom: var(--space-2); }
.kpi { padding: var(--space-4) var(--space-5); }
.kpi-value { font-size: var(--text-xl); font-weight: 700; letter-spacing: -0.02em; }
.kpi-label { color: var(--color-text-secondary); font-size: var(--text-sm); margin-top: var(--space-1); }

/* 横向条形：标签 + 轨道 + 数值 */
.bar-row {
  display: grid;
  grid-template-columns: 200px 1fr 190px;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) 0;
}
.bar-label {
  font-size: var(--text-sm);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.bar-sub {
  display: block;
  color: var(--color-text-tertiary);
  font-size: var(--text-xs);
  font-weight: 400;
}
.bar-track {
  height: 10px;
  border-radius: var(--radius-full);
  background: var(--color-surface-2);
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  border-radius: var(--radius-full);
  background: var(--color-warning);
  transition: width var(--transition-base);
}
.bar-fill.accent { background: var(--color-accent); }
.bar-fill.green { background: var(--color-success); }
.bar-value {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  text-align: right;
  white-space: nowrap;
}

@media (max-width: 640px) {
  .bar-row { grid-template-columns: 1fr; gap: var(--space-1); }
  .bar-value { text-align: left; }
}
</style>
