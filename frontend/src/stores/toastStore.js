import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useToastStore = defineStore('toast', () => {
  const toasts = ref([]);
  let nextId = 0;

  function showToast(message, options = {}) {
    const id = nextId++;
    toasts.value.push({
      id,
      message,
      action: options.action || null,
    });

    // 3초 뒤 자동 제거
    setTimeout(() => {
      removeToast(id);
    }, options.duration || 3000);
  }

  function removeToast(id) {
    const index = toasts.value.findIndex((t) => t.id === id);
    if (index !== -1) {
      toasts.value.splice(index, 1);
    }
  }

  return { toasts, showToast, removeToast };
});
