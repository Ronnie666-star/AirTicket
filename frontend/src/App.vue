<script setup>
// 应用外壳：全局导航 + 路由出口 + 轻提示容器。
import AppNav from './components/AppNav.vue'
import Toast from './components/Toast.vue'
</script>

<template>
  <AppNav />
  <main class="container page">
    <RouterView />
  </main>
  <Toast />
</template>
