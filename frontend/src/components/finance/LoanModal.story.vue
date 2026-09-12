<script setup>
import { ref } from 'vue';
import LoanModal from './LoanModal.vue';

const products = [
  {
    category: '창업 자금',
    name: 'KB사장님+ 마이너스통장',
    rateText: '연 최저 3.8%~',
    description:
      '우량 상권 입점 예정 소상공인을 위한 창업 자금 대출로, 신용등급에 따라 우대금리를 제공합니다.',
  },
  {
    category: '신용대출',
    name: 'KB소상공인 신용대출',
    rateText: '연 최저 4.2%~',
    description: '소상공인 신용등급에 따라 우대금리를 적용받을 수 있는 대출 상품입니다.',
  },
];

const allFourProducts = [
  ...products,
  {
    category: '보증서 대출',
    name: 'KB소상공인 보증서대출(온택트)',
    rateText: '연 최저 3.5%~',
    description: '신용보증재단 보증서 기반 비대면 대출 상품입니다.',
  },
  {
    category: '셀러론',
    name: 'KB셀러론',
    rateText: '연 최저 5.0%~',
    description: '온라인 셀러를 위한 매출 기반 신속 대출 상품입니다.',
  },
];

const isOpen = ref(false);
</script>

<template>
  <Story title="금융 컴포넌트/LoanModal" :layout="{ type: 'single', iframe: false }">
    <Variant title="전체 4개 상품 (실제 스펙)">
      <button
        type="button"
        class="px-4 py-2 rounded-lg bg-primary text-white text-sm"
        @click="isOpen = true"
      >
        모달 열기
      </button>
      <LoanModal
        v-if="isOpen"
        :products="allFourProducts"
        @close="isOpen = false"
        @view-detail="(p) => console.log('상세보기 클릭:', p)"
        @find-branch="() => console.log('영업점 찾기 클릭')"
        @open-app="() => console.log('스타뱅킹 앱 클릭')"
      />
    </Variant>
  </Story>
</template>
