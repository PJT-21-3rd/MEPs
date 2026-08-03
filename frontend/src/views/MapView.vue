<template>
  <div class="w-full h-full flex overflow-hidden">
    <MainSidebar />

    <main class="relative h-full flex-1 overflow-hidden">
      <NaverMap />

      <div v-if="uiStore.isDetailOpen" class="absolute top-5 left-6 z-20 w-[360px]">
        <SearchBar />
      </div>
      <div v-else><QuickNavigation /></div>

      <MapControls />
    </main>
  </div>
</template>

<script setup>
import MainSidebar from '@/components/layout/MainSidebar.vue';
import MapControls from '@/components/map/MapControls.vue';
import NaverMap from '@/components/map/NaverMap.vue';
import QuickNavigation from '@/components/map/QuickNavigation.vue';
import SearchBar from '@/components/map/SearchBar.vue';
import { useUiStore } from '@/stores/uiStore';

const uiStore = useUiStore();
</script>
