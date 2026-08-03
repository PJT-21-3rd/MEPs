<script setup>
import AiReportPanel from './AiReportPanel.vue';

const safeReport = {
  score: 94,
  grade: 'safe',
  overallBriefing:
    '해당 건물은 구조·화재·지반침하·침수 4개 항목 모두 안전 등급으로 평가되었습니다.',
  dangerItems: {
    structure: { status: 'safe', summary: '구조 안전성에 특이사항이 없습니다.' },
    fire: { status: 'safe', summary: '최근 3년간 화재 이력이 없습니다.' },
    sinkhole: { status: 'safe', summary: '지반침하 이력이 없습니다.' },
    flood: { status: 'safe', summary: '침수 이력이 없습니다.' },
  },
};

const goodReport = {
  score: 84,
  grade: 'good',
  overallBriefing:
    '해당 건물은 전반적으로 양호한 상태이나, 경미한 노후화가 확인되어 정기적인 점검을 권장합니다.',
  dangerItems: {
    structure: { status: 'good', summary: '경미한 균열이 발견되었으나 안전 범위 내입니다.' },
    fire: { status: 'good', summary: '소방시설 점검이 최근 완료되었습니다.' },
    sinkhole: { status: 'safe', summary: '지반침하 이력이 없습니다.' },
    flood: { status: 'good', summary: '배수 시설이 양호한 상태입니다.' },
  },
};

const warningReport = {
  score: 76,
  grade: 'warning',
  overallBriefing:
    '해당 건물은 화재 및 침수 이력이 확인되어 주의가 필요합니다. 관련 특약 가입을 권장합니다.',
  dangerItems: {
    structure: { status: 'good', summary: '경미한 균열이 발견되었으나 안전 범위 내입니다.' },
    fire: {
      status: 'warning',
      summary: '최근 2년 내 소규모 화재 이력이 있습니다.',
      detail: {
        insurance: {
          name: '화재배상책임 특약',
          description: '화재로 인한 타인 피해 보장',
        },
      },
    },
    sinkhole: { status: 'safe', summary: '지반침하 이력이 없습니다.' },
    flood: {
      status: 'warning',
      summary: '집중호우 시 침수 이력이 1회 있습니다.',
      detail: {
        insurance: {
          name: '풍수해 손해 특약',
          description: '집중호우, 태풍 등으로 인한 침수 피해를 보장하는 특약입니다.',
        },
      },
    },
  },
};

const dangerReport = {
  score: 42,
  grade: 'danger',
  overallBriefing:
    '해당 건물은 구조·화재·침수 항목에서 위험 등급이 확인되어 정밀 점검이 필요합니다.',
  dangerItems: {
    structure: { status: 'danger', summary: '노후로 인한 구조적 위험이 확인되었습니다.' },
    fire: { status: 'danger', summary: '반복적인 화재 이력이 확인되었습니다.' },
    sinkhole: { status: 'warning', summary: '인근 지역 지반침하 사례가 보고되었습니다.' },
    flood: { status: 'danger', summary: '상습 침수 구역으로 반복적인 침수 이력이 있습니다.' },
  },
};
</script>

<template>
  <Story title="AI 리포트 컴포넌트/AiReportPanel" :layout="{ type: 'single', iframe: false }">
    <Variant title="안전 등급 (safe)">
      <div style="width: 400px; height: 700px; border: 1px solid #eee">
        <AiReportPanel
          :building-id="1"
          building-name="역삼 스타빌딩"
          :initial-report-data="safeReport"
        />
      </div>
    </Variant>

    <Variant title="양호 등급 (good)">
      <div style="width: 400px; height: 700px; border: 1px solid #eee">
        <AiReportPanel
          :building-id="2"
          building-name="센트럴프라자"
          :initial-report-data="goodReport"
        />
      </div>
    </Variant>

    <Variant title="주의 등급 (warning)">
      <div style="width: 400px; height: 700px; border: 1px solid #eee">
        <AiReportPanel
          :building-id="3"
          building-name="그린타워"
          :initial-report-data="warningReport"
        />
      </div>
    </Variant>

    <Variant title="위험 등급 (danger)">
      <div style="width: 400px; height: 700px; border: 1px solid #eee">
        <AiReportPanel
          :building-id="4"
          building-name="동양빌딩"
          :initial-report-data="dangerReport"
        />
      </div>
    </Variant>

    <Variant title="로딩 중 (forceLoading)">
      <div style="width: 400px; height: 700px; border: 1px solid #eee">
        <AiReportPanel :building-id="999" force-loading />
      </div>
    </Variant>
  </Story>
</template>
