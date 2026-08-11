import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || null);
  const isLoggedIn = computed(() => !!accessToken.value);

  function setToken(token) {
    accessToken.value = token;
    localStorage.setItem('accessToken', token);
  }

  function logout() {
    accessToken.value = null;
    localStorage.removeItem('accessToken');
  }

  return { accessToken, isLoggedIn, setToken, logout };
});
