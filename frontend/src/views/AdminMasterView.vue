<script setup>
import { ref, onMounted } from 'vue'
import { masterApi } from '../api/misc'
import BaseCard from '../components/BaseCard.vue'
import BaseButton from '../components/BaseButton.vue'
import BaseInput from '../components/BaseInput.vue'
import BaseSelect from '../components/BaseSelect.vue'
import BaseModal from '../components/BaseModal.vue'
import Skeleton from '../components/Skeleton.vue'
import EmptyState from '../components/EmptyState.vue'
import { toast } from '../composables/useToast'

// 每类基础数据的列与表单定义
const TABS = [
  { kind: 'airline', label: '航司', columns: ['name'], fields: [{ key: 'name', label: '航司名' }] },
  { kind: 'airport', label: '机场', columns: ['name', 'region'], fields: [{ key: 'name', label: '机场名' }, { key: 'region', label: '地区' }] },
  {
    kind: 'plane', label: '机型',
    columns: ['modelName', 'idAirline'],
    fields: [
      { key: 'modelName', label: '型号' },
      { key: 'idAirline', label: '所属航司', type: 'select', source: 'airline' },
      { key: 'length', label: '长度(m)' },
      { key: 'wingspan', label: '翼展(m)' },
      { key: 'height', label: '高度(m)' },
      { key: 'maxTakeoffWeightKg', label: '最大起飞重量(kg)' },
      { key: 'maxLandingWeightKg', label: '最大着陆重量(kg)' },
      { key: 'maxSeatFirstClass', label: '头等舱上限' },
      { key: 'maxSeatBusinessClass', label: '商务舱上限' },
      { key: 'maxSeatEconomyClass', label: '经济舱上限' }
    ]
  },
  { kind: 'channel', label: '渠道', columns: ['channelName', 'apiGatewayUrl'], fields: [{ key: 'channelName', label: '渠道名' }, { key: 'apiGatewayUrl', label: '网关地址' }] }
]

const activeKind = ref('airline')
const lists = ref({ airline: [], airport: [], plane: [], channel: [] })
const loading = ref(false)

const currentTab = () => TABS.find((t) => t.kind === activeKind.value)
const rows = () => lists.value[activeKind.value] || []

async function load(kind = activeKind.value) {
  loading.value = true
  try {
    lists.value[kind] = await masterApi.list(kind)
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
  await ensureSources()
}

function switchTab(kind) {
  activeKind.value = kind
  if (!lists.value[kind].length) load(kind)
}
onMounted(() => load())

// 下拉字段的数据源缓存（如机型的"所属航司"）；按需加载，加载过一次就不再拉
const selectSources = ref({ airline: [], airport: [], plane: [] })
async function ensureSources() {
  const kinds = currentTab().fields.filter((f) => f.type === 'select').map((f) => f.source)
  for (const k of kinds) {
    if (!selectSources.value[k].length) {
      try {
        selectSources.value[k] = await masterApi.list(k)
      } catch (e) {
        toast.error(e.message)
      }
    }
  }
}
function optionsFor(source) {
  return selectSources.value[source].map((item) => ({
    value: item.id,
    label: item.name || item.modelName || item.channelName
  }))
}
// 列表展示时把 ID 翻译成名字（如 idAirline -> 航司名）
function cellValue(row, c) {
  if (c === 'idAirline') {
    const al = selectSources.value.airline.find((a) => a.id === row[c])
    return al ? al.name : row[c]
  }
  return row[c]
}

// 新增 / 编辑
const editOpen = ref(false)
const editing = ref(null)          // null=新增
const form = ref({})
async function openAdd() {
  editing.value = null
  form.value = {}
  await ensureSources()
  editOpen.value = true
}
async function openEdit(row) {
  editing.value = row
  form.value = { ...row }
  await ensureSources()
  editOpen.value = true
}
async function save() {
  try {
    const kind = activeKind.value
    if (editing.value) {
      await masterApi.update(kind, editing.value.id, form.value)
      toast.success('已更新')
    } else {
      await masterApi.create(kind, form.value)
      toast.success('已新增')
    }
    editOpen.value = false
    await load(kind)
  } catch (e) {
    toast.error(e.message)
  }
}
async function remove(row) {
  if (!confirm(`确认删除该${currentTab().label}？被引用数据无法删除。`)) return
  try {
    await masterApi.remove(activeKind.value, row.id)
    toast.success('已删除')
    await load(activeKind.value)
  } catch (e) {
    toast.error(e.message)
  }
}
</script>

<template>
  <div>
    <h1 class="page-title">基础数据</h1>
    <p class="page-subtitle">维护航司 / 机场 / 机型 / 渠道（仅管理员可写）</p>

    <div class="tabs">
      <button v-for="t in TABS" :key="t.kind" class="tab" :class="{ on: activeKind === t.kind }" @click="switchTab(t.kind)">
        {{ t.label }}
      </button>
    </div>

    <div class="row-between" style="margin-bottom: var(--space-4)">
      <span class="muted">共 {{ rows().length }} 条</span>
      <BaseButton @click="openAdd">新增{{ currentTab().label }}</BaseButton>
    </div>

    <div v-if="loading"><Skeleton :rows="4" /></div>
    <div v-else-if="rows().length" class="col">
      <BaseCard v-for="row in rows()" :key="row.id" class="row-between">
        <div>
          <div class="row-name">{{ row.name || row.modelName || row.channelName }}</div>
          <div class="muted">
            <template v-for="(c, i) in currentTab().columns.filter(c => c !== 'name' && c !== 'modelName' && c !== 'channelName')" :key="c">
              {{ c === 'idAirline' ? '航司 ' : '' }}{{ cellValue(row, c) }}{{ i < currentTab().columns.length - 2 ? ' · ' : '' }}
            </template>
          </div>
        </div>
        <div class="row">
          <BaseButton variant="ghost" @click="openEdit(row)">编辑</BaseButton>
          <BaseButton variant="danger" @click="remove(row)">删除</BaseButton>
        </div>
      </BaseCard>
    </div>
    <EmptyState v-else title="暂无数据" emoji="🗄️" />

    <BaseModal :open="editOpen" :title="editing ? '编辑' + currentTab().label : '新增' + currentTab().label" @close="editOpen = false">
      <div class="col">
        <template v-for="f in currentTab().fields" :key="f.key">
          <BaseSelect
            v-if="f.type === 'select'"
            v-model="form[f.key]"
            :label="f.label"
            :options="optionsFor(f.source)"
            :placeholder="`请选择${f.label}`"
          />
          <BaseInput
            v-else
            :model-value="String(form[f.key] ?? '')"
            :label="f.label"
            @update:model-value="form[f.key] = $event"
          />
        </template>
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
.tabs { display: flex; gap: var(--space-2); margin-bottom: var(--space-5); border-bottom: 1px solid var(--color-border); }
.tab {
  border: none;
  background: transparent;
  padding: var(--space-3) var(--space-4);
  font-size: var(--text-base);
  cursor: pointer;
  color: var(--color-text-secondary);
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  font-family: inherit;
}
.tab.on { color: var(--color-accent); border-bottom-color: var(--color-accent); font-weight: 600; }
.row-name { font-weight: 600; margin-bottom: var(--space-1); }
</style>
