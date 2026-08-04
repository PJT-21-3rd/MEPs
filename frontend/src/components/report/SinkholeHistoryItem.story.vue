<script setup>
import SinkholeHistoryItem from './SinkholeHistoryItem.vue';

const detailSample = {
  insurance: {
    name: '지반침하 손해 특약',
    description: '지반침하로 인한 건물 손상을 보장하는 특약입니다.',
  },
};
</script>

<template>
  <Story title="AI 리포트 컴포넌트/SinkholeHistoryItem" :layout="{ type: 'single', iframe: false }">
    <Variant title="안전">
      <SinkholeHistoryItem status="safe" summary="지반침하 이력이 없습니다." />
    </Variant>

    <Variant title="양호">
      <SinkholeHistoryItem status="good" summary="최근 지질 조사 결과 안정적입니다." />
    </Variant>

    <Variant title="주의 (특약 카드 항상 노출)">
      <SinkholeHistoryItem
        status="warning"
        summary="인근 지역 지반침하 사례가 보고되었습니다."
        :detail="detailSample"
      />
    </Variant>

    <Variant title="위험">
      <SinkholeHistoryItem
        status="danger"
        summary="반경 500m 내 지반침하 이력이 반복적으로 확인되었습니다."
      />
    </Variant>
  </Story>
</template>
