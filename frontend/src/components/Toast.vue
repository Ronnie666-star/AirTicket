<script setup>
// 轻提示容器：渲染 useToast 里的 toasts 列表。
import { toasts } from '../composables/useToast'
</script>

<template>
  <Teleport to="body">
    <div class="toast-wrap">
      <TransitionGroup name="toast">
        <div v-for="t in toasts" :key="t.id" class="toast" :class="`toast-${t.type}`">
          {{ t.message }}
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-wrap {
  position: fixed;
  top: var(--space-5);
  left: 50%;
  transform: translateX(-50%);
  z-index: 200;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  pointer-events: none;
}
.toast {
  background: rgba(29, 29, 31, 0.9);
  color: #fff;
  padding: 10px 20px;
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  box-shadow: var(--shadow-md);
  backdrop-filter: blur(8px);
  max-width: 80vw;
}
.toast-success { background: rgba(52, 199, 89, 0.95); }
.toast-error { background: rgba(255, 59, 48, 0.95); }

.toast-enter-active, .toast-leave-active { transition: all var(--transition-base); }
.toast-enter-from { opacity: 0; transform: translateY(-10px); }
.toast-leave-to { opacity: 0; transform: translateY(-10px); }
</style>
