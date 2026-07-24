<template>
  <div class="min-h-screen bg-gray-50 text-gray-900">
    <main class="h-[calc(100vh-64px)]">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
</script>
