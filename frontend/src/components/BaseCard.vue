<script setup>
// 卡片：白底、圆角、柔和阴影。用于列表项 / 表单容器。
defineProps({
  padding: { type: String, default: 'md' },  // sm | md | lg | none
  hoverable: { type: Boolean, default: false },
  clickable: { type: Boolean, default: false }
})
</script>

<template>
  <div
    class="card"
    :class="[`card-pad-${padding}`, { 'card-hover': hoverable || clickable, 'card-click': clickable }]"
  >
    <slot />
  </div>
</template>

<style scoped>
.card {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(0, 0, 0, 0.04);
}
.card-pad-sm { padding: var(--space-3); }
.card-pad-md { padding: var(--space-5); }
.card-pad-lg { padding: var(--space-6); }
.card-pad-none { padding: 0; }
.card-hover { transition: var(--transition-base), transform var(--transition-base); }
.card-hover:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.card-click { cursor: pointer; }
</style>
