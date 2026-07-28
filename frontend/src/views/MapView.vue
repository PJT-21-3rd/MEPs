<template>
  <div class="w-full h-full flex overflow-hidden">
    <MainSidebar />

    <main class="flex-1 h-full relative">
      <NaverMap />
    </main>
  </div>
</template>

<script setup>
import MainSidebar from '@/components/layout/MainSidebar.vue'
import NaverMap from '@/components/map/NaverMap.vue'
</script>
