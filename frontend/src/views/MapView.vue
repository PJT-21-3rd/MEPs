<template>
  <div class="w-full h-full flex overflow-hidden">
    <MainSidebar />

    <main class="flex-1 h-full relative">
      <NaverMap />

      <div v-if="uiStore.isDetailOpen" class="absolute top-5 left-6 z-20 w-[360px]">
        <SearchBar />
      </div>
    </main>
  </div>
</template>

<script setup>
import MainSidebar from '@/components/layout/MainSidebar.vue'
import NaverMap from '@/components/map/NaverMap.vue'
import SearchBar from '@/components/map/SearchBar.vue'
import { useUiStore } from '@/stores/uiStore'

const uiStore = useUiStore()
</script>
