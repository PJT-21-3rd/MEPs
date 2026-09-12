<script setup>
import { ref } from 'vue';
import FloodInsuranceBanner from './FloodInsuranceBanner.vue';

const clickLog = ref('아직 클릭 안 함');

function handleOpenInsurance(type) {
  clickLog.value = `open-insurance 이벤트 발생 (type: "${type}")`;
}
</script>

<template>
  <Story
    title="AI 리포트 컴포넌트/FloodInsuranceBanner"
    :layout="{ type: 'single', iframe: false }"
  >
    <Variant title="기본 (중복 안내 없음)">
      <div style="width: 400px; border: 1px solid #eee; padding: 16px">
        <FloodInsuranceBanner @open-insurance="handleOpenInsurance" />
      </div>
    </Variant>

    <Variant title="특약 중복 안내 있음">
      <div style="width: 400px; border: 1px solid #eee; padding: 16px">
        <FloodInsuranceBanner
          flood-overlap-notice="침수이력 추천 특약의 '풍수해 특약'과 보장이 중복돼요"
          @open-insurance="handleOpenInsurance"
        />
      </div>
    </Variant>
  </Story>
</template>
