<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { flightApi } from '../api/flight'
import { masterApi } from '../api/misc'
import BaseCard from '../components/BaseCard.vue'
import BaseButton from '../components/BaseButton.vue'
import BaseInput from '../components/BaseInput.vue'
import BaseSelect from '../components/BaseSelect.vue'
import BaseModal from '../components/BaseModal.vue'
import Skeleton from '../components/Skeleton.vue'
import EmptyState from '../components/EmptyState.vue'
import { toast } from '../composables/useToast'
import { formatDate, yuan } from '../utils/format'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const flights = ref([])
const loading = ref(false)
const editOpen = ref(false)
const editing = ref(null)   // null=新建
const form = ref(blankForm())

// 航班状态选项（放票管理可把航班置"已取消"模拟极端天气导致无法出行）
const FLIGHT_STATUS_OPTIONS = [
  { value: 'ON_TIME', label: '正常' },
  { value: 'DELAYED', label: '延误' },
  { value: 'CANCELLED', label: '已取消' }
]

// 下拉数据源：航司 / 机型 / 机场，用于把"名字"翻译成"ID"
const airlines = ref([])
const planes = ref([])
const airports = ref([])
const planeOptions = computed(() => {
  const airlineOf = (id) => airlines.value.find((a) => a.id === id)?.name || ''
  return planes.value.map((p) => ({ value: p.id, label: p.modelName + (airlineOf(p.idAirline) ? ` · ${airlineOf(p.idAirline)}` : '') }))
})
const airportOptions = computed(() => airports.value.map((a) => ({ value: a.id, label: `${a.name}（${a.region}）` })))

async function loadMaster() {
  try {
    const [al, pl, ap] = await Promise.all([
      masterApi.list('airline'),
      masterApi.list('plane'),
      masterApi.list('airport')
    ])
    airlines.value = al || []
    planes.value = pl || []
    airports.value = ap || []
  } catch (e) {
    toast.error(e.message)
  }
}

// 选完机场自动填地区（仍可在表单里手动改）
watch(() => form.value.idAirportDep, (id) => {
  const ap = airports.value.find((a) => a.id === Number(id))
  if (ap) form.value.regionDep = ap.region
})
watch(() => form.value.idAirportArr, (id) => {
  const ap = airports.value.find((a) => a.id === Number(id))
  if (ap) form.value.regionArr = ap.region
})

function blankForm() {
  return {
    idPlane: '', idAirportDep: '', idAirportArr: '', code: '',
    datetimeDep: '', datetimeArr: '', regionDep: '', regionArr: '', distance: '',
    seatFirstClass: 0, seatBusinessClass: 0, seatEconomyClass: 0,
    price: '', priceBusinessClass: '', priceFirstClass: '',
    cancellationFee: '', gate: '', status: 'ON_TIME'
  }
}

