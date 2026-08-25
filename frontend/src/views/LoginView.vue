<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authApi } from '../api/auth'
import { useAuthStore } from '../stores/auth'
import BaseInput from '../components/BaseInput.vue'
import BaseButton from '../components/BaseButton.vue'
import { toast } from '../composables/useToast'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

// 首次运行（系统无用户）时提示创建初始管理员
const notInitialized = ref(false)
onMounted(async () => {
  if (auth.isLoggedIn) return
  try {
    notInitialized.value = !(await authApi.initStatus())
  } catch {
    notInitialized.value = false
  }
})

async function submit() {
  error.value = ''
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  try {
    const data = await authApi.login(form)
    auth.setLogin(data.token, { userId: data.userId, username: data.username, realName: data.realName, role: data.role })
    toast.success('登录成功')
    router.push(route.query.redirect || '/')
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
        <div class="auth-logo">✈️</div>
        <h1 class="auth-title">AirTicket</h1>
        <p class="auth-sub">登录后开始预订您的旅程</p>
      </div>
      <div v-if="notInitialized" class="init-banner">
        <span>系统尚未初始化</span>
        <RouterLink to="/init">创建初始管理员 →</RouterLink>
      </div>
      <form class="col" @submit.prevent="submit">
        <BaseInput v-model="form.username" label="用户名" placeholder="请输入用户名" autocomplete="username" />
        <BaseInput v-model="form.password" label="密码" type="password" placeholder="请输入密码" autocomplete="current-password" />
        <p v-if="error" class="auth-error">{{ error }}</p>
        <BaseButton block :loading="loading" type="submit">登 录</BaseButton>
      </form>
      <p class="auth-foot">
        还没有账号？<RouterLink to="/register">立即注册</RouterLink>
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
  max-width: 400px;
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  padding: var(--space-8) var(--space-6);
}
.auth-head { text-align: center; margin-bottom: var(--space-6); }
.auth-logo { font-size: 40px; margin-bottom: var(--space-2); }
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
.init-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  background: rgba(0, 113, 227, 0.08);
  border: 1px solid rgba(0, 113, 227, 0.2);
  border-radius: var(--radius-sm);
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--space-4);
  font-size: var(--text-sm);
  color: var(--color-text);
}
.init-banner a { font-weight: 600; white-space: nowrap; }
</style>
