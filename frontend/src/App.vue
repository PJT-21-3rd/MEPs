<template>
  <div class="w-screen h-screen overflow-hidden">
    <RouterView />
    <ToastContainer />
    <LoginModal v-if="authStore.isLoginModalOpen" @close="authStore.closeLoginModal" />
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router';
import ToastContainer from './components/common/ToastContainer.vue';
import { useAuthStore } from '@/stores/authStore';
import LoginModal from '@/components/auth/LoginModal.vue';
const authStore = useAuthStore();
</script>
