<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api/auth'
import BaseInput from '../components/BaseInput.vue'
import BaseButton from '../components/BaseButton.vue'
import { toast } from '../composables/useToast'

const router = useRouter()
const form = reactive({ username: '', password: '', realName: '', age: null, email: '', phone: '' })
const loading = ref(false)
const error = ref('')

// 前端校验（提交前拦截，与后端规则一致）
const errors = reactive({ username: '', password: '', realName: '', age: '', contact: '', email: '', phone: '' })

const EMAIL_RE = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/
const PHONE_RE = /^1[3-9][0-9]{9}$/

function validate() {
  Object.keys(errors).forEach((k) => (errors[k] = ''))
  let ok = true
  if (!form.username) {
    errors.username = '用户名不能为空'
    ok = false
  }
  const kinds = [
    /[A-Z]/.test(form.password),
    /[a-z]/.test(form.password),
    /\d/.test(form.password),
    /[^A-Za-z0-9]/.test(form.password)
  ].filter(Boolean).length
  if (!form.password || kinds < 3) {
    errors.password = '密码需包含大小写字母、数字、符号中至少三类'
    ok = false
  }
  if (!form.realName) {
    errors.realName = '真实姓名不能为空'
    ok = false
  }
  if (!form.age || form.age < 1 || form.age > 120) {
    errors.age = '年龄需在 1-120 之间'
    ok = false
  }
  const hasEmail = !!form.email
  const hasPhone = !!form.phone
  if (!hasEmail && !hasPhone) {
    errors.contact = '邮箱与手机号至少填一个'
    ok = false
  }
  if (hasEmail && !EMAIL_RE.test(form.email)) {
    errors.email = '邮箱格式非法'
    ok = false
  }
  if (hasPhone && !PHONE_RE.test(form.phone)) {
    errors.phone = '手机号格式非法'
    ok = false
  }
  return ok
}

async function submit() {
  error.value = ''
  if (!validate()) return
  loading.value = true
  try {
    await authApi.register({
      username: form.username,
      password: form.password,
      realName: form.realName,
      age: Number(form.age),
      email: form.email || null,
      phone: form.phone || null
    })
    toast.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-wrap">
    <div class="auth-card">
      <div class="auth-head">
        <h1 class="auth-title">注册账号</h1>
        <p class="auth-sub">创建旅客账号，预订您的旅程</p>
      </div>
      <form class="col" @submit.prevent="submit">
        <BaseInput v-model="form.username" label="用户名" placeholder="请输入用户名" :error="errors.username" />
        <BaseInput v-model="form.password" label="密码" type="password" placeholder="含大小写/数字/符号至少三类" :error="errors.password" />
        <BaseInput v-model="form.realName" label="真实姓名" placeholder="请输入真实姓名" :error="errors.realName" />
        <BaseInput v-model="form.age" label="年龄" type="number" placeholder="请输入年龄" :error="errors.age" />
        <p v-if="errors.contact" class="auth-error">{{ errors.contact }}</p>
        <BaseInput v-model="form.email" label="邮箱" placeholder="选填，需合法邮箱" :error="errors.email" />
        <BaseInput v-model="form.phone" label="手机号" placeholder="选填，11 位大陆手机号" :error="errors.phone" />
        <p v-if="error" class="auth-error">{{ error }}</p>
        <BaseButton block :loading="loading" type="submit">注 册</BaseButton>
      </form>
      <p class="auth-foot">
        已有账号？<RouterLink to="/login">去登录</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-wrap {
  min-height: calc(100vh - 56px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
}
.auth-card {
  width: 100%;
  max-width: 420px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: var(--space-8) var(--space-6);
}
.auth-head { text-align: center; margin-bottom: var(--space-6); }
.auth-title { font-size: var(--text-2xl); font-weight: 700; letter-spacing: -0.03em; }
.auth-sub { color: var(--color-text-secondary); margin-top: var(--space-1); }
.auth-error {
  color: var(--color-danger);
  font-size: var(--text-sm);
  background: rgba(255, 59, 48, 0.06);
  border-radius: var(--radius-sm);
  padding: var(--space-2) var(--space-3);
}
.auth-foot { text-align: center; margin-top: var(--space-5); color: var(--color-text-secondary); font-size: var(--text-sm); }
</style>
