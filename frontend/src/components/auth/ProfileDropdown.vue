<script setup>
import { Mail, LogOut, Trash2 } from '@lucide/vue';
import { useAuthStore } from '@/stores/authStore';
import { ref } from 'vue';

const emit = defineEmits(['logout', 'delete-account']);

const authStore = useAuthStore();

const showDeleteConfirm = ref(false);

function handleDeleteAccount() {
  emit('delete-account');
}
</script>

<template>
  <div @click.stop class="w-[240px] bg-white rounded-xl shadow-lg border border-surface-gray py-2">
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
      v-if="!showDeleteConfirm"
      @click="showDeleteConfirm = true"
      class="w-full flex items-center gap-2 px-4 py-2.5 text-[14px] text-status-danger hover:bg-surface-gray"
    >
      <Trash2 :size="16" />
      계정 삭제
    </button>
    <!-- 계정 삭제 확인 -->
    <div v-else class="m-2 p-3 rounded-lg bg-surface-red">
      <p class="text-[14px] font-semibold text-status-danger mb-1">정말 계정을 삭제할까요?</p>
      <p class="text-[13px] text-text-sub mb-3">찜한 매물 이력이 모두 사라지며 복구할 수 없어요.</p>
      <div class="flex gap-2">
        <button
          @click="showDeleteConfirm = false"
          class="flex-1 py-2 rounded-lg bg-white border border-surface-gray text-[14px]"
        >
          취소
        </button>
        <button
          @click="handleDeleteAccount"
          class="flex-1 py-2 rounded-lg bg-status-danger text-white text-[14px] font-semibold"
        >
          삭제
        </button>
      </div>
    </div>
  </div>
</template>
