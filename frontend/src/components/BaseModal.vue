<script setup>
// 模态框：遮罩 + 居中卡片，支持关闭回调。
defineProps({
  open: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: String, default: '480px' }
})
const emit = defineEmits(['close'])
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="open" class="overlay" @click.self="emit('close')">
        <div class="modal" :style="{ maxWidth: width }">
          <div class="modal-head">
            <h3 class="modal-title">{{ title }}</h3>
            <button class="modal-close" aria-label="关闭" @click="emit('close')">✕</button>
          </div>
          <div class="modal-body">
            <slot />
          </div>
          <div v-if="$slots.footer" class="modal-foot">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: var(--space-5);
  overflow-y: auto;
}
.modal {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  width: 100%;
  max-height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-5) var(--space-5) var(--space-3);
}
.modal-title { font-size: var(--text-lg); }
.modal-close {
  border: none;
  background: var(--color-surface-2);
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
}
.modal-close:hover { background: var(--color-border); }
.modal-body {
  padding: 0 var(--space-5) var(--space-5);
  overflow-y: auto;
  flex: 1 1 auto;
  min-height: 0;
}
.modal-foot { padding: var(--space-4) var(--space-5); border-top: 1px solid var(--color-border); }

.modal-enter-active, .modal-leave-active { transition: opacity var(--transition-base); }
.modal-enter-from, .modal-leave-to { opacity: 0; }
</style>
