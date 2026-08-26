<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { routeApi } from '../api/misc'
import { flightApi } from '../api/flight'
import BaseCard from '../components/BaseCard.vue'
import BaseInput from '../components/BaseInput.vue'
import BaseButton from '../components/BaseButton.vue'
import BaseModal from '../components/BaseModal.vue'
import EmptyState from '../components/EmptyState.vue'
import Skeleton from '../components/Skeleton.vue'
import { toast } from '../composables/useToast'
import { formatDate, formatTime, formatDateTime } from '../utils/format'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const auth = useAuthStore()

// 查询航班号：搜出匹配班次供点选
const codeInput = ref('')
const candidates = ref([])
const searching = ref(false)
const searched = ref(false)

// 轨迹结果：选中航班后按 id 查
const flight = ref(null)
const result = ref(null)
const loading = ref(false)
const queried = ref(false)

// 编辑轨迹：谁放的票谁能编辑（管理员可管一切）
const editOpen = ref(false)
const routeForm = ref(blankRouteForm())
const saving = ref(false)

function blankRouteForm() {
  return {
    distanceRemain: '', timeRemain: '', altitude: '', speed: '',
    latitude: '', longitude: '', timeStamp: ''
  }
}

// 当前用户能否管这个航班（商家只看自己放出的票）
const canManage = computed(() => {
  if (!auth.isLoggedIn) return false
  if (auth.isAdmin) return true
  return auth.isMerchant && flight.value?.createdBy === auth.user?.userId
})

// 是否处于该航班飞行时间窗内（前后端同为本机时区，均按本地时间判断）
const inFlightWindow = computed(() => {
  if (!flight.value) return false
  const dep = new Date(flight.value.datetimeDep).getTime()
  const arr = new Date(flight.value.datetimeArr).getTime()
  const now = Date.now()
  return now >= dep && now <= arr
})

// 是否有权限且处于飞行时间窗内 -> 显示"录入/编辑轨迹"入口（无轨迹记录时也可新建）
const canEditRoute = computed(() => canManage.value && inFlightWindow.value)

