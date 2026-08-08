<script setup>
import FireSafetyItem from './FireSafetyItem.vue';

const detailAiReport = `119 안전센터와의 거리는 1.2km로 양호한 편이나, 상가와 맞닿은 도로의 폭이 8m 미만(세로)이어서 대형 소방차의 즉각적인 진입이 곤란합니다. 또한 건물 지붕 일부에 샌드위치 판넬이 사용되었습니다.

좁은 골목 특성상 불법 주정차 차량이 있을 경우 소방차 진입이 지연되어 '골든타임'을 놓칠 확률이 높습니다. 특히 샌드위치 판넬은 화재 발생 시 연소 속도가 폭발적으로 빠르고 유독가스를 배출하므로, 불길이 이웃 점포로 순식간에 번질 수 있는 치명적인 약점을 가집니다.

점포 내부 인테리어 시 반드시 불연성(방염) 자재를 사용하시고, 소화기를 규정보다 넉넉히 비치하십시오. 타인의 점포로 불이 번질 경우 엄청난 배상 책임이 따르므로, 한도를 높인 'KB 다중이용업소 화재배상책임보험' 가입이 선택이 아닌 필수입니다.`;

const detailSample = {
  insurance: {
    name: '화재배상책임 특약',
    description: '화재로 인한 타인 피해 보장',
  },
};
</script>

<template>
  <Story title="AI 리포트 컴포넌트/FireSafetyItem" :layout="{ type: 'single', iframe: false }">
    <Variant title="안전 (summary)">
      <FireSafetyItem status="safe" summary="최근 3년간 화재 이력이 없습니다." mode="summary" />
    </Variant>

    <Variant title="양호 (summary)">
      <FireSafetyItem status="good" summary="소방시설 점검이 최근 완료되었습니다." mode="summary" />
    </Variant>

    <Variant title="주의 (summary, 특약 카드 노출)">
      <FireSafetyItem
        status="warning"
        summary="최근 2년 내 소규모 화재 이력이 있습니다."
        mode="summary"
        :detail="detailSample"
      />
    </Variant>

    <Variant title="주의 (detail, AI 전문가 의견 3단락 + 특약 카드)">
      <FireSafetyItem
        status="warning"
        summary="최근 2년 내 소규모 화재 이력이 있습니다."
        mode="detail"
        :detail="detailSample"
        :ai-report="detailAiReport"
      />
    </Variant>
  </Story>
</template>
