<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api/auth'
import BaseInput from '../components/BaseInput.vue'
import BaseButton from '../components/BaseButton.vue'
import { toast } from '../composables/useToast'

// 初始化向导：系统尚无任何用户时创建初始管理员（唯一入口）。
// 已初始化时访问自动跳回登录页（GET /init/status 兜底）。
const router = useRouter()
const form = reactive({
  username: '',
  password: '',
  realName: '',
  age: '',
  email: '',
  phone: ''
})
const loading = ref(false)
const error = ref('')
const checking = ref(true)

onMounted(async () => {
  try {
    const initialized = await authApi.initStatus()
    if (initialized) {
      toast.info('系统已初始化，请直接登录')
      router.replace('/login')
      return
    }
  } catch (e) {
    // 接口失败不阻塞，直接展示表单（提交时后端会再兜底）
  } finally {
    checking.value = false
  }
})

async function submit() {
  error.value = ''
  if (!form.username || !form.password) {
    error.value = '请填写用户名和密码'
    return
  }
  if (!form.email && !form.phone) {
    error.value = '邮箱与手机号至少填一个'
    return
  }
  loading.value = true
  try {
    await authApi.initAdmin({
      username: form.username,
      password: form.password,
      realName: form.realName || null,
      age: form.age ? Number(form.age) : null,
      email: form.email || null,
      phone: form.phone || null
    })
    toast.success('初始管理员创建成功，请登录')
    router.replace('/login')
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
        <h1 class="auth-title">初始化系统</h1>
        <p class="auth-sub">首次运行，请创建初始管理员账号</p>
      </div>
      <form class="col" @submit.prevent="submit">
        <BaseInput v-model="form.username" label="用户名" placeholder="请输入用户名" autocomplete="username" />
        <BaseInput v-model="form.password" label="密码" type="password" placeholder="含大小写/数字/符号至少三类" autocomplete="new-password" />
        <BaseInput v-model="form.realName" label="真实姓名（选填）" placeholder="管理员可不填" />
        <BaseInput v-model="form.age" label="年龄（选填）" type="number" placeholder="管理员可不填" />
        <BaseInput v-model="form.email" label="邮箱" placeholder="选填，与手机号至少一个" />
        <BaseInput v-model="form.phone" label="手机号" placeholder="选填，11 位大陆手机号" />
        <p v-if="error" class="auth-error">{{ error }}</p>
        <BaseButton block :loading="loading" type="submit">创建初始管理员</BaseButton>
      </form>
      <p class="auth-foot">
        已初始化？<RouterLink to="/login">直接登录</RouterLink>
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
</style>
