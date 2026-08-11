<template>
  <div class="w-screen h-screen overflow-hidden">
    <RouterView />
    <ToastContainer />
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router';
import ToastContainer from './components/common/ToastContainer.vue';
</script>
