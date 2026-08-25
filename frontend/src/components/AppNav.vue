<script setup>
// 玻璃态顶部导航：登录态按角色显示入口，未登录显示登录/注册。
import { useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { toast } from '../composables/useToast'

const route = useRoute()
const auth = useAuthStore()

const menu = [
  { label: '搜航班', name: 'search', roles: null },
  { label: '实时轨迹', name: 'tracking', roles: null },
  { label: '我的订单', name: 'orders', roles: ['PASSENGER', 'MERCHANT', 'ADMIN'] },
  { label: '个人中心', name: 'profile', roles: ['PASSENGER', 'MERCHANT', 'ADMIN'] },
  { label: '用户管理', name: 'admin-users', roles: ['ADMIN'] },
  { label: '基础数据', name: 'admin-master', roles: ['ADMIN'] },
  { label: '放票管理', name: 'admin-flights', roles: ['MERCHANT', 'ADMIN'] }
]

function visible(item) {
  return !item.roles || auth.user?.role && item.roles.includes(auth.user.role)
}

function logout() {
  auth.logout()
  toast.success('已退出登录')
}
</script>

<template>
  <nav class="nav">
    <div class="container nav-inner">
      <RouterLink to="/" class="brand">✈️ AirTicket</RouterLink>
      <div class="nav-links">
        <RouterLink
          v-for="item in menu.filter(visible)"
          :key="item.name"
          :to="{ name: item.name }"
          class="nav-link"
          :class="{ active: route.name === item.name }"
        >
          {{ item.label }}
        </RouterLink>
      </div>
      <div class="nav-right">
        <template v-if="auth.isLoggedIn">
          <span class="nav-user">{{ auth.user.realName || auth.user.username }}</span>
          <button class="nav-logout" @click="logout">退出</button>
        </template>
        <template v-else>
          <RouterLink to="/login" class="nav-link">登录</RouterLink>
          <RouterLink to="/register" class="nav-register">注册</RouterLink>
        </template>
      </div>
    </div>
  </nav>
</template>

<style scoped>
.nav {
  position: sticky;
  top: 0;
  z-index: 50;
  background: var(--color-glass);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}
.nav-inner {
  height: 56px;
  display: flex;
  align-items: center;
  gap: var(--space-5);
}
.brand {
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.02em;
  white-space: nowrap;
}
.nav-links {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex: 1;
  overflow-x: auto;
}
.nav-link {
  color: var(--color-text-secondary);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  white-space: nowrap;
  transition: var(--transition-fast);
}
.nav-link:hover { color: var(--color-text); background: rgba(0, 0, 0, 0.04); }
.nav-link.active { color: var(--color-accent); font-weight: 600; }
.nav-right { display: flex; align-items: center; gap: var(--space-3); }
.nav-user { font-size: var(--text-sm); color: var(--color-text-secondary); white-space: nowrap; }
.nav-logout {
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: var(--text-sm);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
}
.nav-logout:hover { color: var(--color-danger); background: rgba(255, 59, 48, 0.08); }
.nav-register {
  background: var(--color-accent);
  color: #fff;
  padding: 6px 16px;
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
}
.nav-register:hover { color: #fff; background: var(--color-accent-hover); }

@media (max-width: 640px) {
  .nav-inner { gap: var(--space-3); }
  .nav-user { display: none; }
}
</style>
