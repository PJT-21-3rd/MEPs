<script setup>
import { ArrowLeft, Scale, Sparkles, Info, LandPlot, Building2, Layers } from '@lucide/vue';
import { favoriteBuildings } from '@/mocks/favorites';
import { ref, computed } from 'vue';
import FavoriteCard from '@/components/mypage/FavoriteCard.vue';

const MAX_SELECT = 3;
const selectedIds = ref([]);
const guideText = computed(() => {
  const count = selectedIds.value.length;
  if (count === 0) return '비교할 매물을 선택하세요';
  if (count === 1) return '하나 더 선택해주세요';
  return '비교할 매물이 선택되었어요';
});
const sections = [
  { key: 'report', label: 'AI 안전진단', icon: Sparkles },
  { key: 'basic', label: '기본 정보', icon: Info },
  { key: 'land', label: '토지 정보', icon: LandPlot },
  { key: 'building', label: '건축물 정보', icon: Building2 },
  { key: 'floor', label: '층별 정보', icon: Layers },
];
const activeSections = ref(['report', 'basic', 'land', 'building', 'floor']);

function toggleSection(key) {
  const index = activeSections.value.indexOf(key);

  if (index !== -1) {
    if (activeSections.value.length === 1) return;
    activeSections.value.splice(index, 1);
  } else {
    activeSections.value.push(key);
  }
}

function isActive(key) {
  return activeSections.value.includes(key);
}

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

    <!-- 오른쪽: 비교 영역 (섹션 토글 + 비교 뷰) -->
    <section class="flex-1 min-w-0">
      <div class="flex flex-wrap gap-2 mb-4">
        <button
          v-for="section in sections"
          :key="section.key"
          @click="toggleSection(section.key)"
          class="flex items-center gap-1.5 px-3 py-2 rounded-full text-[13px] font-medium border transition-colors"
          :class="
            isActive(section.key)
              ? 'bg-primary text-white border-primary'
              : 'bg-surface-gray text-text-sub border-surface-gray'
          "
        >
          <component :is="section.icon" :size="15" />
          {{ section.label }}
        </button>
      </div>
    </section>
  </div>
</template>
