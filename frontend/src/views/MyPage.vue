<script setup>
import { ArrowLeft, Scale } from '@lucide/vue';
import { favoriteBuildings } from '@/mocks/favorites';
import { ref, computed } from 'vue';
import FavoriteCard from '@/components/mypage/FavoriteCard.vue';
import ComparePanel from '@/components/mypage/ComparePanel.vue';

const MAX_SELECT = 3;

const selectedIds = ref([]);

const guideText = computed(() => {
  const count = selectedIds.value.length;
  if (count === 0) return '비교할 매물을 선택하세요';
  if (count === 1) return '하나 더 선택해주세요';
  return '비교할 매물이 선택되었어요';
});

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

function selectOrder(id) {
  const index = selectedIds.value.indexOf(id);
  return index === -1 ? null : index + 1;
}
</script>

<template>
  <div class="flex gap-6 p-4 h-full">
    <!-- 왼쪽: 사이드바 (헤더 + 안내 + 찜 리스트) -->
    <aside class="w-[380px] shrink-0">
      <header class="flex items-start gap-2 mb-4">
        <ArrowLeft :size="22" class="mt-0.5" />
        <div>
          <h1 class="text-lg font-bold m-0">마이페이지</h1>
          <p class="text-[13px] text-text-sub mt-0.5">찜한 매물 {{ favoriteBuildings.length }}개</p>
        </div>
      </header>

      <p class="flex items-center gap-2 text-[15px] text-primary mt-8 mb-2">
        <Scale :size="18" />
        {{ guideText }}
      </p>

      <ul class="list-none p-0 m-0 flex flex-col gap-2.5">
        <FavoriteCard
          v-for="building in favoriteBuildings"
          :key="building.id"
          :building="building"
          :order="selectOrder(building.id)"
          @toggle="toggleSelect(building.id)"
        />
      </ul>
    </aside>

    <ComparePanel />
  </div>
</template>
