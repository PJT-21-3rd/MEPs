<script setup>
import FireSafetyItem from './FireSafetyItem.vue';

const detailSample = {
  insurance: {
    name: '화재 배상책임 특약',
    description: '화재로 인한 인접 건물 및 제3자 피해를 보장하는 특약입니다.',
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

    <Variant title="주의 (summary)">
      <FireSafetyItem
        status="warning"
        summary="최근 2년 내 소규모 화재 이력이 있습니다."
        mode="summary"
      />
    </Variant>

    <Variant title="위험 (summary)">
      <FireSafetyItem
        status="danger"
        summary="반복적인 화재 이력이 확인되었습니다."
        mode="summary"
      />
    </Variant>

    <Variant title="주의 (detail, 특약 카드)">
      <FireSafetyItem
        status="warning"
        summary="최근 2년 내 소규모 화재 이력이 있습니다."
        mode="detail"
        :detail="detailSample"
      />
    </Variant>
  </Story>
</template>
