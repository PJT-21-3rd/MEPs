<script setup>
import { ref } from 'vue';
import { Heart } from '@lucide/vue';
import { addSaved, removeSaved } from '@/api/saved';
import { useAuthStore } from '@/stores/authStore';
import { useToastStore } from '@/stores/toastStore';
import { useRouter } from 'vue-router';

const props = defineProps({
  buildingId: { type: [String, Number], required: true },
  initialSaved: { type: Boolean, default: false },
  hoverClass: { type: String, default: 'hover:bg-surface-base' },
});

const authStore = useAuthStore();
const isSaved = ref(props.initialSaved);

const toastStore = useToastStore();
const router = useRouter();

async function handleClick() {
  if (!authStore.isLoggedIn) {
    toastStore.showToast('찜하기는 로그인 후 이용할 수 있어요.', {
      action: {
        label: '로그인',
        onClick: () =>
          router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } }),
      },
    });
    return;
  }

  try {
    if (isSaved.value) {
      await removeSaved(props.buildingId);
      isSaved.value = false;
    } else {
      await addSaved(props.buildingId);
      isSaved.value = true;
    }
  } catch (error) {
    const status = error.response?.status;
    if (status === 401) {
      toastStore.showToast('로그인이 필요합니다.');
    } else if (status === 409) {
      isSaved.value = true;
    } else if (status === 404) {
      console.error('존재하지 않는 건물이거나 처리할 수 없습니다:', error);
    } else {
      console.error('찜하기 처리 실패:', error);
    }
  }
}
</script>

<template>
  <button
    @click.stop="handleClick"
    aria-label="찜하기"
    :class="['rounded-full p-2 transition-colors', hoverClass]"
  >
    <Heart :size="20" :class="isSaved ? 'fill-status-like text-status-like' : 'text-text-sub'" />
  </button>
</template>
