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
    <div class="min-w-0 flex-1 flex flex-col justify-around">
      <div class="min-w-0">
        <div class="flex items-start justify-between gap-2">
          <p class="truncate text-[16px] tracking-tight text-text-main">{{ displayAddress }}</p>

          <SavedButton
            :building-id="building.buildingId"
            :initial-saved="building.saved"
            class="-mt-1.5"
          />
        </div>
        <p
          v-if="displayName"
          class="-mt-1.5 truncate text-[14px] text-text-secondary"
          hover-class="hover:bg-white/70"
        >
          {{ displayName }}
        </p>
      </div>

      <!-- 건물 스펙(준공연차, 주용도, 층수) -->
      <div class="flex items-center justify-between text-[12px] text-text-sub gap-5">
        <div class="flex items-center gap-x-0.5 gap-y-1 min-w-0 truncate pr-2">
          <span>{{ formattedYear }}</span>
          <span>·</span>
          <span>{{ building.mainPurpsNm || '용도 미정' }}</span>
          <span>·</span>
          <span>{{ formattedFloor }}</span>
        </div>
      </div>
    </div>
  </button>
</template>

<script setup>
import { computed } from 'vue';
import { Building2 } from '@lucide/vue';
import {
  formatBuildingAge,
  formatFloor,
  formatShortAddress,
  // formatDistance,
} from '@/utils/formatters';
import SavedButton from '../common/SavedButton.vue';

const props = defineProps({
  building: {
    type: Object,
    required: true,
  },
});

// 건물명 Fallback
const displayName = computed(() => {
  return props.building.bldNm || '';
});

const displayAddress = computed(() =>
  formatShortAddress(props.building.jibunAddr, props.building.roadAddr, props.building.bldNm),
);
const formattedYear = computed(() => formatBuildingAge(props.building.useAprDay));
const formattedFloor = computed(() => formatFloor(props.building.grndFlr, props.building.ugrndFlr));
// const formattedDistance = computed(() => formatDistance(props.building.distance));
</script>
