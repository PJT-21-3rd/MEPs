import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || null);
  const isLoggedIn = computed(() => !!accessToken.value);

  // 로그인 모달 상태
  const isLoginModalOpen = ref(false);
  const openLoginModal = () => {
    isLoginModalOpen.value = true;
  };
  const closeLoginModal = () => {
    isLoginModalOpen.value = false;
  };

  function setToken(token) {
    accessToken.value = token;
    localStorage.setItem('accessToken', token);
  }

  function logout() {
    accessToken.value = null;
    localStorage.removeItem('accessToken');
  }

  return {
    accessToken,
    isLoggedIn,
    isLoginModalOpen,
    openLoginModal,
    closeLoginModal,
    setToken,
    logout,
  };
});
