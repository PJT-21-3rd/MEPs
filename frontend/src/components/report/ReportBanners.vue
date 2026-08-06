<script setup>
import { ChevronRight, ShieldCheck, Landmark } from '@lucide/vue';

// #27: 배너 노출 + 클릭 이벤트 emit까지
// 실제 모달 오픈 로직은 #94(보험)·#102(대출)에서 부모가 처리

defineEmits(['open-insurance', 'business', 'open-loan']);
</script>

<template>
  <div class="flex flex-col gap-14">
    <!-- 사업장 종합보험 배너 -->
    <button
      type="button"
      class="w-full flex items-center gap-4 px-4 py-3 rounded-2xl bg-secondary text-left"
      @click="$emit('open-insurance', 'business')"
    >
      <span class="w-9 h-9 shrink-0 rounded-[10px] bg-white flex items-center justify-center">
        <ShieldCheck class="w-[18px] h-[18px] text-secondary" />
      </span>
      <span class="flex-1 flex flex-col gap-0.5">
        <span class="text-[15px] font-bold text-primary">KB 사업장 종합보험 바로 보기</span>
        <span class="text-[13px] font-medium text-button-primary"
          >업종별 화재, 배상책임을 든든하게 보장</span
        >
      </span>
      <ChevronRight class="w-[18px] h-[18px] text-primary shrink-0" />
    </button>

    <!-- KB 창업/보수 대출 상품 배너 -->
    <button
      type="button"
      class="w-full flex items-center gap-3 px-4 py-4 rounded-2xl bg-primary text-left"
      @click="$emit('open-loan')"
    >
      <Landmark class="w-[18px] h-[18px] text-secondary shrink-0" />
      <span class="flex-1 text-[15px] font-medium text-white truncate"
        >내 상가 맞춤 KB 창업/보수 대출 상품 보기</span
      >
      <ChevronRight class="w-[18px] h-[18px] text-secondary shrink-0" />
    </button>
  </div>
</template>
