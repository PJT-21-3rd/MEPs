<script setup>
import { Phone, MapPin, RefreshCcw } from '@lucide/vue';

// #29 - 리포트 조회 후 해당 지역 공인중개사 안내 영역 표시
// 목업 데이터 기반 (실제 매칭 로직/API는 범위 밖)

defineProps({
  dongName: {
    type: String,
    required: true,
  },
  agent: {
    type: Object,
    required: true,
    // { name: String, company: String, phone: String }
  },
});

defineEmits(['consult', 'refresh']);
</script>

<template>
  <div class="w-[320px] h-[187px] bg-white rounded-2xl shadow-lg p-4 flex flex-col">
    <div class="flex items-center justify-between pb-3 border-b border-surface-gray">
      <div class="flex items-baseline gap-1">
        <p class="text-[15px] font-regular text-button-primary">{{ dongName }}</p>
        <p class="text-[15px] font-regular text-text-main">전문가를 소개합니다</p>
      </div>
      <button
        type="button"
        class="text-text-sub hover:text-text-main"
        aria-label="다른 전문가 보기"
        @click="$emit('refresh')"
      >
        <RefreshCcw class="w-4 h-4" />
      </button>
    </div>

    <div class="flex items-center gap-3 mt-6">
      <div
        class="w-12 h-12 shrink-0 rounded-[14px] bg-primary text-secondary flex items-center justify-center text-[18px] font-bold"
      >
        {{ agent.name[0] }}
      </div>
      <div class="flex-1 flex flex-col">
        <div class="flex items-baseline gap-1">
          <span class="text-[16px] font-medium text-text-main">{{ agent.name }}</span>
          <span class="text-[13px] font-regular text-text-sub">대표</span>
        </div>
        <span class="text-[13px] text-text-badge font-regular">{{ agent.company }}</span>
      </div>
      <a
        :href="`tel:${agent.phone}`"
        class="w-10 h-10 shrink-0 rounded-[14px] bg-[#F0F7FF] flex items-center justify-center"
        aria-label="전화 연결"
      >
        <Phone class="w-4 h-4 text-button-primary" />
      </a>
    </div>

    <button
      type="button"
      class="w-full h-[46px] rounded-[14px] bg-button-primary text-white text-[15px] font-medium flex items-center justify-center gap-1.5 mt-3"
      @click="$emit('consult')"
    >
      <MapPin class="w-4 h-4 shrink-0" />
      상담받고 싶어요
    </button>
  </div>
</template>