async function load() {
  loading.value = true
  try {
    const data = await flightApi.search({ size: 100 })
    flights.value = data.data || []
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}
onMounted(() => {
  load()
  loadMaster()
})

function openAdd() {
  editing.value = null
  form.value = blankForm()
  editOpen.value = true
}
// 权限：谁放的票谁能编辑 —— 管理员可管一切；商家只能编辑自己放出的航班
function canEdit(f) {
  if (auth.isAdmin) return true
  if (auth.isMerchant) return f.createdBy === auth.user?.userId
  return false
}
function openEdit(f) {
  editing.value = f
  form.value = {
    idPlane: f.idPlane, idAirportDep: f.idAirportDep, idAirportArr: f.idAirportArr, code: f.code,
    // datetime-local 只认 YYYY-MM-DDTHH:mm，带秒会显示不出来/提交报错，这里截掉秒
    datetimeDep: String(f.datetimeDep).slice(0, 16), datetimeArr: String(f.datetimeArr).slice(0, 16),
    regionDep: f.regionDep, regionArr: f.regionArr,
    distance: f.distance, seatFirstClass: f.seatFirstClass, seatBusinessClass: f.seatBusinessClass,
    seatEconomyClass: f.seatEconomyClass, price: f.price, priceBusinessClass: f.priceBusinessClass,
    priceFirstClass: f.priceFirstClass, cancellationFee: f.cancellationFee, gate: f.gate, status: f.status
  }
  editOpen.value = true
}

function toPayload() {
  const v = form.value
  return {
    idPlane: Number(v.idPlane), idAirportDep: Number(v.idAirportDep), idAirportArr: Number(v.idAirportArr),
    code: v.code, datetimeDep: v.datetimeDep, datetimeArr: v.datetimeArr,
    regionDep: v.regionDep, regionArr: v.regionArr, distance: Number(v.distance),
    seatFirstClass: Number(v.seatFirstClass), seatBusinessClass: Number(v.seatBusinessClass),
    seatEconomyClass: Number(v.seatEconomyClass),
    price: Number(v.price), priceBusinessClass: Number(v.priceBusinessClass), priceFirstClass: Number(v.priceFirstClass),
    cancellationFee: Number(v.cancellationFee), gate: v.gate, status: v.status
  }
}

async function save() {
  try {
    if (editing.value) {
      // 从非取消切到"已取消"：走后端取消航班接口（会批量全额退款），而不是普通更新
      const wasCancelled = editing.value.status === 'CANCELLED'
      const toCancelled = form.value.status === 'CANCELLED'
      if (!wasCancelled && toCancelled) {
        const r = await flightApi.cancel(editing.value.id)
        toast.success(`航班已取消：退款 ${r.refundedCount} 单，共 ${yuan(r.refundTotal)}`)
      } else {
        await flightApi.update(editing.value.id, toPayload())
        toast.success('航班已更新')
      }
    } else {
      await flightApi.create(toPayload())
      toast.success('航班已创建（放票）')
    }
    editOpen.value = false
    await load()
  } catch (e) {
    toast.error(e.message)
  }
}

async function cancelFlight(f) {
  if (!confirm(`确认取消航班 ${f.code}？已支付订单将全额退款，未支付订单将被取消。`)) return
  try {
    const r = await flightApi.cancel(f.id)
    toast.success(`航班已取消：退款 ${r.refundedCount} 单，共 ${yuan(r.refundTotal)}`)
    await load()
  } catch (e) {
    toast.error(e.message)
  }
}

async function remove(f) {
  if (!confirm(`确认删除航班 ${f.code}？`)) return
  try {
    await flightApi.remove(f.id)
    toast.success('已删除')
    await load()
  } catch (e) {
    toast.error(e.message)
  }
}
</script>

<template>
  <div>
    <h1 class="page-title">放票管理</h1>
    <p class="page-subtitle">创建与维护航班（仅商家 / 管理员）</p>

    <div class="row-between" style="margin-bottom: var(--space-4)">
      <span class="muted">共 {{ flights.length }} 个航班</span>
      <BaseButton @click="openAdd">创建航班</BaseButton>
    </div>

    <div v-if="loading"><Skeleton :rows="4" /></div>
    <div v-else-if="flights.length" class="col">
      <BaseCard v-for="f in flights" :key="f.id" class="row-between">
        <div>
          <div class="f-code">
            {{ f.code }} · {{ f.regionDep }} → {{ f.regionArr }}
            <span v-if="f.status === 'CANCELLED'" class="cancelled-badge">已取消</span>
          </div>
          <div class="muted">{{ formatDate(f.datetimeDep) }} · {{ yuan(f.price) }} 起 · 经济{{ f.seatEconomyClass }} 商务{{ f.seatBusinessClass }} 头等{{ f.seatFirstClass }} · 退票费{{ yuan(f.cancellationFee) }}</div>
          <div v-if="!canEdit(f)" class="muted owner-hint">他人放出的航班，仅管理员可编辑</div>
        </div>
        <div class="row">
          <template v-if="canEdit(f)">
            <BaseButton variant="secondary" @click="openEdit(f)">编辑</BaseButton>
            <BaseButton v-if="f.status !== 'CANCELLED'" variant="danger" @click="cancelFlight(f)">取消航班</BaseButton>
            <BaseButton variant="danger" @click="remove(f)">删除</BaseButton>
          </template>
        </div>
      </BaseCard>
    </div>
    <EmptyState v-else title="暂无航班" emoji="✈️" />

    <BaseModal :open="editOpen" :title="editing ? '编辑航班 ' + editing.code : '创建航班'" @close="editOpen = false" width="640px">
      <div class="form-grid">
        <!-- 航班号/机型/起降机场是身份字段（创建后不可改），编辑时只读展示 -->
        <BaseInput v-model="form.code" label="航班号" :disabled="Boolean(editing)" />
        <BaseSelect v-model="form.idPlane" label="机型" :options="planeOptions" placeholder="请选择机型" :disabled="Boolean(editing)" />
        <BaseSelect v-model="form.idAirportDep" label="出发机场" :options="airportOptions" placeholder="请选择出发机场" :disabled="Boolean(editing)" />
        <BaseSelect v-model="form.idAirportArr" label="到达机场" :options="airportOptions" placeholder="请选择到达机场" :disabled="Boolean(editing)" />
        <BaseInput v-model="form.datetimeDep" label="出发时间" type="datetime-local" />
        <BaseInput v-model="form.datetimeArr" label="到达时间" type="datetime-local" />
        <BaseInput v-model="form.regionDep" label="出发地区" />
        <BaseInput v-model="form.regionArr" label="到达地区" />
        <BaseInput v-model="form.distance" label="距离(km)" type="number" />
        <BaseInput v-model="form.gate" label="登机口" />
        <BaseSelect v-model="form.status" label="状态" :options="FLIGHT_STATUS_OPTIONS" />
        <BaseInput v-model="form.cancellationFee" label="退票费" type="number" />
        <BaseInput v-model="form.price" label="经济舱价" type="number" />
        <BaseInput v-model="form.priceBusinessClass" label="商务舱价" type="number" />
        <BaseInput v-model="form.priceFirstClass" label="头等舱价" type="number" />
        <BaseInput v-model="form.seatEconomyClass" label="经济舱余票" type="number" />
        <BaseInput v-model="form.seatBusinessClass" label="商务舱余票" type="number" />
        <BaseInput v-model="form.seatFirstClass" label="头等舱余票" type="number" />
      </div>
      <template #footer>
        <div class="row" style="justify-content:flex-end">
          <BaseButton variant="ghost" @click="editOpen = false">取消</BaseButton>
          <BaseButton @click="save">保存</BaseButton>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<style scoped>
.f-code { font-weight: 700; margin-bottom: var(--space-1); }
.cancelled-badge {
  margin-left: var(--space-2);
  font-size: var(--text-xs);
  font-weight: 500;
  color: var(--color-danger);
  background: rgba(255, 59, 48, 0.1);
  padding: 2px 10px;
  border-radius: var(--radius-full);
}
.owner-hint { font-size: var(--text-xs); margin-top: var(--space-1); }
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);
}
@media (max-width: 640px) { .form-grid { grid-template-columns: 1fr; } }
</style>
