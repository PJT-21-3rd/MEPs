<script setup>
import FloodHistoryItem from './FloodHistoryItem.vue';

const detailAiReport = `과거 집중호우 당시 침수 피해가 발생했던 이력이 있는 구역입니다. 설상가상으로 사장님께서 계약하실 매장이 도로와 맞닿은 '지상 1층'에 위치하고 있습니다.

여름철 장마나 태풍 발생 시, 하수구 역류나 도로에 고인 빗물이 매장 안으로 들이닥칠 위험이 매우 큽니다. 이는 인테리어 훼손은 물론, 냉장고 등 고가의 고정 집기 파손과 식자재 폐기, 영업 중단이라는 막대한 금전적 피해로 직결됩니다.

임대인과 협의하여 매장 출입구에 물막이판(차수판)을 반드시 설치하시기 바랍니다. 자연재해는 개인의 힘으로 막을 수 없으므로, 수해로 인한 재산 피해를 실질적으로 보상받을 수 있는 'KB 풍수재해 손해 특약'을 미리 준비하여 최악의 상황에 대비하십시오.`;

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

    <Variant title="주의 (summary, 특약 카드 노출)">
      <FloodHistoryItem
        status="warning"
        summary="집중호우 시 침수 이력이 1회 있습니다."
        mode="summary"
        :detail="detailSample"
      />
    </Variant>

    <Variant title="주의 (detail, AI 전문가 의견 3단락 + 특약 카드)">
      <FloodHistoryItem
        status="warning"
        summary="집중호우 시 침수 이력이 1회 있습니다."
        mode="detail"
        :detail="detailSample"
        :ai-report="detailAiReport"
      />
    </Variant>

    <Variant title="위험 (summary)">
      <FloodHistoryItem
        status="danger"
        summary="상습 침수 구역으로 반복적인 침수 이력이 있습니다."
        mode="summary"
      />
    </Variant>
  </Story>
</template>
