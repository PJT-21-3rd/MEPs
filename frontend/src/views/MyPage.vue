<script setup>
import { favoriteBuildings } from '@/mocks/favorites';
import { ref } from 'vue';
import ComparePanel from '@/components/mypage/ComparePanel.vue';
import FavoriteSidebar from '@/components/mypage/FavoriteSidebar.vue';

const MAX_SELECT = 3;

const selectedIds = ref([]);

function toggleSelect(id) {
  const index = selectedIds.value.indexOf(id);

  if (index !== -1) {
    selectedIds.value.splice(index, 1);
  } else {
    if (selectedIds.value.length >= MAX_SELECT) {
      selectedIds.value.shift();
    }
    selectedIds.value.push(id);
  }
}
</script>

<template>
  <div class="flex h-full overflow-hidden divide-x divide-surface-gray">
    <FavoriteSidebar
      :buildings="favoriteBuildings"
      :selectedIds="selectedIds"
      @toggle="toggleSelect"
    />

    <ComparePanel :selectedIds="selectedIds" />
  </div>
</template>
