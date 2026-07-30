<script setup>
import FloodHistoryItem from './FloodHistoryItem.vue';

const detailSample = {
  insurance: {
    name: '풍수해 손해 특약',
    description: '집중호우, 태풍 등으로 인한 침수 피해를 보장하는 특약입니다.',
  },
};
</script>

<template>
  <Story title="AI 리포트 컴포넌트/FloodHistoryItem" :layout="{ type: 'single', iframe: false }">
    <Variant title="안전 (summary)">
      <FloodHistoryItem status="safe" summary="침수 이력이 없습니다." mode="summary" />
    </Variant>

    <Variant title="양호 (summary)">
      <FloodHistoryItem status="good" summary="배수 시설이 양호한 상태입니다." mode="summary" />
    </Variant>

    <Variant title="주의 (summary)">
      <FloodHistoryItem
        status="warning"
        summary="집중호우 시 침수 이력이 1회 있습니다."
        mode="summary"
      />
    </Variant>

    <Variant title="위험 (summary)">
      <FloodHistoryItem
        status="danger"
        summary="상습 침수 구역으로 반복적인 침수 이력이 있습니다."
        mode="summary"
      />
    </Variant>

    <Variant title="주의 (detail, 특약 카드)">
      <FloodHistoryItem
        status="warning"
        summary="집중호우 시 침수 이력이 1회 있습니다."
        mode="detail"
        :detail="detailSample"
      />
    </Variant>
  </Story>
</template>
