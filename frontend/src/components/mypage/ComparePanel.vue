<script setup>
import { ref, watch } from 'vue';
import { Scale, Sparkles, LandPlot, Building2, Layers } from '@lucide/vue';
import { getCompareData } from '@/api/building.js';
import CompareBasicInfo from './CompareBasicInfo.vue';
import CompareAiReport from './CompareAiReport.vue';
import CompareTable from './CompareTable.vue';
const props = defineProps({
  selectedIds: Array,
});
const compareBuildings = ref([]);
watch(
  () => props.selectedIds,
  async (ids) => {
    console.log('선택된 ids:', ids, ids?.length);
    if (!ids || ids.length < 2) {
      compareBuildings.value = [];
      return;
    }
    try {
      const raw = await getCompareData(ids);
      console.log('받은 데이터:', raw);
      compareBuildings.value = raw;
    } catch (error) {
      console.error('비교 데이터 조회 실패:', error);
      compareBuildings.value = [];
    }
  },
  { immediate: true, deep: true },
);

const sections = [
  { key: 'report', label: 'AI 안전진단', icon: Sparkles },
  { key: 'land', label: '토지 정보', icon: LandPlot },
  { key: 'building', label: '건축물 정보', icon: Building2 },
  { key: 'floor', label: '층별 정보', icon: Layers },
];

const activeSections = ref(['report', 'land', 'building', 'floor']);

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
</script>

<template>
  <section class="flex-1 min-w-0 flex flex-col h-full pt-4">
    <!-- 제목 + 섹션 표시 개수 + 섹션 토글 -->
    <div class="pb-4 border-b border-surface-gray px-6">
      <div class="flex items-center justify-between mb-3">
        <h2 class="flex items-center gap-2 text-base font-bold">
          <Scale :size="18" class="text-primary" />
          찜한 매물 비교
        </h2>
        <span class="text-[13px] text-text-sub">
          {{ activeSections.length }}/{{ sections.length }}개 섹션 표시
        </span>
      </div>

      <div class="flex flex-wrap gap-2">
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
    </div>
    <!-- 스크롤 영역 -->
    <div class="flex-1 overflow-y-auto px-6 flex flex-col bg-surface-base">
      <!-- 2개 미만일 때 안내 문구 -->
      <div
        v-if="compareBuildings.length < 2"
        class="flex-1 flex flex-col items-center justify-center text-text-sub"
      >
        <Building2 :size="40" class="mb-3 opacity-40" />
        <p class="text-[15px]">
          {{ compareBuildings.length === 0 ? '비교할 매물을 선택하세요' : '하나 더 선택해주세요' }}
        </p>
        <p class="text-[13px] mt-1 opacity-70">
          왼쪽 찜 목록에서 매물을 선택하면 상세 조건이 비교됩니다
        </p>
      </div>

      <!-- 비교 뷰 -->

      <div v-else class="pt-4 pb-6">
        <CompareBasicInfo :buildings="compareBuildings" />
        <CompareAiReport :buildings="compareBuildings" :activeSections="activeSections" />
        <CompareTable :buildings="compareBuildings" :activeSections="activeSections" />
      </div>
    </div>
  </section>
</template>
