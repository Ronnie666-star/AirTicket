<script setup>
// 输入框：Apple 风格，圆角、浅灰底、聚焦变白+品牌蓝描边。
import { useAttrs } from 'vue'
defineProps({
  label: { type: String, default: '' },
  modelValue: { type: [String, Number], default: '' },
  placeholder: { type: String, default: '' },
  type: { type: String, default: 'text' },
  error: { type: String, default: '' }
})
const attrs = useAttrs()
const emit = defineEmits(['update:modelValue'])
function onInput(e) {
  emit('update:modelValue', e.target.value)
}
</script>

<template>
  <div class="field">
    <label v-if="label" class="field-label">{{ label }}</label>
    <input
      class="input"
      :class="{ 'has-error': error }"
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      v-bind="attrs"
      @input="onInput"
    />
    <p v-if="error" class="field-error">{{ error }}</p>
  </div>
</template>

<style scoped>
.field { display: flex; flex-direction: column; gap: var(--space-2); }
.field-label { font-size: var(--text-sm); font-weight: 500; color: var(--color-text-secondary); }
.input {
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: var(--color-surface-2);
  font-family: inherit;
  font-size: var(--text-base);
  color: var(--color-text);
  transition: var(--transition-fast);
  outline: none;
}
.input:focus {
  background: var(--color-surface);
  border-color: var(--color-accent);
  box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.15);
}
.input::placeholder { color: var(--color-text-tertiary); }
.input.has-error { border-color: var(--color-danger); }
.field-error { font-size: var(--text-sm); color: var(--color-danger); }
</style>