function nowLocalInput() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`
}

async function trackById(id) {
  if (!id) return
  loading.value = true
  result.value = null
  flight.value = null
  try {
    flight.value = await flightApi.detail(Number(id))
    result.value = await routeApi.get(Number(id))
  } catch (e) {
    if (e.message) toast.error(e.message)
  } finally {
    loading.value = false
    queried.value = true
  }
}

async function searchCode() {
  const code = (codeInput.value || '').trim().toUpperCase()
  if (!code) {
    toast.error('请输入航班号')
    return
  }
  searching.value = true
  candidates.value = []
  searched.value = true
  try {
    const data = await flightApi.search({ code, size: 20 })
    candidates.value = data.data || []
  } catch (e) {
    if (e.message) toast.error(e.message)
  } finally {
    searching.value = false
  }
}

function pick(f) {
  trackById(f.id)
}

function openEditRoute() {
  // 有轨迹则带出当前值；无轨迹（新建）则留空、采集时间默认当前时间
  routeForm.value = {
    distanceRemain: result.value?.distanceRemain ?? '',
    timeRemain: result.value?.timeRemain ?? '',
    altitude: result.value?.altitude ?? '',
    speed: result.value?.speed ?? '',
    latitude: result.value?.latitude ?? '',
    longitude: result.value?.longitude ?? '',
    timeStamp: result.value ? String(result.value.timeStamp).slice(0, 16) : nowLocalInput()
  }
  editOpen.value = true
}

async function saveRoute() {
  saving.value = true
  try {
    await routeApi.update(flight.value.id, {
      distanceRemain: Number(routeForm.value.distanceRemain),
      timeRemain: Number(routeForm.value.timeRemain),
      altitude: Number(routeForm.value.altitude),
      speed: Number(routeForm.value.speed),
      latitude: Number(routeForm.value.latitude),
      longitude: Number(routeForm.value.longitude),
      timeStamp: routeForm.value.timeStamp
    })
    toast.success('轨迹已更新')
    editOpen.value = false
    result.value = await routeApi.get(Number(flight.value.id))   // 刷新最新轨迹
  } catch (e) {
    toast.error(e.message)
  } finally {
    saving.value = false
  }
}

// 从航班详情页带着 id 跳进来：直接查轨迹，不用再输入
onMounted(() => {
  if (route.params.id) trackById(route.params.id)
})
</script>

<template>
  <div>
    <h1 class="page-title">实时轨迹</h1>
    <p class="page-subtitle">按航班号查询飞行中的实时位置与状态</p>

    <BaseCard class="q-card">
      <div class="row">
        <BaseInput v-model="codeInput" label="航班号" placeholder="如 CA1201" @keyup.enter="searchCode" />
        <BaseButton :loading="searching" @click="searchCode">查询</BaseButton>
      </div>
    </BaseCard>

    <!-- 航班号搜索出的候选班次：点选后才查轨迹（同一航班号可能有多班） -->
    <div v-if="searching" class="mt"><Skeleton card :rows="3" /></div>
    <template v-else-if="candidates.length">
      <div class="mt">
        <p class="muted count">共 {{ candidates.length }} 个匹配班次，点击查看轨迹</p>
        <div class="col">
          <BaseCard
            v-for="f in candidates"
            :key="f.id"
            hoverable
            clickable
            class="cand"
            @click="pick(f)"
          >
            <div class="row-between">
              <div>
                <div class="cand-code">{{ f.code }} · {{ f.status }}</div>
                <div class="muted">{{ f.regionDep }} → {{ f.regionArr }} · {{ formatDate(f.datetimeDep) }} {{ formatTime(f.datetimeDep) }}</div>
              </div>
              <span class="muted">查看轨迹 →</span>
            </div>
          </BaseCard>
        </div>
      </div>
    </template>
    <EmptyState
      v-else-if="searched"
      class="mt"
      title="没有找到该航班号"
      description="试试别的航班号"
      emoji="🔎"
    />

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
      <div v-if="canEditRoute" class="edit-row">
        <BaseButton variant="secondary" @click="openEditRoute">编辑轨迹</BaseButton>
        <span class="muted hint">模拟机器检测自动更新，仅在飞行时间窗内可编辑</span>
      </div>
      <div v-else-if="canManage" class="edit-row">
        <span class="muted hint">仅航班飞行期间可编辑轨迹（当前不在飞行时间窗内）</span>
      </div>
    </div>

    <EmptyState v-else-if="queried" class="mt" title="该航班暂无轨迹" description="航班可能未起飞或没有轨迹记录" emoji="🛰️">
      <BaseButton v-if="canEditRoute" variant="secondary" @click="openEditRoute">录入轨迹</BaseButton>
    </EmptyState>

    <BaseModal :open="editOpen" :title="`编辑轨迹 · ${flight?.code || ''}`" width="520px" @close="editOpen = false">
      <div class="form-grid">
        <BaseInput v-model="routeForm.distanceRemain" label="剩余距离(km)" type="number" />
        <BaseInput v-model="routeForm.timeRemain" label="剩余时间(分钟)" type="number" />
        <BaseInput v-model="routeForm.altitude" label="高度(m)" type="number" />
        <BaseInput v-model="routeForm.speed" label="速度(km/h)" type="number" />
        <BaseInput v-model="routeForm.latitude" label="纬度" type="number" step="any" />
        <BaseInput v-model="routeForm.longitude" label="经度" type="number" step="any" />
        <BaseInput v-model="routeForm.timeStamp" label="采集时间" type="datetime-local" />
      </div>
      <template #footer>
        <div class="row" style="justify-content:flex-end">
          <BaseButton variant="ghost" @click="editOpen = false">取消</BaseButton>
          <BaseButton :loading="saving" @click="saveRoute">保存</BaseButton>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<style scoped>
.q-card { margin-bottom: var(--space-5); }
.q-card .row { align-items: flex-end; gap: var(--space-3); }
.q-card :deep(.field) { flex: 1; }
.mt { margin-top: var(--space-4); }
.count { margin-bottom: var(--space-2); }
.cand { cursor: pointer; }
.cand-code { font-weight: 700; margin-bottom: var(--space-1); }
.s-title { font-size: var(--text-xl); margin-bottom: var(--space-4); }
.stat { text-align: center; padding: var(--space-5); }
.stat-label { font-size: var(--text-sm); margin-bottom: var(--space-2); }
.stat-val { font-size: var(--text-xl); font-weight: 700; }
.time { margin-top: var(--space-4); text-align: center; }
.edit-row { margin-top: var(--space-4); text-align: center; }
.edit-row .hint { display: block; font-size: var(--text-xs); margin-top: var(--space-2); }
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);
}
@media (max-width: 640px) { .form-grid { grid-template-columns: 1fr; } }
</style>
