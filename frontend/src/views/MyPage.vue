<script setup>
import { Heart, ArrowLeft, Scale } from '@lucide/vue';
import { favoriteBuildings } from '@/mocks/favorites';

function gradeClass(grade) {
  if (grade === '안전') return 'bg-grade-safe text-text-safe';
  if (grade === '양호') return 'bg-grade-good text-text-good';
  if (grade === '주의') return 'bg-grade-warn text-text-warn';
  return '';
}

function scoreColorClass(score) {
  if (score >= 90) return 'text-text-safe';
  if (score >= 80) return 'text-text-good';
  if (score >= 70) return 'text-text-warn';
  return '';
}
</script>

<template>
  <div class="py-5 px-4 max-w-[380px]">
    <header class="flex items-start gap-2 mb-4">
      <ArrowLeft :size="22" class="mt-0.5" />
      <div>
        <h1 class="text-lg font-bold m-0">마이페이지</h1>
        <p class="text-[13px] text-text-sub mt-0.5">찜한 매물 {{ favoriteBuildings.length }}개</p>
      </div>
    </header>
    <p class="flex items-center gap-2 text-[15px] text-primary mt-8 mb-2">
      <Scale :size="18" />
      비교할 매물 2개를 선택하세요
    </p>

    <ul class="list-none p-0 m-0 flex flex-col gap-2.5">
      <li
        v-for="building in favoriteBuildings"
        :key="building.id"
        class="flex items-center gap-3 py-3.5 px-4 border border-surface-gray rounded-xl"
      >
        <span class="shrink-0 w-5 h-5 border-2 border-surface-gray rounded-full"></span>

        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-1.5">
            <span class="text-[15px] font-bold">{{ building.name }}</span>
            <span class="text-[11px] px-[7px] py-0.5 rounded" :class="gradeClass(building.grade)">
              {{ building.grade }}
            </span>
          </div>
          <p class="text-xs text-text-sub mt-[3px]">{{ building.address }}</p>
        </div>

        <div class="flex items-center gap-2.5 shrink-0">
          <span class="text-[17px] font-bold" :class="scoreColorClass(building.score)">
            {{ building.score }}
          </span>
          <Heart :size="18" fill="currentColor" class="text-status-like" />
        </div>
      </li>
    </ul>
  </div>
</template>
