<script setup>
import { ref, onMounted } from 'vue';
import ComparePanel from '@/components/mypage/ComparePanel.vue';
import FavoriteSidebar from '@/components/mypage/FavoriteSidebar.vue';
import { getSavedList, removeSaved } from '@/api/saved';
import { useToastStore } from '@/stores/toastStore';
const toastStore = useToastStore();

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

async function removeFavorite(id) {
  try {
    await removeSaved(id);
    const index = buildings.value.findIndex((b) => b.buildingId === id);
    if (index !== -1) buildings.value.splice(index, 1);
    const selIndex = selectedIds.value.indexOf(id);
    if (selIndex !== -1) selectedIds.value.splice(selIndex, 1);
    toastStore.showToast('찜 목록에서 삭제했어요.');
  } catch (error) {
    console.error('찜 취소 실패:', error);
    toastStore.showToast('찜 취소에 실패했어요.'); // 실패 알림
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
