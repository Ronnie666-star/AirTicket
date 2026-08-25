<script setup>
// 空状态占位：图标 + 文案 + 可选操作。
defineProps({
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '' },
  emoji: { type: String, default: '🗂️' }
})
</script>

<template>
  <div class="empty">
    <div class="empty-icon">{{ emoji }}</div>
    <h3 class="empty-title">{{ title }}</h3>
    <p v-if="description" class="empty-desc">{{ description }}</p>
    <div v-if="$slots.default" class="empty-action">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-8) var(--space-5);
  text-align: center;
  gap: var(--space-3);
}
.empty-icon { font-size: 44px; opacity: 0.6; }
.empty-title { font-size: var(--text-lg); font-weight: 600; }
.empty-desc { color: var(--color-text-secondary); font-size: var(--text-sm); }
.empty-action { margin-top: var(--space-2); }
</style>
