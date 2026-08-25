<script setup>
// 骨架屏：占位加载态。rows 控制行数，card 为卡片块。
defineProps({
  rows: { type: Number, default: 3 },
  card: { type: Boolean, default: false }
})
</script>

<template>
  <div v-if="card" class="sk-card">
    <div v-for="n in rows" :key="n" class="sk-line" :style="{ width: `${100 - n * 12}%` }" />
  </div>
  <div v-else class="sk-list">
    <div v-for="n in rows" :key="n" class="sk-item">
      <div class="sk-avatar" />
      <div class="sk-body">
        <div class="sk-line w60" />
        <div class="sk-line w35" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.sk-line {
  height: 14px;
  border-radius: var(--radius-sm);
  background: linear-gradient(90deg, #ececf1 25%, #f5f5f7 50%, #ececf1 75%);
  background-size: 200% 100%;
  animation: shimmer 1.4s ease infinite;
  margin-bottom: var(--space-3);
}
.sk-card { padding: var(--space-5); background: var(--color-surface); border-radius: var(--radius-md); box-shadow: var(--shadow-sm); }
.sk-list { display: flex; flex-direction: column; gap: var(--space-3); }
.sk-item { display: flex; gap: var(--space-4); padding: var(--space-4); background: var(--color-surface); border-radius: var(--radius-md); box-shadow: var(--shadow-sm); }
.sk-avatar { width: 40px; height: 40px; border-radius: 50%; background: #ececf1; animation: pulse 1.4s ease infinite; }
.sk-body { flex: 1; display: flex; flex-direction: column; gap: var(--space-2); }
.w60 { width: 60%; }
.w35 { width: 35%; }
@keyframes shimmer { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }
</style>
