<script setup>
import AiBriefingCard from './AiBriefingCard.vue';

const shortBriefing =
  '해당 건물은 구조·화재·지반침하·침수 4개 항목 모두 안전 등급으로 평가되었습니다.';

const longBriefing =
  '해당 건물은 준공 후 26년이 경과한 노후 건물로, 최근 정기점검에서 경미한 외벽 균열이 발견되었으나 진행성이 아니어서 안전 범위 내로 판단됩니다. 2023년 인접 상가 화재로 인한 그을림 피해 이력이 있으나 자체 화재 발생 이력은 없으며, 2022년 집중호우 당시 지하 1층 일부 침수가 확인되었으나 이후 배수 시설 보강 공사가 완료되었습니다. 종합적으로 양호 등급에 해당하며, 구조안전 특약과 풍수해 손해 특약 가입을 권장합니다.';
</script>

<template>
  <Story title="AI 리포트 컴포넌트/AiBriefingCard" :layout="{ type: 'single', iframe: false }">
    <Variant title="로딩 (기본값)">
      <AiBriefingCard />
    </Variant>

    <Variant title="짧은 브리핑">
      <AiBriefingCard :loading="false" :briefing="shortBriefing" />
    </Variant>

    <Variant title="긴 브리핑">
      <AiBriefingCard :loading="false" :briefing="longBriefing" />
    </Variant>
  </Story>
</template>
