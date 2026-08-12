import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || null);
  const email = ref(localStorage.getItem('email') || null);
  const isLoggedIn = computed(() => !!accessToken.value);

  // 로그인 모달 상태
  const isLoginModalOpen = ref(false);
  const openLoginModal = () => {
    isLoginModalOpen.value = true;
  };
  const closeLoginModal = () => {
    isLoginModalOpen.value = false;
  };

  function setToken(token, userEmail) {
    accessToken.value = token;
    localStorage.setItem('accessToken', token);
    if (userEmail) {
      email.value = userEmail;
      localStorage.setItem('email', userEmail);
    }
  }

  function logout() {
    accessToken.value = null;
    email.value = null;
    localStorage.removeItem('accessToken');
    localStorage.removeItem('email');
  }

  return {
    accessToken,
    isLoggedIn,
    isLoginModalOpen,
    email,
    openLoginModal,
    closeLoginModal,
    setToken,
    logout,
  };
});
