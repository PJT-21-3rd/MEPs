<script setup>
import { ref } from 'vue';
import { Heart } from '@lucide/vue';
import { addSaved } from '@/api/saved';
import { useAuthStore } from '@/stores/authStore';
import { useToastStore } from '@/stores/toastStore';
import { useRouter } from 'vue-router';

const props = defineProps({
  buildingId: { type: [String, Number], required: true },
  initialSaved: { type: Boolean, default: false },
});

const authStore = useAuthStore();
const isSaved = ref(props.initialSaved);

const toastStore = useToastStore();
const router = useRouter();

async function handleClick() {
  // 비로그인 → 로그인 유도
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

  if (isSaved.value) return;

  try {
    await addSaved(props.buildingId);
    isSaved.value = true; // 하트 채우기
  } catch (error) {
    if (error.response?.status === 409) {
      isSaved.value = true; // 이미 찜된 거니 하트 채움
    } else if (error.response?.status === 401) {
      authStore.openLoginModal();
    } else {
      console.error('찜하기 실패:', error);
    }
  }
}
</script>

<template>
  <button @click.stop="handleClick" aria-label="찜하기">
    <Heart :size="20" :class="isSaved ? 'fill-status-like text-status-like' : 'text-text-sub'" />
  </button>
</template>
