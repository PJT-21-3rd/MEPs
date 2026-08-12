<template>
  <div
    class="relative inline-flex flex-col items-start drop-shadow-[0_2px_4px_rgba(255,206,0,0.5)]"
  >
    <!-- 말풍선 본체 -->
    <div
      class="relative z-10 bg-secondary rounded-[6px_6px_6px_0px] px-[10px] pb-[5px] pt-[6px] ring-1 ring-inset ring-secondary"
    >
      <!-- 주용도 -->
      <p class="text-[10px] font-semibold text-primary leading-tight">{{ purpose }}</p>
      <!-- 연면적 -->
      <p class="mt-0.5 text-[14px] font-bold text-text-main leading-tight">{{ area }}</p>
    </div>

    <!-- 꼬리 -->
    <svg
      class="relative z-0 -mt-[2px] h-[7px] w-[9px] text-secondary"
      viewBox="0 0 9 7"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path d="M0 0H9L8 0C5 4 2 6 0 6V0Z" class="fill-secondary" />

      <path d="M0 0V6C2 6 5 4 8 0" stroke="currentColor" stroke-width="1" stroke-linecap="square" />
    </svg>
  </div>
</template>

<script setup>
defineProps({
  purpose: { type: String, default: '' },
  area: { type: String, default: '' },
});
</script>
