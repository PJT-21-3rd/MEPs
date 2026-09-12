<script setup>
import DiagnosticFactorList from './DiagnosticFactorList.vue';

const safeItems = {
  structure: { status: 'safe', summary: '구조 안전성에 특이사항이 없습니다.' },
  fire: { status: 'safe', summary: '최근 3년간 화재 이력이 없습니다.' },
  sinkhole: { status: 'safe', summary: '지반침하 이력이 없습니다.' },
  flood: { status: 'safe', summary: '침수 이력이 없습니다.' },
};

const mixedItems = {
  structure: {
    status: 'good',
    summary: '경미한 균열이 발견되었으나 안전 범위 내입니다.',
    detail: {
      dataPoints: [
        { label: '준공연도', value: '1998년' },
        { label: '최근 정기점검', value: '2024.03' },
      ],
      aiOpinion: '외벽에서 경미한 균열이 확인되었으나 안전 범위 내로 판단됩니다.',
      insurance: {
        name: '건물 구조안전 특약',
        description: '노후 건물의 구조적 결함으로 인한 손해를 보장하는 특약입니다.',
      },
    },
  },
  fire: {
    status: 'warning',
    summary: '최근 2년 내 소규모 화재 이력이 있습니다.',
    detail: {
      dataPoints: [{ label: '화재 이력', value: '경미 1건 (2023)' }],
      aiOpinion: '인접 상가 화재로 인한 그을림 피해가 확인되었습니다.',
      insurance: {
        name: '화재 배상책임 특약',
        description: '화재로 인한 인접 건물 및 제3자 피해를 보장하는 특약입니다.',
      },
    },
  },
  sinkhole: { status: 'safe', summary: '지반침하 이력이 없습니다.' },
  flood: {
    status: 'warning',
    summary: '집중호우 시 침수 이력이 1회 있습니다.',
    detail: {
      dataPoints: [{ label: '침수 이력', value: '1회 (2022.08)' }],
      aiOpinion: '2022년 8월 집중호우 당시 지하 1층 일부 침수가 확인되었습니다.',
      insurance: {
        name: '풍수해 손해 특약',
        description: '집중호우, 태풍 등으로 인한 침수 피해를 보장하는 특약입니다.',
      },
    },
  },
};

const dangerItems = {
  structure: { status: 'danger', summary: '노후로 인한 구조적 위험이 확인되었습니다.' },
  fire: { status: 'danger', summary: '반복적인 화재 이력이 확인되었습니다.' },
  sinkhole: { status: 'warning', summary: '인근 지역 지반침하 사례가 보고되었습니다.' },
  flood: { status: 'danger', summary: '상습 침수 구역으로 반복적인 침수 이력이 있습니다.' },
};
</script>

<template>
  <Story
    title="AI 리포트 컴포넌트/DiagnosticFactorList"
    :layout="{ type: 'single', iframe: false }"
  >
    <Variant title="전체 안전 (summary)">
      <DiagnosticFactorList :items="safeItems" mode="summary" />
    </Variant>

    <Variant title="혼합 등급 (summary)">
      <DiagnosticFactorList :items="mixedItems" mode="summary" />
    </Variant>

    <Variant title="혼합 등급 (detail)">
      <DiagnosticFactorList :items="mixedItems" mode="detail" />
    </Variant>

    <Variant title="위험 등급 포함 (summary)">
      <DiagnosticFactorList :items="dangerItems" mode="summary" />
    </Variant>
  </Story>
</template>
