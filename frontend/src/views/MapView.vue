<template>
  <div class="w-full h-full flex overflow-hidden relative">
    <MainSidebar />
    <div
      v-if="uiStore.isReportOpen"
      class="absolute top-0 bottom-0 left-[400px] z-20 w-[400px] bg-white shadow-2xl border-l border-surface-base flex flex-col"
    >
      <AiReportPanelWithModals
        :building-id="3"
        building-name="그린타워"
        :initial-report-data="warningReport"
        :initial-detail-report-data="warningDetailReport"
      />
    </div>

    <main class="relative h-full flex-1 overflow-hidden">
      <Transition
        enter-active-class="transition-opacity duration-300 ease-out"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition-opacity duration-200 ease-in"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <ScreenRoadView v-if="uiStore.isRoadViewModalOpen" />
      </Transition>

      <NaverMap />

      <div v-if="uiStore.isDetailOpen" class="absolute top-5 left-6 z-20 w-[360px]">
        <SearchBar />
      </div>
      <div v-else><QuickNavigation /></div>

      <MapResearch />
      <MapControls />
    </main>
  </div>
</template>

<script setup>
import MainSidebar from '@/components/layout/MainSidebar.vue';
import MapControls from '@/components/map/MapControls.vue';
import MapResearch from '@/components/map/MapResearch.vue';
import NaverMap from '@/components/map/NaverMap.vue';
import QuickNavigation from '@/components/map/QuickNavigation.vue';
import ScreenRoadView from '@/components/map/ScreenRoadView.vue';
import SearchBar from '@/components/map/SearchBar.vue';
import AiReportPanelWithModals from '@/components/report/AiReportPanelWithModals.vue';
import { useUiStore } from '@/stores/uiStore';

const uiStore = useUiStore();

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
};

const warningDetailReport = {
  overallAiReport:
    '"사장님, 매력적인 상권이지만 장마철 수해와 화재 골든타임에 대한 철저한 대비가 필요한 상가입니다."\n\n이 건물은 뼈대가 튼튼하고 지반이 안정적이라 구조적인 붕괴 위험은 적습니다. 하지만 과거 침수 이력이 있는 지역의 1층 상가이며, 소방차 진입이 어려운 좁은 골목에 위치해 있어 자연재해 및 화재 발생 시 초기 대응이 어려울 수 있습니다. 사장님의 소중한 자산을 지키기 위해 아래의 상세 진단과 솔루션을 반드시 확인해 주십시오.',
  dangerItems: {
    structure: {
      status: 'warning',
      aiReport:
        "해당 건물은 준공 22년 차의 철근콘크리트 구조물이며, 건축물대장상 불법 증축 등 위반건축물 이력이 없는 깨끗한 상가입니다.\n\n철근콘크리트로 지어져 뼈대는 매우 튼튼하지만, 준공 20년이 넘어감에 따라 배관 노후화나 외벽 미세 균열이 시작될 수 있는 시기입니다. 큰 붕괴 위험은 없으나 장마철 미세한 누수가 발생할 수 있습니다.\n\n상가 계약 전 천장 모서리나 화장실 배관 주변의 누수 흔적을 꼼꼼히 체크하시기 바랍니다. 필요하다면 초기 인테리어 시 방수 공사를 병행하는 것을 추천해 드리며, 이때 'KB 상가 시설 보수 대출'을 활용하시면 자금 부담을 줄이실 수 있습니다.",
    },
    fire: {
      status: 'warning',
      aiReport:
        "119 안전센터와의 거리는 1.2km로 양호한 편이나, 상가와 맞닿은 도로의 폭이 8m 미만(세로)이어서 대형 소방차의 즉각적인 진입이 곤란합니다. 또한 건물 지붕 일부에 샌드위치 판넬이 사용되었습니다.\n\n좁은 골목 특성상 불법 주정차 차량이 있을 경우 소방차 진입이 지연되어 '골든타임'을 놓칠 확률이 높습니다. 특히 샌드위치 판넬은 화재 발생 시 연소 속도가 폭발적으로 빠르고 유독가스를 배출하므로, 불길이 이웃 점포로 순식간에 번질 수 있는 치명적인 약점을 가집니다.\n\n점포 내부 인테리어 시 반드시 불연성(방염) 자재를 사용하시고, 소화기를 규정보다 넉넉히 비치하십시오. 타인의 점포로 불이 번질 경우 엄청난 배상 책임이 따르므로, 한도를 높인 'KB 다중이용업소 화재배상책임보험' 가입이 선택이 아닌 필수입니다.",
    },
    sinkhole: {
      status: 'safe',
      aiReport:
        '해당 상가 반경 500m 이내에 최근 지하 굴착 등으로 인한 지반 침하(싱크홀) 사고 이력이 단 한 건도 발생하지 않은 안전한 구역입니다.\n\n지반이 안정적이어서 도로 꺼짐이나 건물 기울어짐으로 인해 영업을 갑작스럽게 중단해야 하는 리스크로부터 매우 자유롭습니다. 안심하고 영업에 집중하실 수 있습니다.\n\n지반 및 붕괴와 관련된 특수한 보험이나 대비책에 예산을 낭비하실 필요가 없습니다. 절약된 예산을 상권 마케팅 비용으로 활용해보세요!',
    },
    flood: {
      status: 'warning',
      aiReport:
        "과거 집중호우 당시 침수 피해가 발생했던 이력이 있는 구역입니다. 설상가상으로 사장님께서 계약하실 매장이 도로와 맞닿은 '지상 1층'에 위치하고 있습니다.\n\n여름철 장마나 태풍 발생 시, 하수구 역류나 도로에 고인 빗물이 매장 안으로 들이닥칠 위험이 매우 큽니다. 이는 인테리어 훼손은 물론, 냉장고 등 고가의 고정 집기 파손과 식자재 폐기, 영업 중단이라는 막대한 금전적 피해로 직결됩니다.\n\n임대인과 협의하여 매장 출입구에 물막이판(차수판)을 반드시 설치하시기 바랍니다. 자연재해는 개인의 힘으로 막을 수 없으므로, 수해로 인한 재산 피해를 실질적으로 보상받을 수 있는 'KB 풍수재해 손해 특약'을 미리 준비하여 최악의 상황에 대비하십시오.",
    },
  },
};
</script>
