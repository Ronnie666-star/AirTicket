<script setup>
import { reactive, ref, onMounted } from 'vue'
import { adminApi } from '../api/misc'
import BaseCard from '../components/BaseCard.vue'
import BaseInput from '../components/BaseInput.vue'
import BaseSelect from '../components/BaseSelect.vue'
import BaseButton from '../components/BaseButton.vue'
import BaseModal from '../components/BaseModal.vue'
import Skeleton from '../components/Skeleton.vue'
import EmptyState from '../components/EmptyState.vue'
import { toast } from '../composables/useToast'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const filters = reactive({ username: '', role: '', enabled: '' })
const users = ref([])
const loading = ref(false)

const ROLE_OPTIONS = [
  { value: 'PASSENGER', label: '旅客' },
  { value: 'MERCHANT', label: '商家' },
  { value: 'ADMIN', label: '管理员' }
]
const STATUS_OPTIONS = [
  { value: 'true', label: '启用' },
  { value: 'false', label: '禁用' }
]

async function load() {
  loading.value = true
  try {
    const params = {}
    if (filters.username) params.username = filters.username
    if (filters.role) params.role = filters.role
    if (filters.enabled !== '') params.enabled = filters.enabled === 'true'
    users.value = await adminApi.users(params)
  } catch (e) {
    toast.error(e.message)
  } finally {
    loading.value = false
  }
}
onMounted(load)

// 创建商家（管理员分配商家账号，固定 MERCHANT 角色）
const creating = ref(false)
const createForm = reactive({ username: '', password: '', realName: '', age: '', email: '', phone: '' })
const createLoading = ref(false)
const createError = ref('')

function openCreate() {
  Object.assign(createForm, { username: '', password: '', realName: '', age: '', email: '', phone: '' })
  createError.value = ''
  creating.value = true
}
async function doCreate() {
  createError.value = ''
  if (!createForm.username || !createForm.password) {
    createError.value = '请填写用户名和密码'
    return
  }
  if (!createForm.email && !createForm.phone) {
    createError.value = '邮箱与手机号至少填一个'
    return
  }
  createLoading.value = true
  try {
    await adminApi.createUser({
      username: createForm.username,
      password: createForm.password,
      realName: createForm.realName || null,
      age: createForm.age ? Number(createForm.age) : null,
      email: createForm.email || null,
      phone: createForm.phone || null
    })
    toast.success('商家创建成功')
    creating.value = false
    await load()
  } catch (e) {
    createError.value = e.message
  } finally {
    createLoading.value = false
  }
}

// 重置密码
const resetUser = ref(null)
const newPassword = ref('')
async function doReset() {
  try {
    await adminApi.resetPassword(resetUser.value.id, newPassword.value)
    toast.success('密码已重置')
    resetUser.value = null
    newPassword.value = ''
  } catch (e) {
    toast.error(e.message)
  }
}

async function toggle(u) {
  try {
    await adminApi.changeStatus(u.id, !u.enabled)
    toast.success(u.enabled ? '已禁用该账号' : '已启用该账号')
    await load()
  } catch (e) {
    toast.error(e.message)
  }
}

function isSelf(u) {
  return u.id === auth.user?.userId
}
</script>

<template>
  <div>
    <div class="row-between">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-subtitle">管理全部用户账号（仅管理员）</p>
      </div>
      <BaseButton @click="openCreate">创建商家</BaseButton>
    </div>

    <div class="filters row">
      <BaseInput v-model="filters.username" placeholder="用户名" style="flex:1" />
      <BaseSelect v-model="filters.role" placeholder="角色" :options="ROLE_OPTIONS" />
      <BaseSelect v-model="filters.enabled" placeholder="状态" :options="STATUS_OPTIONS" />
      <BaseButton variant="secondary" @click="load">筛选</BaseButton>
    </div>

    <div v-if="loading"><Skeleton :rows="4" /></div>
    <div v-else-if="users.length" class="col">
      <BaseCard v-for="u in users" :key="u.id" class="row-between">
        <div>
          <div class="u-name">{{ u.username }}（{{ u.realName || '—' }}）</div>
          <div class="muted">ID {{ u.id }} · {{ u.role }} · {{ u.enabled ? '启用' : '禁用' }}</div>
        </div>
        <div class="row">
          <BaseButton variant="ghost" :disabled="isSelf(u)" @click="toggle(u)">{{ u.enabled ? '禁用' : '启用' }}</BaseButton>
          <BaseButton variant="secondary" @click="resetUser = u; newPassword = ''">重置密码</BaseButton>
        </div>
      </BaseCard>
    </div>
    <EmptyState v-else title="无匹配用户" emoji="👤" />

    <BaseModal :open="creating" title="创建商家" @close="creating = false">
      <div class="col">
        <BaseInput v-model="createForm.username" label="用户名" placeholder="请输入用户名" />
        <BaseInput v-model="createForm.password" label="初始密码" type="password" placeholder="含大小写/数字/符号至少三类" />
        <BaseInput v-model="createForm.realName" label="真实姓名（选填）" placeholder="商家可不填" />
        <BaseInput v-model="createForm.age" label="年龄（选填）" type="number" placeholder="商家可不填" />
        <BaseInput v-model="createForm.email" label="邮箱" placeholder="选填，与手机号至少一个" />
        <BaseInput v-model="createForm.phone" label="手机号" placeholder="选填，11 位大陆手机号" />
        <p v-if="createError" class="error-text">{{ createError }}</p>
      </div>
      <template #footer>
        <div class="row" style="justify-content:flex-end">
          <BaseButton variant="ghost" @click="creating = false">取消</BaseButton>
          <BaseButton :loading="createLoading" @click="doCreate">创建</BaseButton>
        </div>
      </template>
    </BaseModal>

    <BaseModal :open="!!resetUser" :title="`重置 ${resetUser?.username} 的密码`" @close="resetUser = null">
      <BaseInput v-model="newPassword" label="新密码" type="password" placeholder="含大小写/数字/符号至少三类" />
      <template #footer>
        <div class="row" style="justify-content:flex-end">
          <BaseButton variant="ghost" @click="resetUser = null">取消</BaseButton>
          <BaseButton @click="doReset">重置</BaseButton>
        </div>
      </template>
    </BaseModal>
  </div>
</template>

<style scoped>
.filters { margin-bottom: var(--space-5); flex-wrap: wrap; }
.filters :deep(.field) { min-width: 140px; }
.u-name { font-weight: 600; margin-bottom: var(--space-1); }
.error-text { color: var(--color-danger); font-size: var(--text-sm); }
</style>
