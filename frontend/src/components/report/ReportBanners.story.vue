<script setup>
import { ref } from 'vue';
import ReportBanners from './ReportBanners.vue';

const lastEvent = ref('아직 클릭 없음');

function handleOpenInsurance(type) {
  lastEvent.value = `open-insurance emit 발생 · type="${type}"`;
}

function handleOpenLoan() {
  lastEvent.value = 'open-loan emit 발생';
}
</script>

<template>
  <Story title="AI 리포트 컴포넌트/ReportBanners">
    <Variant title="기본 (3개 배너 노출 + emit 확인)">
      <div style="width: 400px" class="px-4 py-2 bg-white flex flex-col gap-4">
        <ReportBanners
          flood-overlap-notice='침수이력 추천 특약의 "풍수해 특약"과 보장이 중복돼요'
          @open-insurance="handleOpenInsurance"
          @open-loan="handleOpenLoan"
        />
        <p class="text-xs text-text-sub border-t border-surface-gray pt-2">
          마지막 이벤트: {{ lastEvent }}
        </p>
      </div>
    </Variant>
  </Story>
</template>
