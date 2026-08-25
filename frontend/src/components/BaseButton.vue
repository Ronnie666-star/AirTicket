<script setup>
// 按钮：主/次/危险/幽灵 四态，sm/md 两档。Apple 风格：胶囊圆角、克制填充。
defineProps({
  variant: { type: String, default: 'primary' },   // primary | secondary | danger | ghost
  size: { type: String, default: 'md' },            // sm | md
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  block: { type: Boolean, default: false }
})
</script>

<template>
  <button
    class="btn"
    :class="[`btn-${variant}`, `btn-${size}`, { 'btn-block': block, 'is-loading': loading }]"
    :disabled="disabled || loading"
  >
    <span v-if="loading" class="spinner" aria-hidden="true" />
    <slot />
  </button>
</template>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  border: none;
  border-radius: var(--radius-full);
  font-family: inherit;
  font-size: var(--text-base);
  cursor: pointer;
  transition: var(--transition-fast);
  white-space: nowrap;
  user-select: none;
}
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.btn-sm { padding: 6px 14px; font-size: var(--text-sm); }
.btn-md { padding: 10px 22px; }

.btn-primary { background: var(--color-accent); color: #fff; }
.btn-primary:hover:not(:disabled) { background: var(--color-accent-hover); }
.btn-secondary { background: var(--color-surface-2); color: var(--color-text); border: 1px solid var(--color-border); }
.btn-secondary:hover:not(:disabled) { background: var(--color-surface); }
.btn-danger { background: var(--color-danger); color: #fff; }
.btn-danger:hover:not(:disabled) { opacity: 0.9; }
.btn-ghost { background: transparent; color: var(--color-accent); }
.btn-ghost:hover:not(:disabled) { background: rgba(0, 113, 227, 0.08); }

.btn-block { width: 100%; }

.spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
