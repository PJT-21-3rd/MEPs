<script setup>
import { favoriteBuildings } from '@/mocks/favorites';
import { ref } from 'vue';
import ComparePanel from '@/components/mypage/ComparePanel.vue';
import FavoriteSidebar from '@/components/mypage/FavoriteSidebar.vue';

const buildings = ref([...favoriteBuildings]);

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

function removeFavorite(id) {
  const index = buildings.value.findIndex((b) => b.id === id);
  if (index !== -1) {
    buildings.value.splice(index, 1);
  }

  // 혹시 선택(비교)돼 있었으면 선택도 해제
  const selIndex = selectedIds.value.indexOf(id);
  if (selIndex !== -1) {
    selectedIds.value.splice(selIndex, 1);
  }
}
</script>

<template>
  <div class="flex h-full overflow-hidden divide-x divide-surface-gray">
    <FavoriteSidebar
      :buildings="buildings"
      :selectedIds="selectedIds"
      @toggle="toggleSelect"
      @unlike="removeFavorite"
    />

    <ComparePanel :selectedIds="selectedIds" />
  </div>
</template>
