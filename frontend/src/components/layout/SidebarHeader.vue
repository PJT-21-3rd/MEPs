<template>
  <header class="flex items-center justify-between p-6 pb-3">
    <div class="flex items-center gap-2">
      <div class="bg-surface-gray rounded-xl">
        <img src="@/assets/images/MEPS_LOGO.png" alt="meps_logo" class="w-10 h-10 object-contain" />
      </div>

      <h1 class="text-xl font-semibold">MEPS</h1>
    </div>

    <div class="flex items-center gap-1 text-text-sub">
      <button
        @click="handleMyPageClick"
        class="p-2.5 rounded-full hover:bg-surface-gray hover:text-text-main/70"
        aria-label="마이페이지"
      >
        <FolderHeart class="w-5.5 h-5.5" />
      </button>

      <div ref="profileRef" class="relative">
        <button
          @click="handleUserClick"
          class="p-2.5 rounded-full hover:bg-surface-gray hover:text-text-main/70"
          aria-label="유저 메뉴"
        >
          <UserRound class="w-5.5 h-5.5" />
        </button>

        <Transition
          enter-active-class="transition duration-150 ease-out"
          enter-from-class="transform scale-95 opacity-0 -translate-y-2"
          enter-to-class="transform scale-100 opacity-100 translate-y-0"
          leave-active-class="transition duration-100 ease-in"
          leave-from-class="transform scale-100 opacity-100 translate-y-0"
          leave-to-class="transform scale-95 opacity-0 -translate-y-2"
        >
          <div
            v-if="authStore.isLoggedIn && isProfileOpen"
            class="absolute right-0 top-12 z-50 origin-top-right"
          >
            <ProfileDropdown @logout="handleLogout" @delete-account="handleDeleteAccount" />
          </div>
        </Transition>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { FolderHeart, UserRound } from '@lucide/vue';
import ProfileDropdown from '../auth/ProfileDropdown.vue';
import { useClickOutside } from '@/hooks/useClickOutside.js';
import { useAuthStore } from '@/stores/authStore';
import { useToastStore } from '@/stores/toastStore';
import { deleteAccount } from '@/api/auth.js';

const router = useRouter();
const authStore = useAuthStore();
const toastStore = useToastStore();

const isProfileOpen = ref(false);
const profileRef = ref(null);

useClickOutside(profileRef, () => {
  isProfileOpen.value = false;
});

const handleMyPageClick = () => {
  if (!authStore.isLoggedIn) {
    toastStore.showToast('마이페이지는 로그인 후 이용할 수 있어요.', {
      action: {
        label: '로그인',
        onClick: () => router.push({ name: 'Login', query: { redirect: '/mypage' } }),
      },
    });

    return;
  }
  router.push('/mypage');
};

const handleUserClick = () => {
  if (authStore.isLoggedIn) {
    isProfileOpen.value = !isProfileOpen.value;
  } else {
    authStore.openLoginModal();
  }
};

const handleLogout = () => {
  authStore.logout();
  isProfileOpen.value = false;
  toastStore.showToast('로그아웃 되었습니다.');
  router.push('/');
};

const handleDeleteAccount = async () => {
  try {
    await deleteAccount();
    authStore.logout();
    isProfileOpen.value = false;
    toastStore.showToast('계정이 삭제되었습니다.');
    router.push('/');
  } catch (error) {
    if (error.response?.status === 401) {
      toastStore.showToast('로그인이 필요합니다.');
    } else {
      toastStore.showToast('계정 삭제에 실패했습니다.');
    }
  }
};
</script>
