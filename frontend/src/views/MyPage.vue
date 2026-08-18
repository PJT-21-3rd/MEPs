<script setup>
import { ref, onMounted } from 'vue';
import ComparePanel from '@/components/mypage/ComparePanel.vue';
import FavoriteSidebar from '@/components/mypage/FavoriteSidebar.vue';
import { getSavedList } from '@/api/saved';

const buildings = ref([]);
onMounted(async () => {
  try {
    buildings.value = await getSavedList();
  } catch (error) {
    console.error('찜 목록 조회 실패:', error);
  }
});

const MAX_SELECT = 3;

const selectedIds = ref([]);

function toggleSelect(buildingId) {
  const index = selectedIds.value.indexOf(buildingId);

  if (index !== -1) {
    selectedIds.value.splice(index, 1);
  } else {
    if (selectedIds.value.length >= MAX_SELECT) {
      selectedIds.value.shift();
    }
    selectedIds.value.push(buildingId);
  }
}

function removeFavorite(id) {
  const index = buildings.value.findIndex((b) => b.buildingId === id);
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
