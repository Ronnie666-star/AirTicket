<script setup>
import { ref, onMounted } from 'vue'
import { flightApi } from '../api/flight'
import BaseCard from '../components/BaseCard.vue'
import BaseButton from '../components/BaseButton.vue'
import BaseInput from '../components/BaseInput.vue'
import BaseModal from '../components/BaseModal.vue'
import Skeleton from '../components/Skeleton.vue'
import EmptyState from '../components/EmptyState.vue'
import { toast } from '../composables/useToast'
import { formatDate, yuan } from '../utils/format'

const flights = ref([])
const loading = ref(false)
const editOpen = ref(false)
const editing = ref(null)   // null=新建
const form = ref(blankForm())

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
onMounted(load)

function openAdd() {
  editing.value = null
  form.value = blankForm()
  editOpen.value = true
}
function openEdit(f) {
  editing.value = f
  form.value = {
    idPlane: f.idPlane, idAirportDep: f.idAirportDep, idAirportArr: f.idAirportArr, code: f.code,
    datetimeDep: f.datetimeDep, datetimeArr: f.datetimeArr, regionDep: f.regionDep, regionArr: f.regionArr,
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
      await flightApi.update(editing.value.id, toPayload())
      toast.success('航班已更新')
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
          <div class="f-code">{{ f.code }} · {{ f.regionDep }} → {{ f.regionArr }}</div>
          <div class="muted">{{ formatDate(f.datetimeDep) }} · {{ yuan(f.price) }} 起 · 经济{{ f.seatEconomyClass }} 商务{{ f.seatBusinessClass }} 头等{{ f.seatFirstClass }}</div>
        </div>
        <div class="row">
          <BaseButton variant="secondary" @click="openEdit(f)">编辑</BaseButton>
          <BaseButton variant="danger" @click="remove(f)">删除</BaseButton>
        </div>
      </BaseCard>
    </div>
    <EmptyState v-else title="暂无航班" emoji="✈️" />

    <BaseModal :open="editOpen" :title="editing ? '编辑航班 ' + editing.code : '创建航班'" @close="editOpen = false" width="640px">
      <div class="form-grid">
        <BaseInput v-model="form.code" label="航班号" />
        <BaseInput v-model="form.idPlane" label="机型 ID" type="number" />
        <BaseInput v-model="form.idAirportDep" label="出发机场 ID" type="number" />
        <BaseInput v-model="form.idAirportArr" label="到达机场 ID" type="number" />
        <BaseInput v-model="form.datetimeDep" label="出发时间" type="datetime-local" />
        <BaseInput v-model="form.datetimeArr" label="到达时间" type="datetime-local" />
        <BaseInput v-model="form.regionDep" label="出发地区" />
        <BaseInput v-model="form.regionArr" label="到达地区" />
        <BaseInput v-model="form.distance" label="距离(km)" type="number" />
        <BaseInput v-model="form.gate" label="登机口" />
        <BaseInput v-model="form.status" label="状态" />
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
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-3);
}
@media (max-width: 640px) { .form-grid { grid-template-columns: 1fr; } }
</style>
