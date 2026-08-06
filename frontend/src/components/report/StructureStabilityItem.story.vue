<script setup>
import StructureStabilityItem from './StructureStabilityItem.vue';

const detailAiReport = `해당 건물은 준공 22년 차의 철근콘크리트 구조물이며, 건축물대장상 불법 증축 등 위반건축물 이력이 없는 깨끗한 상가입니다.

철근콘크리트로 지어져 뼈대는 매우 튼튼하지만, 준공 20년이 넘어감에 따라 배관 노후화나 외벽 미세 균열이 시작될 수 있는 시기입니다. 큰 붕괴 위험은 없으나 장마철 미세한 누수가 발생할 수 있습니다.

상가 계약 전 천장 모서리나 화장실 배관 주변의 누수 흔적을 꼼꼼히 체크하시기 바랍니다. 필요하다면 초기 인테리어 시 방수 공사를 병행하는 것을 추천해 드리며, 이때 'KB 상가 시설 보수 대출'을 활용하시면 자금 부담을 줄이실 수 있습니다.`;

const detailSample = {
  insurance: {
    name: '노후건물 보장 특약',
    description: '화재·붕괴 등 구조 위험 보장',
  },
};
</script>

<template>
  <Story
    title="AI 리포트 컴포넌트/StructureStabilityItem"
    :layout="{ type: 'single', iframe: false }"
  >
    <Variant title="안전 (summary)">
      <StructureStabilityItem
        status="safe"
        summary="구조 안전성에 특이사항이 없습니다."
        mode="summary"
      />
    </Variant>

    <Variant title="양호 (summary)">
      <StructureStabilityItem
        status="good"
        summary="경미한 균열이 발견되었으나 안전 범위 내입니다."
        mode="summary"
      />
    </Variant>

    <Variant title="주의 (summary, 특약 카드 노출)">
      <StructureStabilityItem
        status="warning"
        summary="노후 건물로 정밀 점검이 필요합니다."
        mode="summary"
        :detail="detailSample"
      />
    </Variant>

    <Variant title="주의 (detail, AI 전문가 의견 3단락 + 특약 카드)">
      <StructureStabilityItem
        status="warning"
        summary="노후 건물로 정밀 점검이 필요합니다."
        mode="detail"
        :detail="detailSample"
        :ai-report="detailAiReport"
      />
    </Variant>

    <Variant title="양호 (detail, AI 전문가 의견만)">
      <StructureStabilityItem
        status="good"
        summary="경미한 균열이 발견되었으나 안전 범위 내입니다."
        mode="detail"
        :ai-report="detailAiReport"
      />
    </Variant>
  </Story>
</template>
