<template>
  <div
    class="relative inline-flex flex-col items-start transition-all duration-200 ease-out origin-bottom cursor-pointer hover:scale-110 hover:-translate-y-1 hover:z-50"
    :class="theme.shadow"
  >
    <div
      v-if="isSaved || isRecent"
      class="absolute -top-2 -right-2 z-20 flex h-[18px] w-[18px] items-center justify-center rounded-full bg-white shadow-sm ring-1"
      :class="isSaved ? 'ring-status-like' : 'ring-button-primary'"
    >
      <Heart v-if="isSaved" :size="10" class="fill-status-like text-status-like" />
      <Clock v-else-if="isRecent" :size="10" class="text-button-primary" />
    </div>

    <div
      class="relative z-10 rounded-[6px_6px_6px_0px] px-[10px] pb-[5px] pt-[6px] ring-1 ring-inset transition-colors duration-200"
      :class="theme.bg"
    >
      <!-- 주용도 -->
      <p class="text-[10px] font-semibold leading-tight" :class="theme.subText">
        {{ purpose }}
      </p>
      <!-- 연면적 -->
      <p class="mt-0.5 text-[14px] font-bold leading-tight" :class="theme.mainText">
        {{ area }}
      </p>
    </div>

    <svg
      class="relative z-0 -mt-[2px] h-[7px] w-[9px] transition-colors duration-200"
      :class="theme.tail"
      viewBox="0 0 9 7"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path d="M0 0H9L8 0C5 4 2 6 0 6V0Z" :class="theme.tailFill" />
      <path d="M0 0V6C2 6 5 4 8 0" stroke="currentColor" stroke-width="1" stroke-linecap="square" />
    </svg>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { Heart, Clock } from '@lucide/vue';

const props = defineProps({
  purpose: { type: String, default: '' },
  area: { type: String, default: '' },
  isSaved: { type: Boolean, default: false },
  isRecent: { type: Boolean, default: false },
});

const theme = computed(() => {
  // 찜
  if (props.isSaved) {
    return {
      shadow: 'drop-shadow-[0_2px_4px_rgba(244,63,94,0.4)]',
      bg: 'bg-status-like ring-status-like',
      subText: 'text-rose-100',
      mainText: 'text-white',
      tail: 'text-status-like',
      tailFill: 'fill-status-like',
    };
  }

  // 최근 본 곳
  if (props.isRecent) {
    return {
      shadow: 'drop-shadow-[0_2px_4px_rgba(59,130,246,0.4)]',
      bg: 'bg-button-primary ring-button-primary',
      subText: 'text-white',
      mainText: 'text-white',
      tail: 'text-button-primary',
      tailFill: 'fill-button-primary',
    };
  }

  // 기본
  return {
    shadow: 'drop-shadow-[0_2px_4px_rgba(255,206,0,0.5)]',
    bg: 'bg-secondary ring-secondary',
    subText: 'text-primary',
    mainText: 'text-text-main',
    tail: 'text-secondary',
    tailFill: 'fill-secondary',
  };
});
</script>
