import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || null);
  const refreshToken = ref(localStorage.getItem('refreshToken') || null);
  const email = ref(localStorage.getItem('email') || null);
  const isLoggedIn = computed(() => {
    const token = accessToken.value;
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const now = Date.now() / 1000;
      return payload.exp > now;
    } catch {
      return false;
    }
  });

  // 로그인 모달 상태
  const isLoginModalOpen = ref(false);
  const openLoginModal = () => {
    isLoginModalOpen.value = true;
  };
  const closeLoginModal = () => {
    isLoginModalOpen.value = false;
  };

  function setToken(token, newRefreshToken, userEmail) {
    accessToken.value = token;
    localStorage.setItem('accessToken', token);

    if (newRefreshToken) {
      refreshToken.value = newRefreshToken;
      localStorage.setItem('refreshToken', newRefreshToken); // 추가
    }

    if (userEmail) {
      email.value = userEmail;
      localStorage.setItem('email', userEmail);
    }
  }

  function logout() {
    accessToken.value = null;
    refreshToken.value = null;

    email.value = null;
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');

    localStorage.removeItem('email');
  }

  return {
    accessToken,
    refreshToken,
    isLoggedIn,
    isLoginModalOpen,
    email,
    openLoginModal,
    closeLoginModal,
    setToken,
    logout,
  };
});
