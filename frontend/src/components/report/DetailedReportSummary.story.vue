<script setup>
import DetailedReportSummary from './DetailedReportSummary.vue';

const aiReport = `"사장님, 매력적인 상권이지만 장마철 수해와 화재 골든타임에 대한 철저한 대비가 필요한 상가입니다."

이 건물은 뼈대가 튼튼하고 지반이 안정적이라 구조적인 붕괴 위험은 적습니다. 하지만 과거 침수 이력이 있는 지역의 1층 상가이며, 소방차 진입이 어려운 좁은 골목에 위치해 있어 자연재해 및 화재 발생 시 초기 대응이 어려울 수 있습니다. 사장님의 소중한 자산을 지키기 위해 아래의 상세 진단과 솔루션을 반드시 확인해 주십시오.`;
</script>

<template>
  <Story
    title="AI 리포트 컴포넌트/DetailedReportSummary"
    :layout="{ type: 'single', iframe: false }"
  >
    <Variant title="기본 (2단락)">
      <div style="width: 400px" class="p-4 bg-white">
        <DetailedReportSummary grade="warning" :ai-report="aiReport" />
      </div>
    </Variant>

    <Variant title="짧은 텍스트 (1단락)">
      <div style="width: 400px" class="p-4 bg-white">
        <DetailedReportSummary
          grade="safe"
          ai-report="이 건물은 4대 안전 진단 항목 모두에서 우수한 평가를 받았습니다."
        />
      </div>
    </Variant>
  </Story>
</template>
