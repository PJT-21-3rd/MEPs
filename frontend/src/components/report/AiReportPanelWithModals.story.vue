<script setup>
import AiReportPanelWithModals from './AiReportPanelWithModals.vue';

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
        insurance: { name: '화재배상책임 특약', description: '화재로 인한 타인 피해 보장' },
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
  disasterLiability: {
    required: true,
    description: '1층 음식점 의무 가입 · 미가입 시 과태료 최대 300만 원',
    evidenceTags: ['지상 1층', '업종 카페'],
  },
  fireLiability: {
    required: false,
    description: '다중이용업소 미해당 · 바닥면적 기준 미달',
    evidenceTags: [],
  },
  insuranceRidersByFactor: {
    fire: [
      {
        coverageType: 'RIDER',
        name: '화재배상책임',
        description: '화재로 인한 타인 피해 보장',
      },
    ],
    flood: [
      {
        coverageType: 'RIDER',
        name: '풍수재손해',
        description: '태풍·홍수·해일 등 풍재·수재로 인한 사업장 재물 손해를 보장하는 유형입니다.',
      },
      {
        coverageType: 'PRODUCT',
        name: '풍수해·지진재해보험(Ⅵ)',
        description:
          '풍수해·지진으로 인한 재물 손해를 보장하는 정책보험으로, 정부·지자체가 보험료의 55% 이상을 지원합니다.',
      },
    ],
  },
};

const warningDetailReport = {
  overallAiReport:
    '"사장님, 매력적인 상권이지만 장마철 수해와 화재 골든타임에 대한 철저한 대비가 필요한 상가입니다."\n\n이 건물은 뼈대가 튼튼하고 지반이 안정적이라 구조적인 붕괴 위험은 적습니다. 하지만 과거 침수 이력이 있는 지역의 1층 상가이며, 소방차 진입이 어려운 좁은 골목에 위치해 있어 자연재해 및 화재 발생 시 초기 대응이 어려울 수 있습니다.',
  dangerItems: {
    structure: {
      status: 'good',
      aiReport:
        '해당 건물은 준공 22년 차의 철근콘크리트 구조물이며, 건축물대장상 불법 증축 등 위반건축물 이력이 없는 깨끗한 상가입니다.\n\n철근콘크리트로 지어져 뼈대는 매우 튼튼하지만, 준공 20년이 넘어감에 따라 배관 노후화나 외벽 미세 균열이 시작될 수 있는 시기입니다.\n\n상가 계약 전 천장 모서리나 화장실 배관 주변의 누수 흔적을 꼼꼼히 체크하시기 바랍니다.',
    },
    fire: {
      status: 'warning',
      aiReport:
        "119 안전센터와의 거리는 1.2km로 양호한 편이나, 상가와 맞닿은 도로의 폭이 8m 미만(세로)이어서 대형 소방차의 즉각적인 진입이 곤란합니다.\n\n좁은 골목 특성상 불법 주정차 차량이 있을 경우 소방차 진입이 지연되어 '골든타임'을 놓칠 확률이 높습니다.\n\n한도를 높인 'KB 다중이용업소 화재배상책임보험' 가입이 선택이 아닌 필수입니다.",
    },
    sinkhole: {
      status: 'safe',
      aiReport:
        '해당 상가 반경 500m 이내에 최근 지하 굴착 등으로 인한 지반 침하(싱크홀) 사고 이력이 단 한 건도 발생하지 않은 안전한 구역입니다.\n\n지반이 안정적이어서 도로 꺼짐이나 건물 기울어짐으로 인해 영업을 갑작스럽게 중단해야 하는 리스크로부터 매우 자유롭습니다.',
    },
    flood: {
      status: 'warning',
      aiReport:
        "과거 집중호우 당시 침수 피해가 발생했던 이력이 있는 구역입니다. 설상가상으로 사장님께서 계약하실 매장이 도로와 맞닿은 '지상 1층'에 위치하고 있습니다.\n\n여름철 장마나 태풍 발생 시, 하수구 역류나 도로에 고인 빗물이 매장 안으로 들이닥칠 위험이 매우 큽니다.\n\n'KB 풍수재해 손해 특약'을 미리 준비하여 최악의 상황에 대비하십시오.",
    },
  },
};

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
  disasterLiability: {
    required: false,
    description: '1층 미해당 · 의무 가입 대상 아님',
    evidenceTags: [],
  },
  fireLiability: {
    required: false,
    description: '다중이용업소 미해당 · 바닥면적 기준 미달',
    evidenceTags: [],
  },
  insuranceRidersByFactor: {},
};

const safeDetailReport = {
  overallAiReport:
    '"사장님, 이 상가는 4대 안전 진단 항목 모두에서 우수한 평가를 받은 안심할 수 있는 매물입니다."\n\n구조·화재·지반침하·침수 이력 어디에서도 특별한 위험 신호가 발견되지 않았습니다.',
  dangerItems: {
    structure: {
      status: 'safe',
      aiReport:
        '해당 건물은 준공 5년 차의 철근콘크리트 구조물로, 내진설계가 적용된 신축 상가입니다.\n\n구조적으로 매우 안정적이며 위반건축물 이력도 전혀 없습니다.',
    },
    fire: {
      status: 'safe',
      aiReport:
        '119 안전센터와의 거리가 600m로 매우 가까워 화재 발생 시 신속한 초기 대응이 가능합니다.\n\n최근 5년간 동일 건물 및 인접 상가에서 화재 이력이 전혀 확인되지 않았습니다.',
    },
    sinkhole: {
      status: 'safe',
      aiReport:
        '반경 500m 이내에 지반침하 사고 이력이 전혀 없는 안정적인 지반입니다.\n\n인근 대규모 공사 현장도 없어 지반 침하 리스크가 매우 낮습니다.',
    },
    flood: {
      status: 'safe',
      aiReport:
        '해당 지번은 과거 침수 이력이 전혀 확인되지 않은 지역입니다.\n\n주변 배수시설도 양호하게 관리되고 있어 집중호우 시에도 침수 위험이 낮습니다.',
    },
  },
};
</script>

<template>
  <Story
    title="AI 리포트 컴포넌트/AiReportPanelWithModals"
    :layout="{ type: 'single', iframe: false }"
  >
    <Variant title="주의 등급 - 배너 클릭 → 모달 전체 흐름 확인">
      <div style="width: 400px; height: 700px; border: 1px solid #eee; position: relative">
        <AiReportPanelWithModals
          :building-id="3"
          building-name="그린타워"
          :initial-report-data="warningReport"
          :initial-detail-report-data="warningDetailReport"
        />
      </div>
    </Variant>

    <Variant title="안전 등급 - 특약/의무보험 없음 (엣지케이스)">
      <div style="width: 400px; height: 700px; border: 1px solid #eee; position: relative">
        <AiReportPanelWithModals
          :building-id="1"
          building-name="역삼 스타빌딩"
          :initial-report-data="safeReport"
          :initial-detail-report-data="safeDetailReport"
        />
      </div>
    </Variant>
  </Story>
</template>
