<script setup>
// 选择器：Apple 风格下拉。
defineProps({
  label: { type: String, default: '' },
  modelValue: { type: [String, Number], default: '' },
  options: { type: Array, default: () => [] },   // [{value, label}]
  placeholder: { type: String, default: '请选择' }
})
const emit = defineEmits(['update:modelValue'])
function onChange(e) {
  emit('update:modelValue', e.target.value)
}
</script>

<template>
  <div class="field">
    <label v-if="label" class="field-label">{{ label }}</label>
    <select class="select" :value="modelValue" @change="onChange">
      <option value="" disabled>{{ placeholder }}</option>
      <option v-for="opt in options" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
    </select>
  </div>
</template>

<style scoped>
.field { display: flex; flex-direction: column; gap: var(--space-2); }
.field-label { font-size: var(--text-sm); font-weight: 500; color: var(--color-text-secondary); }
.select {
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface-2);
  font-family: inherit;
  font-size: var(--text-base);
  color: var(--color-text);
  transition: var(--transition-fast);
  outline: none;
  cursor: pointer;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='8' viewBox='0 0 12 8'%3E%3Cpath d='M1 1l5 5 5-5' stroke='%2386868b' stroke-width='2' fill='none' stroke-linecap='round'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 14px center;
  padding-right: 36px;
}
.select:focus {
  background-color: var(--color-surface);
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.15);
}
</style>
