<script setup>
import { useToastStore } from '@/stores/toastStore';

const toastStore = useToastStore();

function handleAction(toast) {
  if (toast.action?.onClick) {
    toast.action.onClick();
  }
  toastStore.removeToast(toast.id);
}
</script>

<template>
  <div class="fixed top-22 left-1/2 z-[100] flex flex-col gap-2">
    <div
      v-for="toast in toastStore.toasts"
      :key="toast.id"
      class="flex items-center gap-3 px-4 py-3 bg-white text-text-main text-[14px] rounded-xl shadow-lg border border-surface-gray max-w-[360px]"
    >
      <span class="flex-1">{{ toast.message }}</span>

      <!-- 버튼 (action 있을 때만) -->
      <button
        v-if="toast.action"
        @click="handleAction(toast)"
        class="shrink-0 px-3 py-1.5 bg-text-main text-white text-[13px] rounded-lg"
      >
        {{ toast.action.label }}
      </button>
    </div>
  </div>
</template>
