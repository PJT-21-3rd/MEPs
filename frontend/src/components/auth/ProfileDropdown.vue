<script setup>
import { Mail, LogOut, Trash2 } from '@lucide/vue';
import { useAuthStore } from '@/stores/authStore';

const emit = defineEmits(['logout', 'delete-account']);

const authStore = useAuthStore();
</script>

<template>
  <div class="w-[240px] bg-white rounded-xl shadow-lg border border-surface-gray py-2">
    <!-- 로그인된 계정 -->
    <p class="text-[13px] text-text-sub px-4 pt-1 pb-2">로그인된 계정</p>
    <div class="flex items-center gap-2 px-4 pb-3 border-b border-surface-gray">
      <div class="w-8 h-8 rounded-full bg-surface-blue flex items-center justify-center shrink-0">
        <Mail :size="16" class="text-primary" />
      </div>
      <span class="text-[14px] font-medium truncate">{{ authStore.email }}</span>
    </div>

    <!-- 로그아웃 -->
    <button
      @click="emit('logout')"
      class="w-full flex items-center gap-2 px-4 py-2.5 text-[14px] hover:bg-surface-gray"
    >
      <LogOut :size="16" />
      로그아웃
    </button>

    <!-- 계정 삭제 -->
    <button
      @click="emit('delete-account')"
      class="w-full flex items-center gap-2 px-4 py-2.5 text-[14px] text-status-danger hover:bg-surface-gray"
    >
      <Trash2 :size="16" />
      계정 삭제
    </button>
  </div>
</template>
