<script setup>
import SinkholeHistoryItem from './SinkholeHistoryItem.vue';

const detailAiReport = `해당 상가 반경 500m 이내에 최근 지하 굴착 등으로 인한 지반 침하(싱크홀) 사고 이력이 단 한 건도 발생하지 않은 안전한 구역입니다.

지반이 안정적이어서 도로 꺼짐이나 건물 기울어짐으로 인해 영업을 갑작스럽게 중단해야 하는 리스크로부터 매우 자유롭습니다. 안심하고 영업에 집중하실 수 있습니다.

지반 및 붕괴와 관련된 특수한 보험이나 대비책에 예산을 낭비하실 필요가 없습니다. 절약된 예산을 상권 마케팅 비용으로 활용해보세요!`;
</script>

<template>
  <Story title="AI 리포트 컴포넌트/SinkholeHistoryItem" :layout="{ type: 'single', iframe: false }">
    <Variant title="안전 (summary)">
      <SinkholeHistoryItem status="safe" summary="지반침하 이력이 없습니다." mode="summary" />
    </Variant>

    <Variant title="양호 (summary)">
      <SinkholeHistoryItem
        status="good"
        summary="최근 지질 조사 결과 안정적입니다."
        mode="summary"
      />
    </Variant>

    <Variant title="안전 (detail, AI 전문가 의견 — 특약 카드 없음)">
      <SinkholeHistoryItem
        status="safe"
        summary="지반침하 이력이 없습니다."
        mode="detail"
        :ai-report="detailAiReport"
      />
    </Variant>

    <Variant title="주의 (detail, AI 전문가 의견 + 특약 카드)">
      <SinkholeHistoryItem
        status="warning"
        summary="인근 지역 지반침하 사례가 보고되었습니다."
        mode="detail"
        :detail="{
          insurance: {
            name: '붕괴·침강 및 사태 손해 특약',
            description: '건물 노후·지반 침하로 인한 붕괴 손해를 보장합니다.',
          },
        }"
        ai-report="반경 500m 이내에 최근 5년간 지반침하(싱크홀) 사고 이력이 1건 확인되었습니다.

지반 침하가 진행 중인 지역과 인접해 있어, 도로 꺼짐이나 건물 기울어짐 등 예기치 못한 영업 중단 리스크가 존재합니다.

건물 및 시설 손해를 보장하는 붕괴·침강 손해 특약 가입을 권장드립니다."
      />
    </Variant>

    <Variant title="위험 (summary)">
      <SinkholeHistoryItem
        status="danger"
        summary="반경 500m 내 최근 5년간 지반침하 신고 이력이 있어요."
        mode="summary"
      />
    </Variant>
  </Story>
</template>
