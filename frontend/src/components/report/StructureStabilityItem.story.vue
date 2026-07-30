<script setup>
import StructureStabilityItem from './StructureStabilityItem.vue';

const detailSample = {
  dataPoints: [
    { label: '준공연도', value: '1998년' },
    { label: '최근 정기점검', value: '2024.03' },
    { label: '균열 발견 여부', value: '경미(1건)' },
  ],
  aiOpinion:
    '외벽에서 경미한 균열이 확인되었으나, 구조 전문가 소견상 진행성 균열이 아니며 안전 범위 내로 판단됩니다.',
  insurance: {
    name: '건물 구조안전 특약',
    description: '노후 건물의 구조적 결함으로 인한 손해를 보장하는 특약입니다.',
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

    <Variant title="주의 (summary)">
      <StructureStabilityItem
        status="warning"
        summary="노후 건물로 정밀 점검이 필요합니다."
        mode="summary"
      />
    </Variant>

    <Variant title="주의 (detail, 특약 카드)">
      <StructureStabilityItem
        status="warning"
        summary="노후 건물로 정밀 점검이 필요합니다."
        mode="detail"
        :detail="detailSample"
      />
    </Variant>
  </Story>
</template>
