<script setup>
import { ref, computed, onMounted } from 'vue'
import { authApi } from '../api/auth'
import { passengerApi } from '../api/misc'
import BaseCard from '../components/BaseCard.vue'
import BaseInput from '../components/BaseInput.vue'
import BaseButton from '../components/BaseButton.vue'
import BaseModal from '../components/BaseModal.vue'
import Skeleton from '../components/Skeleton.vue'
import EmptyState from '../components/EmptyState.vue'
import { toast } from '../composables/useToast'

const tab = ref('profile')   // profile | password | passengers

// 旅客的真实姓名 / 年龄必填，管理员与商家可留空
const isPassenger = computed(() => profile.value?.role === 'PASSENGER')

// ===== 资料 =====
const profile = ref(null)
const form = ref({ realName: '', age: null, email: '', phone: '' })
const saveLoading = ref(false)

async function loadProfile() {
  profile.value = await authApi.getProfile()
  form.value = { realName: profile.value.realName, age: profile.value.age, email: profile.value.email || '', phone: profile.value.phone || '' }
}
async function saveProfile() {
  saveLoading.value = true
  try {
    profile.value = await authApi.updateProfile({
      realName: form.value.realName || null,
      age: form.value.age ? Number(form.value.age) : null,
      email: form.value.email || null,
      phone: form.value.phone || null
    })
    toast.success('资料已更新')
  } catch (e) {
    toast.error(e.message)
  } finally {
    saveLoading.value = false
  }
}

// ===== 密码 =====
const pwd = ref({ oldPassword: '', newPassword: '', confirm: '' })
const pwdLoading = ref(false)
async function changePassword() {
  if (pwd.value.newPassword !== pwd.value.confirm) {
    toast.error('两次输入的新密码不一致')
    return
  }
  pwdLoading.value = true
  try {
    await authApi.changePassword({ oldPassword: pwd.value.oldPassword, newPassword: pwd.value.newPassword })
    toast.success('密码修改成功')
    pwd.value = { oldPassword: '', newPassword: '', confirm: '' }
  } catch (e) {
    toast.error(e.message)
  } finally {
    pwdLoading.value = false
  }
}

// ===== 常用乘机人 =====
const passengers = ref([])
const addOpen = ref(false)
const addPassengerId = ref(null)
const passLoading = ref(false)

async function loadPassengers() {
  passengers.value = await passengerApi.list()
}
async function addPassenger() {
  if (!addPassengerId.value) {
    toast.error('请输入乘机人用户 ID')
    return
  }
  passLoading.value = true
  try {
    await passengerApi.add(Number(addPassengerId.value))
    toast.success('已添加乘机人')
    addOpen.value = false
    addPassengerId.value = null
    await loadPassengers()
  } catch (e) {
    toast.error(e.message)
  } finally {
    passLoading.value = false
  }
}
async function removePassenger(id) {
  try {
    await passengerApi.remove(id)
    toast.success('已删除')
    await loadPassengers()
  } catch (e) {
    toast.error(e.message)
  }
}

function switchTab(t) {
  tab.value = t
  if (t === 'passengers') loadPassengers()
}

onMounted(loadProfile)
</script>

<template>
  <div>
    <h1 class="page-title">个人中心</h1>
    <p class="page-subtitle">管理您的资料、密码与常用乘机人</p>

    <div class="tabs">
      <button class="tab" :class="{ on: tab === 'profile' }" @click="switchTab('profile')">个人资料</button>
      <button class="tab" :class="{ on: tab === 'password' }" @click="switchTab('password')">修改密码</button>
      <button class="tab" :class="{ on: tab === 'passengers' }" @click="switchTab('passengers')">常用乘机人</button>
    </div>

    <!-- 资料 -->
    <BaseCard v-if="tab === 'profile'">
      <template v-if="profile">
        <div class="col">
          <BaseInput :model-value="profile.username" label="用户名" disabled />
          <BaseInput :model-value="profile.role" label="角色" disabled />
          <BaseInput v-model="form.realName" :label="isPassenger ? '真实姓名' : '真实姓名（选填）'" />
          <BaseInput v-model="form.age" :label="isPassenger ? '年龄' : '年龄（选填）'" type="number" />
          <BaseInput v-model="form.email" label="邮箱" />
          <BaseInput v-model="form.phone" label="手机号" />
          <div style="display:flex; justify-content:flex-end">
            <BaseButton :loading="saveLoading" @click="saveProfile">保存</BaseButton>
          </div>
        </div>
      </template>
    </BaseCard>

    <!-- 密码 -->
    <BaseCard v-if="tab === 'password'">
      <div class="col">
        <BaseInput v-model="pwd.oldPassword" label="原密码" type="password" />
        <BaseInput v-model="pwd.newPassword" label="新密码" type="password" placeholder="含大小写/数字/符号至少三类" />
        <BaseInput v-model="pwd.confirm" label="确认新密码" type="password" />
        <div style="display:flex; justify-content:flex-end">
          <BaseButton :loading="pwdLoading" @click="changePassword">修改密码</BaseButton>
        </div>
      </div>
    </BaseCard>

    <!-- 乘机人 -->
    <div v-if="tab === 'passengers'">
      <div class="row-between" style="margin-bottom: var(--space-4)">
        <span class="muted">共 {{ passengers.length }} 位常用乘机人</span>
        <BaseButton @click="addOpen = true">添加乘机人</BaseButton>
      </div>
      <div v-if="passengers.length" class="col">
        <BaseCard v-for="p in passengers" :key="p.id" class="row-between">
          <div>
            <div class="p-name">{{ p.realName }}（{{ p.username }}）</div>
            <div class="muted">乘机人 ID：{{ p.passengerId }}</div>
          </div>
          <BaseButton variant="ghost" @click="removePassenger(p.id)">删除</BaseButton>
        </BaseCard>
      </div>
      <EmptyState v-else title="暂无常用乘机人" description="添加常乘机人，订票更方便" emoji="👥" />
    </div>

    <BaseModal :open="addOpen" title="添加常用乘机人" @close="addOpen = false">
      <BaseInput v-model="addPassengerId" label="乘机人用户 ID" type="number" placeholder="输入对方账号的用户 ID" />
      <template #footer>
        <div class="row" style="justify-content:flex-end">
          <BaseButton variant="ghost" @click="addOpen = false">取消</BaseButton>
          <BaseButton :loading="passLoading" @click="addPassenger">添加</BaseButton>
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
.p-name { font-weight: 600; }
</style>
