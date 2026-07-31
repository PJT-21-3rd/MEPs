<template>
  <button
    class="flex w-full gap-3.5 rounded-2xl p-3.5 text-left transition-all bg-surface-base hover:bg-surface-gray"
  >
    <!-- 미니썸네일 -->
    <div
      class="relative flex h-[76px] w-[76px] shrink-0 items-center justify-center rounded-xl bg-surface-gray"
    >
      <Building2 :size="32" class="text-text-sub" :stroke-width="1.5" />
    </div>

    <!-- 정보(건물명, 주소, 찜) -->
    <div class="min-w-0 flex-1">
      <div class="flex items-start justify-between gap-2">
        <div class="min-w-0">
          <p class="truncate text-[16px] tracking-tight text-text-main">{{ displayAddress }}</p>
          <p v-if="displayName" class="mt-0.5 truncate text-[14px] text-text-sub">
            {{ displayName }}
          </p>
        </div>
        <span
          @click.prevent.stop="toggleFavorite"
          class="-mr-1 shrink-0 rounded-full p-1.5 transition-colors hover:bg-white/70"
        >
          <Heart
            :size="20"
            :class="isFavorite ? 'fill-status-like text-status-like' : 'text-text-secondary/50'"
          />
        </span>
      </div>

      <!-- 건물 스펙(준공연차, 주용도, 층수) -->
      <div class="mt-2.5 flex flex-wrap items-center gap-x-0.5 gap-y-1 text-[13px] text-text-sub">
        <span>{{ formattedYear }}</span>
        <span>·</span>
        <span>{{ building.mainPurpsNm || '용도 미정' }}</span>
        <span>·</span>
        <span>{{ formattedFloor }}</span>
        <span class="ml-auto text-text-sub/70">120m</span>
      </div>
    </div>
  </button>
</template>

<script setup>
import { ref, computed } from 'vue';
import { Building2, Heart } from '@lucide/vue';

const props = defineProps({
  building: {
    type: Object,
    required: true,
  },
});

// 임시 찜 기능
const isFavorite = ref(false);
const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value;
};

// 건물명 Fallback
const displayName = computed(() => {
  return props.building.bldNm || '';
});

// 주소 표시 (지번 || 도로명)
const displayAddress = computed(() => {
  const rawAddr = props.building.jibunAddr || props.building.roadAddr;

  if (rawAddr) {
    const parts = rawAddr.split(' ');

    if (parts.length >= 2 && (parts[0].endsWith('시') || parts[0].endsWith('도'))) {
      return parts.slice(1).join(' ');
    }

    return rawAddr;
  }

  return props.building.bldNm || '상세 주소 미상';
});

// 연식 포맷팅 (YYYYMMDD -> 준공 n년차)
const formattedYear = computed(() => {
  const dateStr = props.building.useAprDay;
  if (!dateStr || dateStr.length < 4) return '연식 미상';

  const year = parseInt(dateStr.substring(0, 4));
  const currentYear = new Date().getFullYear();
  const age = currentYear - year + 1; // n년차 계산

  return `준공 ${age}년차`;
});

// 층수 텍스트 포맷팅 (예: 44F/B3)
const formattedFloor = computed(() => {
  const up = props.building.grndFlr || 0;
  const down = props.building.ugrndFlr || 0;

  let floorText = '';
  floorText += `${up}F`;
  if (down > 0) floorText += `/B${down}`;

  return floorText;
});
</script>
