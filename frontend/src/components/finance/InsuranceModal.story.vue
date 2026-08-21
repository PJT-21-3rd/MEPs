<script setup>
import { ref } from 'vue';
import InsuranceModal from './InsuranceModal.vue';

const floodItems = [
  {
    name: '풍수해·지진재해보험(Ⅵ)',
    description:
      '풍수해·지진으로 인한 재물 손해를 보장하는 정책보험으로, 정부·지자체가 보험료의 55% 이상을 지원합니다.',
  },
];

const businessItems = [
  {
    name: '재난배상책임보험',
    description: '1층 음식점 의무 가입 · 미가입 시 과태료 최대 300만 원',
    badge: '필수',
  },
  {
    name: '풍수재손해',
    description: '태풍·홍수·해일 등 풍재·수재로 인한 사업장 재물 손해를 보장하는 유형입니다.',
  },
  {
    name: '급배수시설누출손해',
    description: '급배수시설 누수로 인한 재물 손해를 보장하는 유형입니다.',
  },
];

const isFloodOpen = ref(false);
const isBusinessOpen = ref(false);
</script>

<template>
  <Story title="금융 컴포넌트/InsuranceModal" :layout="{ type: 'single', iframe: false }">
    <Variant title="풍수해보험 모달">
      <button
        type="button"
        class="px-4 py-2 rounded-lg bg-primary text-white text-sm"
        @click="isFloodOpen = true"
      >
        모달 열기
      </button>
      <InsuranceModal
        v-if="isFloodOpen"
        highlight="풍수해보험"
        subtitle="침수 피해 복구비 보장"
        :items="floodItems"
        cta-text="사장님 맞춤 보험 상담 신청하기"
        @close="isFloodOpen = false"
        @submit="isFloodOpen = false"
      />
    </Variant>

    <Variant title="사업장종합보험 모달 (필수 배지 포함)">
      <button
        type="button"
        class="px-4 py-2 rounded-lg bg-primary text-white text-sm"
        @click="isBusinessOpen = true"
      >
        모달 열기
      </button>
      <InsuranceModal
        v-if="isBusinessOpen"
        highlight="KB손해보험"
        subtitle="한빛상가의 진단 결과에 맞춰 필요한 특약을 골라봤어요."
        :items="businessItems"
        cta-text="사장님 맞춤 보험 상담 신청하기"
        @close="isBusinessOpen = false"
        @submit="isBusinessOpen = false"
      />
    </Variant>
  </Story>
</template>
